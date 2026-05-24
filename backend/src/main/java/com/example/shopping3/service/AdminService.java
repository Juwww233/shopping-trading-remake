package com.example.shopping3.service;

import com.example.shopping3.entity.Admin;
import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> login(String username, String password);
    boolean register(Admin admin);
    Admin getById(Integer id);
    List<Admin> getAllAdmins();
    boolean updateAdmin(Admin admin);
    boolean deleteAdmin(Integer id);
}