package com.starfinder.service;

import com.starfinder.dto.TutorialDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Tutorial;

import java.util.List;

public interface TutorialService {
    Result<Tutorial> createTutorial(TutorialDTO dto);

    List<Tutorial> getAllTutorials();

    List<Tutorial> getTutorialsByCategory(String category);

    Result<Void> deleteTutorial(Long id);
}
