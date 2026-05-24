package com.example.shopping3.consumer;

import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.entity.Order;
import com.example.shopping3.mapper.OrderMapper;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.util.StompOrderUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "shopping_order_group")
    public void consumeOrderMessage(ConsumerRecord<String, Order> record, Acknowledgment ack) {
        Order order = record.value();
        String orderNo = order.getOrderNo();

        try {
            // 幂等性检查：防止重复消费
            int count = orderMapper.countByOrderNo(orderNo);
            if (count > 0) {
                logger.warn("订单已存在，跳过重复消费，订单号: {}", orderNo);
                ack.acknowledge();
                return;
            }

            // 处理订单（落库）
            orderService.handleOrderMessage(order);

            // 手动提交 offset
            ack.acknowledge();
            logger.info("订单处理成功，offset 已提交，订单号: {}", orderNo);

        } catch (Exception e) {
            logger.error("订单处理失败，订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            // 不提交 offset，Kafka 会重新投递该消息
            throw e;
        }
    }
}