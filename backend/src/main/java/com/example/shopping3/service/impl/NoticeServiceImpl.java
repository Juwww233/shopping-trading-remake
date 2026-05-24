package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Notice;
import com.example.shopping3.mapper.NoticeMapper;
import com.example.shopping3.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<Notice> getAllNotices() {
        return noticeMapper.selectAll();
    }

    @Override
    public Notice getById(Integer id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public Notice addNotice(Notice notice) {
        notice.setTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public void deleteNotice(Integer id) {
        noticeMapper.delete(id);
    }
}