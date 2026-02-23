package com.starfinder.service.impl;

import com.starfinder.dto.Result;
import com.starfinder.entity.Stream;
import com.starfinder.entity.User;
import com.starfinder.mapper.StreamMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.service.SC2PulseService;
import com.starfinder.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StreamServiceImpl implements StreamService {

    @Autowired
    private StreamMapper streamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SC2PulseService sc2PulseService;

    @Override
    public Result<Stream> addStream(Stream stream) {
        if (stream.getStreamUrl() == null || stream.getStreamUrl().isEmpty()) {
            return Result.BadRequest("直播链接不能为空");
        }

        // Try to fetch highest MMR across all BattleTags provided
        String[] tagsToCheck = {
            stream.getBattleTag(),
            stream.getBattleTagCN(),
            stream.getBattleTagUS(),
            stream.getBattleTagEU(),
            stream.getBattleTagKR()
        };
        int highestMmr = 0;
        for (String tag : tagsToCheck) {
            if (tag == null || tag.trim().isEmpty()) continue;
            try {
                Long characterId = sc2PulseService.findCharacterId(tag.trim());
                if (characterId != null) {
                    Integer mmr = sc2PulseService.getMMR(characterId, "LOTV_1V1");
                    if (mmr != null && mmr > highestMmr) {
                        highestMmr = mmr;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        if (highestMmr > 0) {
            stream.setMmr(highestMmr);
        }

        streamMapper.insert(stream);
        return Result.success(stream);
    }

    @Override
    public Result<List<Stream>> getAllStreams() {
        return Result.success(streamMapper.findAll());
    }

    @Override
    public Result<Void> deleteStream(Long id, Long adminId) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) {
            return Result.BadRequest("需要登录");
        }
        if (adminId != null && !adminId.equals(principal.userId())) {
            return Result.BadRequest("无权限删除直播");
        }
        if (!principal.isAdmin()) {
            return Result.BadRequest("无权限删除直播");
        }
        streamMapper.deleteById(id);
        return Result.success();
    }
}
