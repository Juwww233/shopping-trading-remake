package com.example.shopping3.mapper;

import com.example.shopping3.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeMapper {
    List<Notice> selectAll();
    Notice selectById(Integer id);
    int insert(Notice notice);
    int delete(Integer id);
}