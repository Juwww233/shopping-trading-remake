package com.example.shopping3.service;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(Goods goods, Integer userId, Integer buyCount,
                                    String address, String phone, String userName);
    void handleOrderMessage(Order order);
    Order getOrderDetail(String orderNo);
    List<Order> getOrderListByUserId(Integer userId);

    Map<String, Object> payOrder(String orderNo);
    Map<String, Object> cancelOrder(String orderNo, Integer userId);
    Map<String, Object> shipOrder(String orderNo);
    Map<String, Object> confirmReceive(String orderNo);
}