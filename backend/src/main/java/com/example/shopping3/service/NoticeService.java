package com.example.shopping3.service;

import com.example.shopping3.entity.Notice;
import java.util.List;

public interface NoticeService {
    List<Notice> getAllNotices();
    Notice getById(Integer id);
    Notice addNotice(Notice notice);
    void deleteNotice(Integer id);
}