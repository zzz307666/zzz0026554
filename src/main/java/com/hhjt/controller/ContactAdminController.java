package com.hhjt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contact-admin")
public class ContactAdminController {

    // 跳转联系管理员页面
    @GetMapping
    public String contactAdminPage() {
        return "contact-admin";
    }
}