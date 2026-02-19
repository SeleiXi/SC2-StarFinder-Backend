package com.starfinder.service.impl;

import com.starfinder.dto.TutorialDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Tutorial;
import com.starfinder.mapper.TutorialMapper;
import com.starfinder.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TutorialServiceImpl implements TutorialService {

    @Autowired
    private TutorialMapper tutorialMapper;

    @Override
    public Result<Tutorial> createTutorial(TutorialDTO dto) {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(dto.getTitle());
        tutorial.setUrl(dto.getUrl());
        tutorial.setCategory(dto.getCategory());
        tutorial.setDescription(dto.getDescription());
        tutorial.setAuthor(dto.getAuthor());
        tutorialMapper.insert(tutorial);
        return Result.success(tutorial);
    }

    @Override
    public List<Tutorial> getAllTutorials() {
        return tutorialMapper.findAll();
    }

    @Override
    public List<Tutorial> getTutorialsByCategory(String category) {
        return tutorialMapper.findByCategory(category);
    }

    @Override
    public Result<Void> deleteTutorial(Long id) {
        tutorialMapper.deleteById(id);
        return Result.success();
    }
}
