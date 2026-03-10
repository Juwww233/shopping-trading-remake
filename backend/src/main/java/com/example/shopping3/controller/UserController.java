package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.UserService;
import com.example.shopping3.util.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            String sessionId = UUID.randomUUID().toString();
            // 核心新增：存储sessionId和用户信息
            SessionManager.setSession(sessionId, user);

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId); // 返回给前端
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            return Result.success(data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    // 新增：登出接口（可选）
    @PostMapping("/logout")
    public Result<String> logout(String sessionId) {
        SessionManager.removeSession(sessionId);
        return Result.success("登出成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User registerUser) {
        boolean success = userService.register(registerUser);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("用户名已存在，注册失败");
        }
    }
}