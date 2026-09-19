package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射：
 * 把 /avatars/** 这种 URL 映射到本地磁盘目录，让前端能访问到上传的头像
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:D:/buka-shop-files/avatars}")
    private String avatarDir;

    @Value("${app.upload.avatar-url:/avatars}")
    private String avatarUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把访问路径 /avatars/** 映射到本地磁盘目录 file:D:/buka-shop-files/avatars/
        String diskPath = "file:" + avatarDir;
        if (!diskPath.endsWith("/") && !diskPath.endsWith("\\")) {
            diskPath = diskPath + "/";
        }
        registry.addResourceHandler(avatarUrlPrefix + "/**")
                .addResourceLocations(diskPath);
    }
}
