package com.example.shopping3.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Order {
    private Integer id;
    private String goodsName;
    private String goodsImg;
    private BigDecimal total;
    private String time;
    private String payNo;
    private String payTime;
    private String address;
    private String phone;
    private String userName;
    private String status;
    private Integer saleId;
    private BigDecimal goodsPrice;
    private Integer count;
    private String orderNo;
    private Integer userId;
    //商品ID，用于 Kafka 消费者回滚库存
    private Integer goodsId;
}