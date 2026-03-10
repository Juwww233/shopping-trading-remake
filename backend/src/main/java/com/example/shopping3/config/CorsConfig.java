package com.example.shopping3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口跨域
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有域名（生产环境指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600);  // 预检请求有效期（秒）
    }
}