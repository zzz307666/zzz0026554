package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@TableName("sys_user")
public class User implements UserDetails {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Long roleId; // 1-管理员 2-教师 3-学生
    private Integer status; // 0-禁用 1-正常
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String avatar; // 用户头像地址
    private String signature; // 个性签名
    private transient Role role;
    @TableField("login_error_count")
    private Integer loginErrorCount; // 登录错误次数
    @TableField("reset_pwd_error_count")
    private Integer resetPwdErrorCount; // 重置密码错误次数
    @TableField("first_error_time")
    private LocalDateTime firstErrorTime; // 首次错误时间

    // 新增：判断是否为管理员
    public boolean isAdmin() {
        return roleId != null && roleId == 1L;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        return status == 1; // 只有状态为1的用户才是启用的
    }
}