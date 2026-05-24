package com.example.shopping3.service.impl;

import com.example.shopping3.entity.User;
import com.example.shopping3.mapper.UserMapper;
import com.example.shopping3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUserName(username);
        if (user != null) {
            System.out.println("=== 登录调试 ===");
            System.out.println("用户名: " + username);
            System.out.println("输入的明文密码: " + password);
            System.out.println("数据库BCrypt密码: " + user.getPassword());
            System.out.println("密码长度: " + user.getPassword().length());
            
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("密码匹配结果: " + matches);
            System.out.println("===============\n");
            
            if (matches) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        User existUser = userMapper.selectByUserName(user.getUsername());
        if (existUser != null) {
            return false;
        }

        if (user.getAvatar() == null) {
            user.setAvatar("/images/default-avatar.png");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        int rows = userMapper.insertUser(user);
        return rows > 0;
    }

    @Override
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean updateById(User user) {
        return userMapper.updateUser(user) > 0;
    }
}
