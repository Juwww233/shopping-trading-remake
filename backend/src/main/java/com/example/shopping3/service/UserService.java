package com.example.shopping3.service;

import com.example.shopping3.entity.User;

public interface UserService {
    // 登录：根据用户名查询用户
    User login(String username, String password);

    // 注册：新增用户
    boolean register(User user);
}