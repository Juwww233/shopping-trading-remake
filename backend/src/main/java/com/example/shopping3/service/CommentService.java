package com.example.shopping3.service;

import com.example.shopping3.entity.Comment;
import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);
    List<Comment> getCommentsByGoodsId(Integer goodsId);
    void deleteComment(Integer id, Integer userId);
}