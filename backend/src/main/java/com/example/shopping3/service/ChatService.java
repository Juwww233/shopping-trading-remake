package com.example.shopping3.service;

import com.example.shopping3.entity.ChatMessage;
import com.example.shopping3.entity.ChatSession;
import java.util.List;

public interface ChatService {
    ChatSession createSession(Long buyerId, Long sellerId);
    ChatMessage sendMessage(Long sessionId, Long senderId, String content);
    List<ChatMessage> getMessages(Long sessionId); // 返回类型修正为 List
}