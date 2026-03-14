package com.example.shopping3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录 (解决 IDE 运行和打包运行路径不一致的问题)
        // 假设上传目录在: shopping3/backend/src/main/resources/static/uploads
        String uploadPath = "src/main/resources/static/uploads/";

        // 转换为绝对路径
        Path path = Paths.get(uploadPath);
        String absolutePath = path.toFile().getAbsolutePath();

        // 打印日志方便调试，确认路径是否正确
        System.out.println("📂 头像资源映射路径: " + absolutePath);

        // 映射 URL: http://localhost:8080/uploads/xxx.jpg
        // 到本地文件: file:/绝对路径/xxx.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}