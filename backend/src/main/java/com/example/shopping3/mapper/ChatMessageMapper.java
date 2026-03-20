package com.example.shopping3.mapper;

import com.example.shopping3.entity.ChatMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatMessageMapper {
    int insertMessage(ChatMessage message);

    List<ChatMessage> selectMessagesBySessionId(@Param("sessionId") Long sessionId);
}