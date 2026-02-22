package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.CoachingPost;
import com.starfinder.entity.User;
import com.starfinder.mapper.CoachingPostMapper;
import com.starfinder.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coaching")
public class CoachingController {

    @Autowired
    private CoachingPostMapper coachingPostMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<CoachingPost>> list(@RequestParam(required = false) String type) {
        List<CoachingPost> posts;
        if (type != null && !type.isEmpty()) {
            posts = coachingPostMapper.findByType(type);
        } else {
            posts = coachingPostMapper.findAll();
        }
        return Result.success(posts);
    }

    @PostMapping
    public Result<CoachingPost> create(@RequestBody Map<String, Object> body) {
        Object userIdObj = body.get("userId");
        if (userIdObj == null) return Result.BadRequest("需要登录");

        String title = (String) body.get("title");
        String description = (String) body.get("description");
        
        if (title == null || title.trim().isEmpty()) return Result.BadRequest("标题不能为空");
        if (description == null || description.trim().isEmpty()) return Result.BadRequest("描述不能为空");
        if (description.length() > 3000) return Result.BadRequest("描述不能超过3000字");

        Long userId = ((Number) userIdObj).longValue();
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        CoachingPost post = new CoachingPost();
        post.setUserId(userId);
        post.setTitle(title.trim());
        post.setDescription(description.trim());
        post.setAuthorTag(user.getBattleTag() != null ? user.getBattleTag() : user.getEmail());

        String race = (String) body.get("race");
        if (race != null) post.setRace(race);

        Object mmrObj = body.get("mmr");
        if (mmrObj instanceof Number) post.setMmr(((Number) mmrObj).intValue());

        String priceInfo = (String) body.get("priceInfo");
        if (priceInfo != null && priceInfo.length() <= 100) post.setPriceInfo(priceInfo.trim());

        String contact = (String) body.get("contact");
        if (contact != null && contact.length() <= 200) post.setContact(contact.trim());

        String postType = (String) body.get("postType");
        post.setPostType("find".equals(postType) ? "find" : "coach");

        coachingPostMapper.insert(post);
        return Result.success(post);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");
        coachingPostMapper.deleteById(id);
        return Result.success("已删除");
    }
}
