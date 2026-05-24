package com.example.shopping3.controller;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.common.Result;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.UserService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionUtil sessionUtil;

    // 1. 登录 (明文比对)
    @NoAuth
    @PostMapping("/login")
    //手动取请求头 → 手动查 Redis → 手动判断
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            String sessionId = UUID.randomUUID().toString();
            sessionUtil.setSession(sessionId, user);

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            data.put("avatar", user.getAvatar());
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }

    // 2. 注册 (明文存储)
    @NoAuth
    @PostMapping("/register")
    public Result<String> register(@RequestBody User registerUser) {
        if (userService.register(registerUser)) {
            return Result.success("注册成功");
        }
        return Result.error("用户名已存在");
    }

    // 3. 获取用户详情
    @GetMapping("/info/{id}")
    public Result<User> getUserInfo(@PathVariable Integer id,
                                    @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId == null || sessionUtil.getSession(sessionId) == null) {
            return Result.error("未登录或会话过期");
        }
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 脱敏，不返回密码
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    // 4. 更新个人资料
    @PutMapping("/update")
    public Result<String> updateUserInfo(@RequestBody User updateUser,
                                         @RequestHeader("X-Session-Id") String sessionId) {
        User currentUser = (User) sessionUtil.getSession(sessionId);
        if (currentUser == null || !currentUser.getId().equals(updateUser.getId())) {
            return Result.error("未登录或无权操作");
        }

        User userToUpdate = new User();
        userToUpdate.setId(updateUser.getId());
        userToUpdate.setName(updateUser.getName());
        userToUpdate.setPhone(updateUser.getPhone());
        // 注意：这里不允许通过此接口修改密码

        if (userService.updateById(userToUpdate)) {
            // 同步更新 Redis Session
            currentUser.setName(updateUser.getName());
            currentUser.setPhone(updateUser.getPhone());
            sessionUtil.setSession(sessionId, currentUser);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    // 5. 上传头像
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                                    @RequestParam("userId") Integer userId,
                                                    @RequestHeader("X-Session-Id") String sessionId) {
        User currentUser = (User) sessionUtil.getSession(sessionId);
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            return Result.error("未登录或无权操作");
        }

        if (file.isEmpty()) return Result.error("文件为空");

        try {
            // 建议将路径改为绝对路径或配置在 application.yml 中，避免 IDE 运行目录问题
            // 这里暂时保持你的相对路径逻辑，但要注意 IDEA 的工作目录
            String uploadDir = "src/main/resources/static/uploads/avatars/";

            // 【优化】如果是在打包后的 jar 运行，src/main/resources 是只读的。
            // 测试阶段建议直接写到项目根目录下的 uploads 文件夹，或者配置外部路径
            // 这里为了让你能跑通，尝试创建一个外部路径 (项目根目录/uploads)
            // 如果必须用 resources，请确保 IDEA 允许写入 (通常不允许)
            // 临时方案：写到当前工作目录
            Path path = Paths.get("uploads/avatars/");
            if (!Files.exists(path)) Files.createDirectories(path);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("非法文件名");
            }
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + ext;

            file.transferTo(path.resolve(fileName));

            // 数据库访问路径 (映射到 static 或直接访问)
            // 假设你配置了静态资源映射 /uploads/** -> file:./uploads/
            String avatarUrl = "/uploads/avatars/" + fileName;

            User u = new User();
            u.setId(userId);
            u.setAvatar(avatarUrl);
            userService.updateById(u);

            // 更新 Session
            currentUser.setAvatar(avatarUrl);
            sessionUtil.setSession(sessionId, currentUser);

            Map<String, String> data = new HashMap<>();
            data.put("avatar", avatarUrl);
            return Result.success(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    // 6. 修改密码 (【已修改】改为明文逻辑)
    @PutMapping("/changePassword")
    public Result<String> changePassword(@RequestBody Map<String, String> params,
                                         @RequestHeader("X-Session-Id") String sessionId) {
        User currentUser = (User) sessionUtil.getSession(sessionId);
        if (currentUser == null) return Result.error("未登录");

        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");

        if (oldPwd == null || newPwd == null) {
            return Result.error("参数缺失");
        }

        // 【修改点】直接明文比对原密码
        // 注意：此时数据库中存的也是明文
        if (!currentUser.getPassword().equals(oldPwd)) {
            return Result.error("原密码错误");
        }

        // 【修改点】直接存新密码明文，不再加密
        User u = new User();
        u.setId(currentUser.getId());
        u.setPassword(newPwd);

        if (userService.updateById(u)) {
            // 密码修改后，为了安全起见（即使是明文），通常也建议让用户重新登录
            // 或者更新 Session 中的密码字段，防止当前会话状态不一致
            currentUser.setPassword(newPwd);
            sessionUtil.setSession(sessionId, currentUser);

            return Result.success("密码修改成功");
        }
        return Result.error("修改失败");
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("X-Session-Id") String sessionId) {
        sessionUtil.removeSession(sessionId);
        return Result.success("登出成功");
    }
}