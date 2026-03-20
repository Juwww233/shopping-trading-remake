package com.example.shopping3.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}