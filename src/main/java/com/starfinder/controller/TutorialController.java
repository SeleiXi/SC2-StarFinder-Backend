package com.starfinder.controller;

import com.starfinder.dto.TutorialDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Tutorial;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.service.TutorialService;
import com.starfinder.mapper.TutorialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {

    @Autowired
    private TutorialService tutorialService;

    @Autowired
    private TutorialMapper tutorialMapper;

    @PostMapping
    public Result<Tutorial> createTutorial(@RequestBody TutorialDTO dto) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (!principal.isAdmin()) return Result.BadRequest("无权限");
        return tutorialService.createTutorial(dto);
    }

    @GetMapping("/list")
    public List<Tutorial> getTutorials(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return tutorialService.getTutorialsByCategory(category);
        }
        return tutorialService.getAllTutorials();
    }

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(tutorialMapper.findDistinctCategories());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTutorial(@PathVariable Long id) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (!principal.isAdmin()) return Result.BadRequest("无权限");
        return tutorialService.deleteTutorial(id);
    }
}
