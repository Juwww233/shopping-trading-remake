package com.example.shopping3.mapper;

import com.example.shopping3.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AdminMapper {
    Admin selectByUsername(String username);
    Admin selectById(Integer id);
    List<Admin> selectAll();
    int insert(Admin admin);
    int update(Admin admin);
    int delete(Integer id);
}