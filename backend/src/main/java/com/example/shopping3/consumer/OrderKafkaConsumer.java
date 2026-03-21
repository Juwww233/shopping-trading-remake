package com.example.shopping3.consumer;

import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.entity.Order;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.util.StompOrderUtil; // 导入新工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {
    @Autowired
    private OrderService orderService;

    // ✅ 注入新的 STOMP 工具类
    @Autowired
    private StompOrderUtil stompOrderUtil;

    // 监听订单主题
    @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "shopping_order_group")
    public void consumeOrderMessage(Order order) {
        // 1. 处理订单（落库）
        orderService.handleOrderMessage(order);
        // 2. ✅ 通过 STOMP 推送订单结果给前端（兼容聊天）
        stompOrderUtil.sendOrderResult(order.getOrderNo(), order.getStatus());
    }
}