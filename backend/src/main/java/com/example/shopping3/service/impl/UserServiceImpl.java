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

    // 【回退】登录逻辑：直接明文比对
    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUserName(username);
        if (user != null) {
            // 直接使用 equals 比对明文密码
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // 【回退】注册逻辑：直接存明文
    @Override
    public boolean register(User user) {
        // 检查用户名是否存在
        User existUser = userMapper.selectByUserName(user.getUsername());
        if (existUser != null) {
            return false;
        }

        // 默认头像
        if (user.getAvatar() == null) {
            user.setAvatar("/images/default-avatar.png");
        }

        int rows = userMapper.insertUser(user);
        return rows > 0;
    }

    @Override
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean updateById(User user) {
        // 如果前端传了密码过来，这里也是直接存明文
        // 注意：通常修改资料接口不应该包含密码字段，除非是专门的“修改密码”接口
        return userMapper.updateUser(user) > 0;
    }

    /* 加密辅助方法
    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    @Override
    public String encryptPassword(String rawPassword) {
        return rawPassword; // 明文直接返回
    }
     */
}