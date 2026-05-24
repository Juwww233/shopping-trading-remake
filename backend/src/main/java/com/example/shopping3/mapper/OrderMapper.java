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
    int countByOrderNo(@Param("orderNo") String orderNo);

    int updateStatus(@Param("orderNo") String orderNo, @Param("status") String status);
    int updatePayInfo(@Param("orderNo") String orderNo, @Param("payNo") String payNo,
                      @Param("payTime") String payTime, @Param("status") String status);
}