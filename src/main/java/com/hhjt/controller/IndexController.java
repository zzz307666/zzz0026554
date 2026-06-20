package com.hhjt.controller;

import com.hhjt.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    // 统一首页（登录+操作入口）
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        // 获取当前登录用户信息（含角色）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = (User) authentication.getPrincipal();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("realName", user.getRealName());
            model.addAttribute("roleName", user.getRole().getRoleName()); // 角色名称
        }
        return "index";
    }
}