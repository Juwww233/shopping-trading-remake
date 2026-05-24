package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Comment;
import com.example.shopping3.mapper.CommentMapper;
import com.example.shopping3.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Comment addComment(Comment comment) {
        comment.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public List<Comment> getCommentsByGoodsId(Integer goodsId) {
        return commentMapper.selectByGoodsId(goodsId);
    }

    @Override
    public void deleteComment(Integer id, Integer userId) {
        commentMapper.delete(id);
    }
}