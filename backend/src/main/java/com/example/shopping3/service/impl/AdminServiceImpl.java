package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Admin;
import com.example.shopping3.exception.BusinessException;
import com.example.shopping3.mapper.AdminMapper;
import com.example.shopping3.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements AdminService {

    private static final String ADMIN_SESSION_PREFIX = "admin_session:";
    private static final long SESSION_TTL_MINUTES = 30;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null || !encoder.matches(password, admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String sessionId = UUID.randomUUID().toString();
        admin.setPassword(null);
        redisTemplate.opsForValue().set(ADMIN_SESSION_PREFIX + sessionId, admin, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("adminId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("role", admin.getRole());
        return data;
    }

    @Override
    public boolean register(Admin admin) {
        Admin existing = adminMapper.selectByUsername(admin.getUsername());
        if (existing != null) {
            return false;
        }
        admin.setPassword(encoder.encode(admin.getPassword()));
        return adminMapper.insert(admin) > 0;
    }

    @Override
    public Admin getById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    public List<Admin> getAllAdmins() {
        List<Admin> admins = adminMapper.selectAll();
        for (Admin a : admins) {
            a.setPassword(null);
        }
        return admins;
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        return adminMapper.update(admin) > 0;
    }

    @Override
    public boolean deleteAdmin(Integer id) {
        return adminMapper.delete(id) > 0;
    }

    public Admin getAdminBySession(String sessionId) {
        return (Admin) redisTemplate.opsForValue().get(ADMIN_SESSION_PREFIX + sessionId);
    }
}