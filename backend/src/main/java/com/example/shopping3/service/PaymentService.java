package com.example.shopping3.service;

import java.util.Map;

public interface PaymentService {
    Map<String, Object> pay(String orderNo);
}