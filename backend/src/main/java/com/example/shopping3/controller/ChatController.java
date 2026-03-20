package com.example.shopping3.controller;

import com.example.shopping3.entity.ChatMessage;
import com.example.shopping3.entity.ChatSession;
import com.example.shopping3.service.ChatService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionUtil sessionUtil;

    @PostMapping("/session")
    public ResponseEntity<ChatSession> createSession(
            @RequestParam Long sellerId,
            @RequestHeader("X-Session-Id") String sessionId
    ) {
        Object sessionUser = sessionUtil.getSession(sessionId);
        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 修正：添加 .longValue() 进行类型转换
        Long buyerId = ((com.example.shopping3.entity.User) sessionUser).getId().longValue();

        ChatSession session = chatService.createSession(buyerId, sellerId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam Long sessionId,
            @RequestParam String content,
            @RequestHeader("X-Session-Id") String sessionIdHeader
    ) {
        Object sessionUser = sessionUtil.getSession(sessionIdHeader);
        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 修正：添加 .longValue() 进行类型转换
        Long senderId = ((com.example.shopping3.entity.User) sessionUser).getId().longValue();

        ChatMessage message = chatService.sendMessage(sessionId, senderId, content);
        return ResponseEntity.ok(message);
    }
}