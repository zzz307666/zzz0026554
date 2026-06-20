package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhjt.entity.Role;
import com.hhjt.entity.User;
import com.hhjt.mapper.RoleMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RoleMapper roleMapper;

    // BCrypt编码器（与SecurityConfig保持一致）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // 测试用：生成abc123的BCrypt密文（运行main方法获取）
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String bcryptPwd = encoder.encode("abc123");
        System.out.println("abc123 的 BCrypt 密文：" + bcryptPwd);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("尝试登录的用户名：{}", username);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);

        User user = getBaseMapper().selectOne(userWrapper);
        log.info("查询到的用户：{}", user == null ? "不存在" : user.toString());

        // 1. 用户名不存在
        if (user == null) {
            log.error("用户名不存在：{}", username);
            throw new UsernameNotFoundException("用户名不存在");
        }

        // 2. 角色校验
        Role role = roleMapper.selectById(user.getRoleId());
        log.info("用户{}的角色：{}", username, role == null ? "不存在" : role.toString());
        if (role == null) {
            log.error("用户{}未配置角色", username);
            throw new UsernameNotFoundException("用户未配置角色");
        }
        user.setRole(role);

        return user;
    }
}