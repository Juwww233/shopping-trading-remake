package com.example.shopping3.entity;

import lombok.Data;

@Data
public class Comment {
    private Integer id;
    private Integer userId;
    private Integer goodsId;
    private String content;
    private String time;
}