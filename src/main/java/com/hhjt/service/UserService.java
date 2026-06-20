package com.hhjt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhjt.entity.User; // 替换Teacher为User
import org.springframework.security.core.userdetails.UserDetailsService;

// 关键修改：IService<Teacher> → IService<User>
public interface UserService extends IService<User>, UserDetailsService {
    // 继承IService（MyBatis-Plus，绑定User实体） + UserDetailsService（Spring Security）
}