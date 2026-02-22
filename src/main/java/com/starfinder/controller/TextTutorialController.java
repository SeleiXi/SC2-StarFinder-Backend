package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.TextTutorial;
import com.starfinder.entity.User;
import com.starfinder.mapper.TextTutorialMapper;
import com.starfinder.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/text-tutorial")
public class TextTutorialController {

    @Autowired
    private TextTutorialMapper textTutorialMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<TextTutorial>> list(@RequestParam(required = false) String category) {
        List<TextTutorial> tutorials;
        if (category != null && !category.isEmpty()) {
            tutorials = textTutorialMapper.findByCategory(category);
        } else {
            tutorials = textTutorialMapper.findAll();
        }
        return Result.success(tutorials);
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(textTutorialMapper.findDistinctCategories());
    }

    @PostMapping
    public Result<TextTutorial> create(@RequestBody Map<String, Object> body) {
        Object userIdObj = body.get("userId");
        if (userIdObj == null) return Result.BadRequest("需要登录");

        String title = (String) body.get("title");
        String content = (String) body.get("content");
        
        if (title == null || title.trim().isEmpty()) return Result.BadRequest("标题不能为空");
        if (content == null || content.trim().isEmpty()) return Result.BadRequest("内容不能为空");
        if (content.length() > 50000) return Result.BadRequest("内容不能超过50000字");

        Long userId = ((Number) userIdObj).longValue();
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        TextTutorial tutorial = new TextTutorial();
        tutorial.setUserId(userId);
        tutorial.setTitle(title.trim());
        tutorial.setContent(content.trim());
        tutorial.setAuthorTag(user.getBattleTag() != null ? user.getBattleTag() : user.getEmail());

        String category = (String) body.get("category");
        if (category != null && !category.trim().isEmpty() && category.length() <= 50) {
            tutorial.setCategory(category.trim());
        }

        textTutorialMapper.insert(tutorial);
        return Result.success(tutorial);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");
        if (!"admin".equals(user.getRole())) return Result.BadRequest("无权限");
        textTutorialMapper.deleteById(id);
        return Result.success("已删除");
    }
}
