package com.example.shopping3.entity;

import lombok.Data;

@Data
public class Category {
    private Integer id;
    private String name;
    private Integer goodsId;
    private Integer userId;
}