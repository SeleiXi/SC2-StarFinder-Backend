package com.starfinder.service.impl;

import com.starfinder.dto.CheaterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Cheater;
import com.starfinder.mapper.CheaterMapper;
import com.starfinder.service.CheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheaterServiceImpl implements CheaterService {

    @Autowired
    private CheaterMapper cheaterMapper;

    @Override
    public Result<Cheater> reportCheater(CheaterDTO dto, Long reportedBy) {
        Cheater cheater = new Cheater();
        cheater.setBattleTag(dto.getBattleTag());
        cheater.setCheatType(dto.getCheatType());
        cheater.setDescription(dto.getDescription());
        cheater.setReportedBy(reportedBy);
        cheater.setStatus("approved"); // Auto-approve for now; change to "pending" for review flow
        cheater.setMmr(0);
        cheater.setRace("");
        cheaterMapper.insert(cheater);
        return Result.success(cheater);
    }

    @Override
    public List<Cheater> getApprovedCheaters() {
        return cheaterMapper.findAllApproved();
    }

    @Override
    public List<Cheater> searchCheaters(String battleTag) {
        return cheaterMapper.searchByBattleTag(battleTag);
    }

    @Override
    public Result<Cheater> approveCheater(Long id) {
        Cheater cheater = cheaterMapper.findById(id);
        if (cheater == null) {
            return Result.BadRequest("记录不存在");
        }
        cheaterMapper.updateStatus(id, "approved");
        cheater.setStatus("approved");
        return Result.success(cheater);
    }
}
