package com.hhjt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.avatar-path}")
    private String avatarPath;

    @Value("${upload.access-path}")
    private String accessPath;

    /**
     * 配置静态资源映射（关键：确保上传的头像能被访问）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射头像上传目录
        registry.addResourceHandler(accessPath + "**")
                .addResourceLocations("file:" + avatarPath);

        // 映射默认静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}