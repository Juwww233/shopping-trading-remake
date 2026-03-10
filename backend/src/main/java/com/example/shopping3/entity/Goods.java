package com.example.shopping3.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Goods {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String content;
    private String address;
    private String img;
    private Date date;
    private String status; // 审核通过/未审核
    private String category; // 电子产品/美食/服装/生活/二手物品
    private Integer userId;
    private String saleStatus; // 已上架/未上架
    private Integer readCount;
}