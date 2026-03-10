package com.example.shopping3;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 核心注解：标记SpringBoot应用，扫描当前包及子包的组件
@SpringBootApplication
// 扫描Mapper接口所在包（替代每个Mapper加@Mapper注解，二选一即可）
@MapperScan("com.example.shopping3.mapper")
public class Shopping3Application {

    public static void main(String[] args) {
        // 启动SpringBoot应用
        SpringApplication.run(Shopping3Application.class, args);
    }

}