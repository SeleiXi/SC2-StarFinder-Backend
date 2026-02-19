package com.starfinder.service;

import com.starfinder.dto.CheaterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Cheater;

import java.util.List;

public interface CheaterService {
    Result<Cheater> reportCheater(CheaterDTO dto, Long reportedBy);
    List<Cheater> getApprovedCheaters();
    List<Cheater> searchCheaters(String battleTag);
    Result<Cheater> approveCheater(Long id);
}
