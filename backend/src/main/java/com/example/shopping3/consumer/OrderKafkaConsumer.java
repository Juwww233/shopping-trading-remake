package com.example.shopping3.consumer;

import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.entity.Order;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.util.StompOrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {
    @Autowired
    private OrderService orderService;

    @Autowired
    private StompOrderUtil stompOrderUtil;

    @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "shopping_order_group")
    public void consumeOrderMessage(Order order) {
        // 处理订单（落库），handleOrderMessage方法中已经包含了消息推送
        orderService.handleOrderMessage(order);
    }
}