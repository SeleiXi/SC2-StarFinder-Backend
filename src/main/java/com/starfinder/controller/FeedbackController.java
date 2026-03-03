package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.Feedback;
import com.starfinder.entity.User;
import com.starfinder.mapper.FeedbackMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public Result<Feedback> create(@RequestBody Map<String, Object> body) {
        Long userId = AuthContext.getUserId();
        if (userId == null) return Result.BadRequest("需要登录");

        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) return Result.BadRequest("反馈内容不能为空");
        if (content.length() > 5000) return Result.BadRequest("内容不能超过5000字");

        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(content.trim());
        feedback.setAuthorTag(user.getNickname() != null ? user.getNickname() : user.getEmail());
        feedback.setStatus("pending");

        feedbackMapper.insert(feedback);
        return Result.success(feedback);
    }

    @GetMapping("/my")
    public Result<List<Feedback>> myFeedbacks() {
        Long userId = AuthContext.getUserId();
        if (userId == null) return Result.BadRequest("需要登录");
        List<Feedback> all = feedbackMapper.findAll();
        List<Feedback> mine = all.stream().filter(f -> f.getUserId().equals(userId)).toList();
        return Result.success(mine);
    }
}
