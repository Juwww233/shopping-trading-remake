package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.GoodsService;
import com.example.shopping3.service.impl.AdminServiceImpl;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods/manage")
public class GoodsManageController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SessionUtil sessionUtil;

    @Autowired
    private AdminServiceImpl adminService;

    @PostMapping("/publish")
    public Result<Goods> publishGoods(@RequestBody Goods goods,
                                       @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        if (!"merchant".equals(user.getRole()) && !"seller".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            return Result.error("仅商家可发布商品");
        }
        try {
            goods.setUserId(user.getId());
            Goods saved = goodsService.publishGoods(goods);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布商品失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Goods> updateGoods(@PathVariable Integer id, @RequestBody Goods goods,
                                      @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            goods.setId(id);
            goods.setUserId(user.getId());
            Goods updated = goodsService.updateGoods(goods);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/sale-status")
    public Result<String> toggleSaleStatus(@PathVariable Integer id,
                                            @RequestParam String saleStatus,
                                            @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            goodsService.updateGoodsSaleStatus(id, saleStatus);
            return Result.success("操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    public Result<String> reviewGoods(@PathVariable Integer id,
                                       @RequestParam String status,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (adminService.getAdminBySession(sessionId) == null) {
            return Result.error("无权操作，仅管理员可审核");
        }
        try {
            goodsService.updateGoodsStatus(id, status);
            return Result.success("审核完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @GetMapping("/my-list")
    public Result<List<Goods>> getMyGoods(@RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            List<Goods> list = goodsService.getSellerGoods(user.getId());
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }
}