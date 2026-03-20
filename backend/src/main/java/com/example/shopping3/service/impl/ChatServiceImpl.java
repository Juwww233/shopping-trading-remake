package com.example.shopping3.service.impl;

import com.example.shopping3.entity.ChatMessage;
import com.example.shopping3.entity.ChatSession;
import com.example.shopping3.mapper.ChatMessageMapper;
import com.example.shopping3.mapper.ChatSessionMapper;
import com.example.shopping3.service.ChatService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SessionUtil sessionUtil;

    @Override
    @Transactional
    public ChatSession createSession(Long buyerId, Long sellerId) {
        ChatSession session = sessionMapper.selectByBuyerAndSeller(buyerId, sellerId);
        if (session == null) {
            session = new ChatSession();
            session.setBuyerId(buyerId);
            session.setSellerId(sellerId);
            sessionMapper.insertSession(session);
        }
        return session;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long sessionId, Long senderId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        messageMapper.insertMessage(message);

        // 修正：通过sessionId查询会话（之前错误地使用了未定义的session变量）
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        redisTemplate.convertAndSend(
                "chat:messages:" + session.getSellerId(),
                "{\"id\":" + message.getId() +
                        ",\"sessionId\":" + message.getSessionId() +
                        ",\"senderId\":" + message.getSenderId() +
                        ",\"content\":\"" + message.getContent() + "\"}"
        );

        return message;
    }

    @Override
    public List<ChatMessage> getMessages(Long sessionId) {

        return messageMapper.selectMessagesBySessionId(sessionId);
    }
}