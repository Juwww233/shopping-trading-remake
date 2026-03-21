package com.example.shopping3.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StompOrderUtil {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 推送订单结果给前端
     */
    public void sendOrderResult(String orderNo, String status) {
        System.out.println(">>> [STOMP] 推送订单结果：" + orderNo + " -> " + status);

        OrderResult result = new OrderResult();
        result.setOrderNo(orderNo);
        result.setStatus(status);

        // ✅ 发送到 /order/{orderNo} 目的地
        messagingTemplate.convertAndSend("/order/" + orderNo, result);
    }

    // ✅ 内部类或单独创建 OrderResult 类
    public static class OrderResult {
        private String orderNo;
        private String status;

        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}