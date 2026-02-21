package com.starfinder.service.impl;

import com.starfinder.dto.Result;
import com.starfinder.entity.Stream;
import com.starfinder.entity.User;
import com.starfinder.mapper.StreamMapper;
import com.starfinder.mapper.UserMapper;
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

        // Try to fetch MMR if BattleTag is provided
        if (stream.getBattleTag() != null && !stream.getBattleTag().isEmpty()) {
            try {
                Long characterId = sc2PulseService.findCharacterId(stream.getBattleTag());
                if (characterId != null) {
                    Integer mmr = sc2PulseService.getMMR(characterId);
                    if (mmr != null) {
                        stream.setMmr(mmr);
                    }
                }
            } catch (Exception e) {
                // Ignore failure to fetch MMR
            }
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
        User admin = userMapper.findById(adminId);
        if (admin == null || !("admin".equals(admin.getRole()) || "super_admin".equals(admin.getRole()))) {
            return Result.BadRequest("无权限删除直播");
        }
        streamMapper.deleteById(id);
        return Result.success();
    }
}
