package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.User;
import com.example.shopping3.entity.Order;
import com.example.shopping3.service.GoodsService;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.service.impl.AdminServiceImpl;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SessionUtil sessionUtil;

    @Autowired
    private AdminServiceImpl adminService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(
            @RequestParam Integer goodsId,
            @RequestParam Integer buyCount,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String userName,
            @RequestHeader("X-Session-Id") String sessionId) {

        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录，请先登录");
        }

        Goods goods = goodsService.getGoodsDetail(goodsId);
        if (goods == null) {
            return Result.error("商品不存在");
        }

        Map<String, Object> result = orderService.createOrder(goods, user.getId(), buyCount,
                address, phone, userName);

        if ((Integer) result.get("code") == 200) {
            return Result.success(result);
        } else {
            return Result.error(result.get("msg").toString());
        }
    }

    @GetMapping("/query/{orderNo}")
    public Result<Map<String, Object>> queryOrder(@PathVariable String orderNo) {
        Order order = orderService.getOrderDetail(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        return Result.success(data);
    }

    // 【新增】获取当前用户订单列表
    @GetMapping("/list")
    public Result<List<Order>> getOrderList(@RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录，请先登录");
        }
        List<Order> orderList = orderService.getOrderListByUserId(user.getId());
        return Result.success(orderList);
    }

    // 支付订单
    @PostMapping("/{orderNo}/pay")
    public Result<Map<String, Object>> payOrder(@PathVariable String orderNo,
                                                 @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        Map<String, Object> result = orderService.payOrder(orderNo);
        if ((Integer) result.get("code") == 200) {
            return Result.success(result);
        }
        return Result.error(result.get("msg").toString());
    }

    // 取消订单
    @PostMapping("/{orderNo}/cancel")
    public Result<Map<String, Object>> cancelOrder(@PathVariable String orderNo,
                                                    @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        Map<String, Object> result = orderService.cancelOrder(orderNo, user.getId());
        if ((Integer) result.get("code") == 200) {
            return Result.success(result);
        }
        return Result.error(result.get("msg").toString());
    }

    // 卖家发货
    @PostMapping("/{orderNo}/ship")
    public Result<Map<String, Object>> shipOrder(@PathVariable String orderNo,
                                                  @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        Map<String, Object> result = orderService.shipOrder(orderNo);
        if ((Integer) result.get("code") == 200) {
            return Result.success(result);
        }
        return Result.error(result.get("msg").toString());
    }

    // 确认收货
    @PostMapping("/{orderNo}/receive")
    public Result<Map<String, Object>> confirmReceive(@PathVariable String orderNo,
                                                       @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        Map<String, Object> result = orderService.confirmReceive(orderNo);
        if ((Integer) result.get("code") == 200) {
            return Result.success(result);
        }
        return Result.error(result.get("msg").toString());
    }
}