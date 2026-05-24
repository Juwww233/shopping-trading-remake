package com.example.shopping3.controller;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Comment;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.CommentService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private SessionUtil sessionUtil;

    @NoAuth
    @GetMapping("/goods/{goodsId}")
    public Result<List<Comment>> getComments(@PathVariable Integer goodsId) {
        try {
            List<Comment> comments = commentService.getCommentsByGoodsId(goodsId);
            return Result.success(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Comment> addComment(@RequestBody Comment comment,
                                       @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        comment.setUserId(user.getId());
        try {
            Comment saved = commentService.addComment(comment);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发表评论失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Integer id,
                                         @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            commentService.deleteComment(id, user.getId());
            return Result.success("删除评论成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除评论失败：" + e.getMessage());
        }
    }
}