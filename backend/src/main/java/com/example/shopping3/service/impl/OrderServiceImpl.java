package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.Order;
import com.example.shopping3.mapper.OrderMapper;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.util.StockRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private StockRedisUtil stockRedisUtil;

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Map<String, Object> createOrder(Goods goods, Integer userId, Integer buyCount,
                                           String address, String phone, String userName) {
        Map<String, Object> result = new HashMap<>();
        Integer goodsId = goods.getId();

        // 1. 查询 Redis 库存
        Integer stock = stockRedisUtil.getStock(goodsId);
        if (stock < buyCount) {
            result.put("code", 500);
            result.put("msg", "库存不足，无法购买");
            return result;
        }

        // 2. 原子扣减 Redis 库存
        boolean decreaseSuccess = stockRedisUtil.decreaseStock(goodsId, buyCount);
        if (!decreaseSuccess) {
            result.put("code", 500);
            result.put("msg", "库存扣减失败，请重试");
            return result;
        }

        // 3. 构建订单对象
        Order order = new Order();
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        order.setOrderNo(orderNo);
        order.setGoodsName(goods.getName());
        order.setGoodsImg(goods.getImg());
        order.setGoodsPrice(goods.getPrice());
        order.setCount(buyCount);
        order.setTotal(goods.getPrice().multiply(new BigDecimal(buyCount)));
        order.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        order.setAddress(address);
        order.setPhone(phone);
        order.setUserName(userName);
        order.setStatus("待处理");
        order.setSaleId(goods.getUserId());
        order.setUserId(userId);
        order.setPayNo("");
        order.setPayTime("");
        // 【新增】设置 goodsId，用于 Kafka 消费者回滚
        order.setGoodsId(goodsId);

        // 4. 发送 Kafka 消息
        kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, orderNo, order);

        // 5. 返回前端
        result.put("code", 200);
        result.put("msg", "付款成功，请等待订单处理");
        result.put("orderNo", orderNo);
        return result;
    }

    @Override
    public void handleOrderMessage(Order order) {
        try {
            Thread.sleep(1000);
            order.setStatus("已完成");
            orderMapper.insertOrder(order);
        } catch (Exception e) {
            // 【修复】使用 order.getGoodsId() 而不是从 orderNo 解析
            if (order.getGoodsId() != null) {
                stockRedisUtil.increaseStock(order.getGoodsId(), order.getCount());
            }
            order.setStatus("失败");
            e.printStackTrace();
        }
    }

    @Override
    public Order getOrderDetail(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    // 【新增】获取用户订单列表
    @Override
    public java.util.List<Order> getOrderListByUserId(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }
}