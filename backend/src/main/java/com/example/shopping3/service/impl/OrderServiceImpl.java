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

    // 注入 RedisTemplate 用于删除商品详情缓存 (解决数据一致性问题)
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Goods goods, Integer userId, Integer buyCount,
                                           String address, String phone, String userName) {
        Map<String, Object> result = new HashMap<>();
        Integer goodsId = goods.getId();

        // 1. 查询 Redis 库存 (预扣减模式)
        // 注意：这里依赖启动时的预热，或者未来 addGoods 时的同步
        Integer stock = stockRedisUtil.getStock(goodsId);

        if (stock == null || stock < buyCount) {
            result.put("code", 500);
            result.put("msg", "库存不足，当前剩余：" + (stock != null ? stock : 0));
            return result;
        }

        // 2. 原子扣减 Redis 库存 (使用 Lua 脚本，需在 StockRedisUtil 中已实现)
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
            order.setGoodsId(goodsId); // 【关键】设置 goodsId，用于 Kafka 消费者回滚

            // 4. 发送 Kafka 消息 (异步落库)
            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, orderNo, order);

            // 5. 删除商品详情缓存
            // Key 必须与 GoodsServiceImpl 中定义的 CACHE_KEY_DETAIL 一致
            // 假设 GoodsServiceImpl 中定义的是 "goods:detail:" + id
            String cacheKey = "goods:detail:" + goodsId;
            redisTemplate.delete(cacheKey);

            // 如果有列表缓存，建议也删除，防止列表页显示库存不准
            redisTemplate.delete("goods:guessLike");
            redisTemplate.delete("goods:secondHand");

            // 6. 返回前端
            result.put("code", 200);
            result.put("msg", "付款成功，请等待订单处理");
            result.put("orderNo", orderNo);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            // 如果发送 Kafka 失败，需要回滚 Redis 库存
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

        } catch (Exception e) {
            e.printStackTrace();
            // 异常回滚：库存加回去
            if (order.getGoodsId() != null) {
                stockRedisUtil.increaseStock(order.getGoodsId(), order.getCount());
            }
            order.setStatus("失败");
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