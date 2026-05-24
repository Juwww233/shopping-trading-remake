package com.example.shopping3.controller;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Notice;
import com.example.shopping3.service.NoticeService;
import com.example.shopping3.service.impl.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private AdminServiceImpl adminService;

    @NoAuth
    @GetMapping("/list")
    public Result<List<Notice>> getAllNotices() {
        try {
            List<Notice> notices = noticeService.getAllNotices();
            return Result.success(notices);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取通知列表失败：" + e.getMessage());
        }
    }

    @NoAuth
    @GetMapping("/{id}")
    public Result<Notice> getNoticeById(@PathVariable Integer id) {
        try {
            Notice notice = noticeService.getById(id);
            if (notice == null) {
                return Result.error("通知不存在");
            }
            return Result.success(notice);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取通知失败：" + e.getMessage());
        }
    }

    private boolean checkAdmin(String sessionId) {
        return sessionId != null && adminService.getAdminBySession(sessionId) != null;
    }

    @PostMapping
    public Result<Notice> addNotice(@RequestBody Notice notice,
                                     @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            Notice saved = noticeService.addNotice(notice);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增通知失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteNotice(@PathVariable Integer id,
                                        @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            noticeService.deleteNotice(id);
            return Result.success("删除通知成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除通知失败：" + e.getMessage());
        }
    }
}