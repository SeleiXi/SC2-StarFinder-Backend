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
import com.starfinder.service.EmailService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SC2PulseService sc2PulseService;

    @Autowired
    private EmailService emailService;

    @Override
    public Result<User> createUser(RegisterDTO registerDTO) {
        // Enforce email verification for registration
        if (registerDTO.getEmail() == null || registerDTO.getEmail().isEmpty()) {
            return Result.BadRequest("电子邮箱不能为空");
        }
        if (registerDTO.getEmailCode() == null || registerDTO.getEmailCode().isEmpty()) {
            return Result.BadRequest("验证码不能为空");
        }
        
        // Password strength check
        if (registerDTO.getPassword() == null || registerDTO.getPassword().length() < 8) {
            return Result.BadRequest("密码太弱：需至少8位");
        }

        // Check verification code
        if (!emailService.verifyCode(registerDTO.getEmail(), registerDTO.getEmailCode())) {
            return Result.BadRequest("验证码无效或已过期");
        }

        // Check if email already exists
        User existingEmail = userMapper.findByEmail(registerDTO.getEmail());
        if (existingEmail != null) {
            return Result.BadRequest("该邮箱已注册");
        }

        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        
        // Use email as name if not provided
        String name = registerDTO.getName();
        if (name == null || name.trim().isEmpty()) {
            name = registerDTO.getEmail().split("@")[0];
        }
        user.setName(name.trim());
        
        user.setBattleTag(registerDTO.getBattleTag());
        user.setQq(registerDTO.getQq());
        user.setRegion(registerDTO.getRegion() != null ? registerDTO.getRegion() : "US");
        user.setRace("");
        user.setMmr(0);
        user.setRole("user");

        // Try to fetch MMR from SC2 Pulse if battleTag is provided
        if (registerDTO.getBattleTag() != null && !registerDTO.getBattleTag().isEmpty()) {
            try {
                Long characterId = sc2PulseService.findCharacterId(registerDTO.getBattleTag());
                if (characterId != null) {
                    user.setCharacterId(characterId);
                    user.setMmr(sc2PulseService.getMMR(characterId, "LOTV_1V1"));
                    user.setMmr2v2(sc2PulseService.getMMR(characterId, "LOTV_2V2"));
                    user.setMmr3v3(sc2PulseService.getMMR(characterId, "LOTV_3V3"));
                    user.setMmr4v4(sc2PulseService.getMMR(characterId, "LOTV_4V4"));
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
        String identifier = loginDTO.getEmail(); // Use email as identifier
        String password = loginDTO.getPassword();
        
        // Try to find by email
        User user = userMapper.findByEmail(identifier);

        // If not found, try to find by name
        if (user == null) {
            user = userMapper.findByName(identifier);
        }

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
    public Result<User> verifyUserByCode(String email, String code) {
        if (!emailService.verifyCode(email, code)) {
            return Result.BadRequest("验证码错误或已过期");
        }
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return Result.BadRequest("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<String> resetPassword(String email, String code, String newPassword) {
        if (!emailService.verifyCode(email, code)) {
            return Result.BadRequest("验证码错误或已过期");
        }
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return Result.BadRequest("该邮箱未注册");
        }
        userMapper.updatePassword(email, newPassword);
        return Result.success("密码已重置");
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
        if (dto.getPassword() != null && !dto.getPassword().isEmpty())
            user.setPassword(dto.getPassword());

        if (dto.getBattleTagCN() != null)
            user.setBattleTagCN(dto.getBattleTagCN());
        if (dto.getBattleTagUS() != null)
            user.setBattleTagUS(dto.getBattleTagUS());
        if (dto.getBattleTagEU() != null)
            user.setBattleTagEU(dto.getBattleTagEU());
        if (dto.getBattleTagKR() != null)
            user.setBattleTagKR(dto.getBattleTagKR());

        // Always try to sync MMR from SC2 Pulse when BattleTag is updated or profile is saved
        syncUserMMR(user);

        userMapper.update(user);
        user.setPassword(null);
        return Result.success(user);
    }

    private void syncUserMMR(User user) {
        String[] tags = { 
            user.getBattleTag(), 
            user.getBattleTagCN(), 
            user.getBattleTagUS(), 
            user.getBattleTagEU(), 
            user.getBattleTagKR() 
        };

        for (String tag : tags) {
            if (tag != null && !tag.isEmpty()) {
                try {
                    Long characterId = sc2PulseService.findCharacterId(tag);
                    if (characterId != null) {
                        user.setCharacterId(characterId);
                        user.setMmr(sc2PulseService.getMMR(characterId, "LOTV_1V1"));
                        user.setMmr2v2(sc2PulseService.getMMR(characterId, "LOTV_2V2"));
                        user.setMmr3v3(sc2PulseService.getMMR(characterId, "LOTV_3V3"));
                        user.setMmr4v4(sc2PulseService.getMMR(characterId, "LOTV_4V4"));
                        
                        // If we successfully found a character and synced MMR, we can stop
                        // In the future, we might want to aggregate across regions
                        return;
                    }
                } catch (Exception e) {
                    // ignore sync error for this tag, try next
                }
            }
        }
    }

    @Override
    public List<User> findMatches(int mmr, int range, String opponentRace, String mode) {
        int minMmr = mmr - range;
        int maxMmr = mmr + range;
        List<User> matches;

        if ("2v2".equalsIgnoreCase(mode)) {
            matches = (opponentRace != null && !opponentRace.isEmpty())
                    ? userMapper.findByMmr2v2RangeAndRace(minMmr, maxMmr, opponentRace)
                    : userMapper.findByMmr2v2Range(minMmr, maxMmr);
        } else if ("3v3".equalsIgnoreCase(mode)) {
            matches = (opponentRace != null && !opponentRace.isEmpty())
                    ? userMapper.findByMmr3v3RangeAndRace(minMmr, maxMmr, opponentRace)
                    : userMapper.findByMmr3v3Range(minMmr, maxMmr);
        } else if ("4v4".equalsIgnoreCase(mode)) {
            matches = (opponentRace != null && !opponentRace.isEmpty())
                    ? userMapper.findByMmr4v4RangeAndRace(minMmr, maxMmr, opponentRace)
                    : userMapper.findByMmr4v4Range(minMmr, maxMmr);
        } else {
            // Default 1v1
            matches = (opponentRace != null && !opponentRace.isEmpty())
                    ? userMapper.findByMmrRangeAndRace(minMmr, maxMmr, opponentRace)
                    : userMapper.findByMmrRange(minMmr, maxMmr);
        }

        // Don't return passwords
        for (User u : matches) {
            u.setPassword(null);
        }
        return matches;
    }
}