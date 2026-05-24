package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.Order;
import com.example.shopping3.mapper.OrderMapper;
import com.example.shopping3.service.OrderService;
import com.example.shopping3.config.KafkaConfig;
import com.example.shopping3.exception.BusinessException;
import com.example.shopping3.util.StockRedisUtil;
import com.example.shopping3.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();
    static {
        VALID_TRANSITIONS.put("待支付", new HashSet<>(Arrays.asList("已支付", "已取消")));
        VALID_TRANSITIONS.put("已支付", new HashSet<>(Arrays.asList("已发货", "已取消")));
        VALID_TRANSITIONS.put("已发货", new HashSet<>(Arrays.asList("已收货")));
        VALID_TRANSITIONS.put("已收货", new HashSet<>(Arrays.asList("已完成")));
    }

    @Autowired
    private StockRedisUtil stockRedisUtil;

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    @Qualifier("kafkaCallbackExecutor")
    private Executor kafkaCallbackExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Goods goods, Integer userId, Integer buyCount,
                                           String address, String phone, String userName) {
        Map<String, Object> result = new HashMap<>();
        Integer goodsId = goods.getId();

        Integer stock = stockRedisUtil.getStock(goodsId);
        if (stock == null || stock < buyCount) {
            result.put("code", 500);
            result.put("msg", "库存不足，当前剩余：" + (stock != null ? stock : 0));
            return result;
        }

        boolean decreaseSuccess = stockRedisUtil.decreaseStock(goodsId, buyCount);
        if (!decreaseSuccess) {
            result.put("code", 500);
            result.put("msg", "库存扣减失败，请重试");
            return result;
        }

        try {
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
            order.setStatus("待支付");
            order.setSaleId(goods.getUserId());
            order.setUserId(userId);
            order.setPayNo("");
            order.setPayTime("");
            order.setGoodsId(goodsId);

            orderMapper.insertOrder(order);

            result.put("code", 200);
            result.put("msg", "订单创建成功，请尽快支付");
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
    public Map<String, Object> payOrder(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            result.put("code", 500);
            result.put("msg", "订单不存在");
            return result;
        }
        if (!"待支付".equals(order.getStatus())) {
            result.put("code", 500);
            result.put("msg", "订单状态异常，当前状态：" + order.getStatus());
            return result;
        }

        String payNo = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String payTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        orderMapper.updatePayInfo(orderNo, payNo, payTime, "已支付");

        CompletableFuture<SendResult<String, Order>> future = kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, orderNo, order);
        future.whenCompleteAsync((sendResult, ex) -> {
            if (ex != null) {
                System.err.println("Kafka 消息发送失败，订单号: " + orderNo + ", 错误: " + ex.getMessage());
            } else {
                System.out.println("Kafka 消息发送成功，订单号: " + orderNo);
            }
        }, kafkaCallbackExecutor);

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("orderNo", orderNo);
        pushData.put("status", "已支付");
        pushData.put("msg", "支付成功，等待发货");
        messagingTemplate.convertAndSend("/order/" + orderNo, pushData);

        result.put("code", 200);
        result.put("msg", "支付成功");
        result.put("orderNo", orderNo);
        return result;
    }

    @Override
    public Map<String, Object> cancelOrder(String orderNo, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            result.put("code", 500);
            result.put("msg", "订单不存在");
            return result;
        }
        if (!"待支付".equals(order.getStatus()) && !"已支付".equals(order.getStatus())) {
            result.put("code", 500);
            result.put("msg", "当前状态不可取消：" + order.getStatus());
            return result;
        }

        orderMapper.updateStatus(orderNo, "已取消");

        if (order.getGoodsId() != null && order.getCount() != null) {
            stockRedisUtil.increaseStock(order.getGoodsId(), order.getCount());
        }

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("orderNo", orderNo);
        pushData.put("status", "已取消");
        pushData.put("msg", "订单已取消");
        messagingTemplate.convertAndSend("/order/" + orderNo, pushData);

        result.put("code", 200);
        result.put("msg", "订单已取消");
        return result;
    }

    @Override
    public Map<String, Object> shipOrder(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            result.put("code", 500);
            result.put("msg", "订单不存在");
            return result;
        }
        if (!"已支付".equals(order.getStatus())) {
            result.put("code", 500);
            result.put("msg", "当前状态不可发货：" + order.getStatus());
            return result;
        }

        orderMapper.updateStatus(orderNo, "已发货");

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("orderNo", orderNo);
        pushData.put("status", "已发货");
        pushData.put("msg", "卖家已发货");
        messagingTemplate.convertAndSend("/order/" + orderNo, pushData);

        result.put("code", 200);
        result.put("msg", "发货成功");
        return result;
    }

    @Override
    public Map<String, Object> confirmReceive(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            result.put("code", 500);
            result.put("msg", "订单不存在");
            return result;
        }
        if (!"已发货".equals(order.getStatus())) {
            result.put("code", 500);
            result.put("msg", "当前状态不可确认收货：" + order.getStatus());
            return result;
        }

        orderMapper.updateStatus(orderNo, "已完成");

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("orderNo", orderNo);
        pushData.put("status", "已完成");
        pushData.put("msg", "订单已完成");
        messagingTemplate.convertAndSend("/order/" + orderNo, pushData);

        result.put("code", 200);
        result.put("msg", "确认收货成功");
        return result;
    }

    @Async("orderTaskExecutor")
    @Override
    public void handleOrderMessage(Order order) {
        try {
            int existingCount = orderMapper.countByOrderNo(order.getOrderNo());

            if (existingCount > 0) {
                orderMapper.updateStatus(order.getOrderNo(), "已发货");
            } else {
                order.setStatus("已发货");
                orderMapper.insertOrder(order);
            }

            if (order.getGoodsId() != null && order.getCount() != null) {
                goodsMapper.updateStock(order.getGoodsId(), order.getCount());
                System.out.println("已更新数据库库存：商品ID=" + order.getGoodsId() + "，扣减数量=" + order.getCount());
                try {
                    redisTemplate.delete("goods:detail:" + order.getGoodsId());
                } catch (Exception e) {
                    System.err.println("删除商品缓存失败: " + e.getMessage());
                }
            }

            Map<String, Object> pushData = new HashMap<>();
            pushData.put("orderNo", order.getOrderNo());
            pushData.put("status", "已发货");
            pushData.put("msg", "卖家已发货");

            messagingTemplate.convertAndSend("/order/" + order.getOrderNo(), pushData);
            System.out.println("已推送订单结果：" + order.getOrderNo());

        } catch (Exception e) {
            e.printStackTrace();
            if (order.getGoodsId() != null) {
                stockRedisUtil.increaseStock(order.getGoodsId(), order.getCount());
                try {
                    redisTemplate.delete("goods:detail:" + order.getGoodsId());
                } catch (Exception deleteEx) {
                    System.err.println("删除商品缓存失败: " + deleteEx.getMessage());
                }
            }

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