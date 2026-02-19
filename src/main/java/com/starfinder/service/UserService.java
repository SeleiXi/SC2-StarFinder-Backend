package com.starfinder.service;

import com.starfinder.dto.ProfileUpdateDTO;
import com.starfinder.dto.RegisterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.User;

import java.util.List;

public interface UserService {
    Result<User> createUser(RegisterDTO registerDTO);
    Result<User> verifyUser(RegisterDTO registerDTO);
    User getUserById(Long id);
    Result<User> updateProfile(Long userId, ProfileUpdateDTO dto);
    List<User> findMatches(int mmr, int range, String opponentRace);
}