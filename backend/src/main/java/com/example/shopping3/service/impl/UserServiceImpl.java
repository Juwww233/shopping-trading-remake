package com.example.shopping3.service.impl;

import com.example.shopping3.entity.User;
import com.example.shopping3.mapper.UserMapper;
import com.example.shopping3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 登录逻辑（明文密码匹配）
    @Override
    public User login(String username, String password) {
        // 根据用户名查询用户
        User user = userMapper.selectByUserName(username);
        // 验证用户是否存在 + 密码是否匹配
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // 注册逻辑
    @Override
    public boolean register(User user) {
        // 先检查用户名是否已存在
        User existUser = userMapper.selectByUserName(user.getUsername());
        if (existUser != null) {
            return false;  // 用户名已存在
        }
        // 新增用户
        int rows = userMapper.insertUser(user);
        return rows > 0;
    }
}