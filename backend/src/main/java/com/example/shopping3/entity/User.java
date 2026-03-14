package com.example.shopping3.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;          // 用户ID
    private String username;     // 用户名（登录用）
    private String password;     // 密码
    private String name;         // 真实姓名
    private String phone;        // 手机号
    private String role;         // 角色：user/merchant/admin/seller
    private String avatar;       // 头像
    private String email;        // 邮箱
}