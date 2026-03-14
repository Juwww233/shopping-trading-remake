package com.example.shopping3.service;

import com.example.shopping3.entity.User;

public interface UserService {
    // 登录
    User login(String username, String password);

    // 注册
    boolean register(User user);

    // 根据ID获取用户
    User getById(Integer id);

    // 更新用户信息 (用于修改资料、头像)
    boolean updateById(User user);

    // 加密/比对接口，安全功能实现。

    // boolean checkPassword(String rawPassword, String encodedPassword);
    // String encryptPassword(String rawPassword);
}