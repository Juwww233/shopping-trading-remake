package com.example.shopping3.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatSession {
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
}