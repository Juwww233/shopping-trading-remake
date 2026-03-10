package com.example.shopping3.mapper;

import com.example.shopping3.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 根据用户名查询用户（登录）
    User selectByUserName(String username);

    // 新增用户（注册）
    int insertUser(User user);
}