package com.example.shopping3.entity;

import lombok.Data; // Lombok注解，自动生成get/set/toString等方法

// 对应数据库address表
@Data
public class Address {
    private Integer id;          // 地址ID
    private Integer userId;      // 用户ID（数据库字段：user_id）
    private String name;         // 收货人姓名
    private String phone;        // 手机号
    private String addressDetail; // 详细地址（数据库字段：address_detail）
    private Integer isDefault;   // 是否默认地址（数据库字段：is_default）
}