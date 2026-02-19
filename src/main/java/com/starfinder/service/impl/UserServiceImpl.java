package com.starfinder.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.starfinder.dto.ProfileUpdateDTO;
import com.starfinder.dto.RegisterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.User;
import com.starfinder.mapper.UserMapper;
import com.starfinder.service.SC2PulseService;
import com.starfinder.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SC2PulseService sc2PulseService;

    @Override
    public Result<User> createUser(RegisterDTO registerDTO) {
        // Check if phone number already exists
        User existing = userMapper.findByPhoneNumber(registerDTO.getPhoneNumber());
        if (existing != null) {
            return Result.BadRequest("该手机号已注册");
        }

        User user = new User();
        user.setPhoneNumber(registerDTO.getPhoneNumber());
        user.setPassword(registerDTO.getPassword());
        user.setName(registerDTO.getName() != null ? registerDTO.getName() : "未命名");
        user.setBattleTag(registerDTO.getBattleTag());
        user.setQq(registerDTO.getQq());
        user.setRegion(registerDTO.getRegion() != null ? registerDTO.getRegion() : "US");
        user.setRace("");
        user.setMmr(0);

        // Try to fetch MMR from SC2 Pulse if battleTag is provided
        if (registerDTO.getBattleTag() != null && !registerDTO.getBattleTag().isEmpty()) {
            try {
                Long characterId = sc2PulseService.findCharacterId(registerDTO.getBattleTag());
                if (characterId != null) {
                    user.setCharacterId(characterId);
                    Integer mmr = sc2PulseService.getMMR(characterId);
                    if (mmr != null) {
                        user.setMmr(mmr);
                    }
                }
            } catch (Exception e) {
                // SC2 Pulse lookup failed, continue with default MMR
            }
        }

        userMapper.insert(user);
        user.setPassword(null); // Don't return password
        return Result.success(user);
    }

    @Override
    public Result<User> verifyUser(RegisterDTO loginDTO) {
        String userPhoneNum = loginDTO.getPhoneNumber();
        String password = loginDTO.getPassword();
        User user = userMapper.findByPhoneNumber(userPhoneNum);
        if (user == null) {
            return Result.BadRequest("用户不存在");
        }
        if (password != null && password.equals(user.getPassword())) {
            user.setPassword(null); // Don't return password
            return Result.success(user);
        }
        return Result.BadRequest("密码错误");
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public Result<User> updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.BadRequest("用户不存在");
        }

        if (dto.getName() != null)
            user.setName(dto.getName());
        if (dto.getBattleTag() != null)
            user.setBattleTag(dto.getBattleTag());
        if (dto.getRace() != null)
            user.setRace(dto.getRace());
        if (dto.getQq() != null)
            user.setQq(dto.getQq());
        if (dto.getStreamUrl() != null)
            user.setStreamUrl(dto.getStreamUrl());
        if (dto.getSignature() != null)
            user.setSignature(dto.getSignature());
        if (dto.getRegion() != null)
            user.setRegion(dto.getRegion());

        // Re-fetch MMR if battleTag changed
        if (dto.getBattleTag() != null && !dto.getBattleTag().isEmpty()) {
            try {
                Long characterId = sc2PulseService.findCharacterId(dto.getBattleTag());
                if (characterId != null) {
                    user.setCharacterId(characterId);
                    Integer mmr = sc2PulseService.getMMR(characterId);
                    if (mmr != null) {
                        user.setMmr(mmr);
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        userMapper.update(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public List<User> findMatches(int mmr, int range, String opponentRace) {
        List<User> matches;
        if (opponentRace != null && !opponentRace.isEmpty()) {
            matches = userMapper.findByMmrRangeAndRace(mmr - range, mmr + range, opponentRace);
        } else {
            matches = userMapper.findByMmrRange(mmr - range, mmr + range);
        }
        // Don't return passwords
        for (User u : matches) {
            u.setPassword(null);
        }
        return matches;
    }
}