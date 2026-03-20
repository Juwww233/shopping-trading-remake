package com.example.shopping3.mapper;

import com.example.shopping3.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatSessionMapper {
    ChatSession selectByBuyerAndSeller(
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId
    );

    int insertSession(ChatSession session);

    // 新增：通过ID查询会话（解决之前错误）
    ChatSession selectById(Long id);
}