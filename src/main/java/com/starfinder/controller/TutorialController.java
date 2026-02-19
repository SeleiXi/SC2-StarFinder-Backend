package com.starfinder.controller;

import com.starfinder.dto.TutorialDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Tutorial;
import com.starfinder.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {

    @Autowired
    private TutorialService tutorialService;

    @PostMapping
    public Result<Tutorial> createTutorial(@RequestBody TutorialDTO dto) {
        return tutorialService.createTutorial(dto);
    }

    @GetMapping("/list")
    public List<Tutorial> getTutorials(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return tutorialService.getTutorialsByCategory(category);
        }
        return tutorialService.getAllTutorials();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTutorial(@PathVariable Long id) {
        return tutorialService.deleteTutorial(id);
    }
}
