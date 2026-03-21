package com.example.shopping3.mapper;

import com.example.shopping3.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insertOrder(Order order);
    Order selectByOrderNo(String orderNo);
    List<Order> selectByUserId(@Param("userId") Integer userId);
}