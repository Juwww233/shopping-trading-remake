package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.PaymentService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SessionUtil sessionUtil;

    @PostMapping("/pay")
    public Result<Map<String, Object>> pay(@RequestParam String orderNo,
                                            @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            Map<String, Object> result = paymentService.pay(orderNo);
            if ((Integer) result.get("code") == 200) {
                return Result.success(result);
            }
            return Result.error(result.get("msg").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("支付失败：" + e.getMessage());
        }
    }
}