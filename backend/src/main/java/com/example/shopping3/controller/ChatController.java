package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.ChatMessage;
import com.example.shopping3.entity.ChatSession;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.ChatService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionUtil sessionUtil;

    @PostMapping("/session")
    public Result<ChatSession> createSession(
            @RequestBody CreateSessionRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        if (sessionId == null || sessionUtil.getSession(sessionId) == null) {
            return Result.error("未登录");
        }
        User currentUser = (User) sessionUtil.getSession(sessionId);
        ChatSession session = chatService.createSession(currentUser.getId().longValue(), request.getSellerId());
        return Result.success(session);
    }

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        if (sessionId == null || sessionUtil.getSession(sessionId) == null) {
            return Result.error("未登录");
        }
        User currentUser = (User) sessionUtil.getSession(sessionId);
        ChatMessage message = chatService.sendMessage(request.getSessionId(), currentUser.getId().longValue(), request.getContent());
        return Result.success(message);
    }

    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(@RequestParam Long sessionId) {
        List<ChatMessage> messages = chatService.getMessages(sessionId);
        return Result.success(messages);
    }

    public static class CreateSessionRequest {
        private Long sellerId;
        public Long getSellerId() { return sellerId; }
        public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    }

    public static class SendMessageRequest {
        private Long sessionId;
        private String content;
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}