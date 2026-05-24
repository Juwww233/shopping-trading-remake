package com.example.shopping3.controller;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Admin;
import com.example.shopping3.exception.BusinessException;
import com.example.shopping3.service.AdminService;
import com.example.shopping3.service.impl.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminServiceImpl adminServiceImpl;

    @NoAuth
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Admin loginAdmin) {
        try {
            Map<String, Object> data = adminService.login(loginAdmin.getUsername(), loginAdmin.getPassword());
            return Result.success(data);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @NoAuth
    @PostMapping("/register")
    public Result<String> register(@RequestBody Admin admin) {
        try {
            if (adminService.register(admin)) {
                return Result.success("注册成功");
            }
            return Result.error("用户名已存在");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    private boolean checkAdmin(String sessionId) {
        return sessionId != null && adminServiceImpl.getAdminBySession(sessionId) != null;
    }

    @GetMapping("/list")
    public Result<List<Admin>> getAllAdmins(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            List<Admin> admins = adminService.getAllAdmins();
            return Result.success(admins);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取管理员列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Admin> getAdminById(@PathVariable Integer id,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            return Result.success(admin);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取管理员信息失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> updateAdmin(@PathVariable Integer id, @RequestBody Admin admin,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            admin.setId(id);
            if (adminService.updateAdmin(admin)) {
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新管理员失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAdmin(@PathVariable Integer id,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            if (adminService.deleteAdmin(id)) {
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除管理员失败：" + e.getMessage());
        }
    }
}