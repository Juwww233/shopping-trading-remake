package com.example.shopping3.service.impl;

import com.example.shopping3.service.OrderService;
import com.example.shopping3.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private OrderService orderService;

    @Override
    public Map<String, Object> pay(String orderNo) {
        return orderService.payOrder(orderNo);
    }
}