package com.example.shopping3.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * STOMP 订单推送工具类（兼容聊天 WebSocket）
 */
@Component
public class StompOrderUtil {

    // Spring STOMP 核心推送模板
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定订单号推送处理结果
     * @param orderNo 订单编号
     * @param status 订单状态：已完成/失败
     */
    public void sendOrderResult(String orderNo, String status) {
        // 拼接订阅地址：/order/订单号（前端监听这个地址）
        String destination = "/order/" + orderNo;
        // 构造返回数据
        String result = "{\"orderNo\":\"" + orderNo + "\",\"status\":\"" + status + "\"}";
        // 推送消息给前端
        messagingTemplate.convertAndSend(destination, result);
    }
}