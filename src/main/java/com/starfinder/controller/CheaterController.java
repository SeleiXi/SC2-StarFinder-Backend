package com.starfinder.controller;

import com.starfinder.dto.CheaterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Cheater;
import com.starfinder.service.CheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cheater")
public class CheaterController {

    @Autowired
    private CheaterService cheaterService;

    @PostMapping("/report")
    public Result<Cheater> reportCheater(@RequestBody CheaterDTO dto,
            @RequestParam(required = false) Long userId) {
        return cheaterService.reportCheater(dto, userId);
    }

    @GetMapping("/list")
    public List<Cheater> getCheaters() {
        return cheaterService.getApprovedCheaters();
    }

    @GetMapping("/search")
    public List<Cheater> searchCheaters(@RequestParam String battleTag) {
        return cheaterService.searchCheaters(battleTag);
    }

    @PutMapping("/{id}/approve")
    public Result<Cheater> approveCheater(@PathVariable Long id) {
        return cheaterService.approveCheater(id);
    }
}
