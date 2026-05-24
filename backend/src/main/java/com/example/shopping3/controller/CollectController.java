package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Collect;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.CollectService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collect")
public class CollectController {

    @Autowired
    private CollectService collectService;

    @Autowired
    private SessionUtil sessionUtil;

    @PostMapping
    public Result<Collect> addCollect(@RequestParam Integer goodsId,
                                      @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            Collect collect = collectService.addCollect(user.getId(), goodsId);
            return Result.success(collect);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("收藏失败：" + e.getMessage());
        }
    }

    @DeleteMapping
    public Result<String> removeCollect(@RequestParam Integer goodsId,
                                         @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            collectService.removeCollect(user.getId(), goodsId);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消收藏失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getUserCollects(@RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            List<Map<String, Object>> list = collectService.getUserCollects(user.getId());
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/check")
    public Result<Boolean> checkCollected(@RequestParam Integer goodsId,
                                          @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            boolean collected = collectService.isCollected(user.getId(), goodsId);
            return Result.success(collected);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询收藏状态失败：" + e.getMessage());
        }
    }
}