package com.example.shopping3.mapper;

import com.example.shopping3.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 根据用户名查询
    User selectByUserName(@Param("username") String username);

    // 新增用户
    int insertUser(User user);

    // 【新增】根据ID查询用户
    User selectById(@Param("id") Integer id);

    // 【新增】更新用户 (动态更新非空字段)
    int updateUser(User user);
}