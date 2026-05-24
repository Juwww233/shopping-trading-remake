package com.example.shopping3.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String[] passwords = {"123456", "333333", "111111", "222222", "444444", "000", "444", "333", "admin123"};
        
        System.out.println("=== BCrypt 密码生成器 ===\n");
        
        for (String pwd : passwords) {
            String encoded = encoder.encode(pwd);
            System.out.println("明文: " + pwd);
            System.out.println("BCrypt: " + encoded);
            System.out.println();
        }
        
        System.out.println("=== 生成完成 ===");
    }
}
