package com.hhjt.config;

import com.hhjt.entity.User;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.UserService;
import com.hhjt.utils.ErrorCountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.net.URLEncoder;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ErrorCountUtil errorCountUtil;

    // 核心修改：BCrypt密码编码器（替换原MD5）
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 加密强度10，兼顾安全与性能
        return new BCryptPasswordEncoder(10);
    }

    // 登录失败处理器（保持逻辑，适配BCrypt加密）
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String errorMsg;
            String username = request.getParameter("username");
            log.error("用户{}登录失败，原因：{}", username, exception.getMessage());

            // 1. 处理异常类型
            if (exception instanceof UsernameNotFoundException ||
                    exception instanceof InternalAuthenticationServiceException) {
                errorMsg = "用户名不存在";
            } else if (exception instanceof BadCredentialsException) {
                errorMsg = "密码错误";
            } else if (exception instanceof DisabledException) {
                errorMsg = "账号已禁用，请联系管理员";
            } else {
                errorMsg = "用户名或密码错误";
            }

            // 2. 统计错误次数（持久化到数据库）
            if (username != null && !"".equals(username) &&
                    !(exception instanceof UsernameNotFoundException)) {
                User user = userMapper.selectByUsername(username);
                if (user != null && user.getStatus() == 1) {
                    boolean isOverLimit = errorCountUtil.addErrorCount(user, ErrorCountUtil.TYPE_LOGIN);
                    if (isOverLimit) {
                        user.setStatus(0);
                        errorMsg = "单日登录/重置密码错误超8次，账号已禁用，请联系管理员";
                        log.error("用户{}单日错误超8次，已自动禁用", username);
                    }
                    user.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(user); // 错误次数写入数据库
                }
            }

            // 3. 重定向携带错误信息
            String encodedMsg = URLEncoder.encode(errorMsg, "UTF-8");
            response.sendRedirect("/?error=true&msg=" + encodedMsg);
        };
    }

    // 登录成功处理器（重置错误计数）
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, auth) -> {
            User user = (User) auth.getPrincipal();
            log.info("登录成功，用户角色：{}", auth.getAuthorities().iterator().next().getAuthority());
            // 登录成功→重置错误计数（持久化到数据库）
            errorCountUtil.resetErrorCount(user);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 核心修复：设置session中的userId属性，供后续业务逻辑使用
            request.getSession().setAttribute("userId", user.getId());

            // 角色跳转逻辑不变
            String role = auth.getAuthorities().iterator().next().getAuthority();
            switch (role) {
                case "ROLE_ADMIN":
                    response.sendRedirect("/admin/index");
                    break;
                case "ROLE_TEACHER":
                    response.sendRedirect("/teacher/index");
                    break;
                case "ROLE_STUDENT":
                    response.sendRedirect("/student/index");
                    break;
                default:
                    response.sendRedirect("/index");
            }
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/css/**", "/js/**", "/images/**","/image/**", "/lib/**", "/", "/login", "/doLogin", "/error",
                        "/reset-password/**", "/contact-admin", "/profile/**")
                .permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 管理员和教师都可以访问教师端功能
                .antMatchers("/teacher/**").access("hasRole('TEACHER') or hasRole('ADMIN')")
                // 学生端页面需要学生角色，但排名页面允许所有认证用户访问
                .antMatchers("/student/ranking", "/student/ranking/data").authenticated()
                // 积分兑换功能
                .antMatchers("/student/exchange/**").hasRole("STUDENT")
                .antMatchers("/admin/exchange/**").hasRole("ADMIN")
                .antMatchers("/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/")
                .loginProcessingUrl("/doLogin")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureHandler(customAuthenticationFailureHandler())
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .and().headers().frameOptions().disable();
    }
}