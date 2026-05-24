package com.example.shopping3.entity;

import lombok.Data;

@Data
public class Admin {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String role;
    private String phone;
    private String email;
}