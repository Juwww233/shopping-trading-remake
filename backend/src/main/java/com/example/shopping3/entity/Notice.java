package com.example.shopping3.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notice {
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime time;
    private String user;
}