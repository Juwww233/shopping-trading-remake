package com.example.shopping3.mapper;

import com.example.shopping3.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    List<Comment> selectByGoodsId(Integer goodsId);
    int delete(Integer id);
}