package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.Order;
import com.example.shopping3.mapper.OrderMapper;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.util.StockRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;  // ✅ 添加导入
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ✅ 添加 SimpMessagingTemplate 用于推送消息
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Goods goods, Integer userId, Integer buyCount,
                                           String address, String phone, String userName) {
        Map<String, Object> result = new HashMap<>();
        Integer goodsId = goods.getId();

        // 1. 查询 Redis 库存
        Integer stock = stockRedisUtil.getStock(goodsId);

        if (stock == null || stock < buyCount) {
            result.put("code", 500);
            result.put("msg", "库存不足，当前剩余：" + (stock != null ? stock : 0));
            return result;
        }

        // 2. 原子扣减 Redis 库存
        boolean decreaseSuccess = stockRedisUtil.decreaseStock(goodsId, buyCount);
        if (!decreaseSuccess) {
            result.put("code", 500);
            result.put("msg", "库存扣减失败，请重试");
            return result;
        }

        try {
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
            order.setGoodsId(goodsId);

            // 4. 发送 Kafka 消息
            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, orderNo, order);

            // 5. 删除商品详情缓存
            String cacheKey = "goods:detail:" + goodsId;
            redisTemplate.delete(cacheKey);
            redisTemplate.delete("goods:guessLike");
            redisTemplate.delete("goods:secondHand");

            // 6. 返回前端
            result.put("code", 200);
            result.put("msg", "付款成功，请等待订单处理");
            result.put("orderNo", orderNo);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            stockRedisUtil.increaseStock(goodsId, buyCount);
            result.put("code", 500);
            result.put("msg", "系统繁忙，订单创建失败");
            return result;
        }
    }

    @Override
    public void handleOrderMessage(Order order) {
        try {
            // 模拟业务处理耗时
            Thread.sleep(1000);

            order.setStatus("已完成");
            orderMapper.insertOrder(order);

            // ✅ 推送消息到前端 - 关键代码！
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("orderNo", order.getOrderNo());
            pushData.put("status", order.getStatus());
            pushData.put("msg", "订单处理完成");

            // 发送到 /order/{orderNo} 主题
            messagingTemplate.convertAndSend("/order/" + order.getOrderNo(), pushData);
            System.out.println("已推送订单结果：" + order.getOrderNo());

        } catch (Exception e) {
            e.printStackTrace();
            // 异常回滚：库存加回去
            if (order.getGoodsId() != null) {
                stockRedisUtil.increaseStock(order.getGoodsId(), order.getCount());
            }
            order.setStatus("失败");

            // ✅ 推送失败消息到前端
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("orderNo", order.getOrderNo());
            pushData.put("status", "失败");
            pushData.put("msg", "订单处理失败，库存已回滚");

            messagingTemplate.convertAndSend("/order/" + order.getOrderNo(), pushData);
            System.out.println("已推送订单失败结果：" + order.getOrderNo());
        }
    }

    @Override
    public Order getOrderDetail(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public java.util.List<Order> getOrderListByUserId(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }
}