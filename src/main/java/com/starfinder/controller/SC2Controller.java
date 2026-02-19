package com.starfinder.controller;

import com.starfinder.service.SC2PulseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sc2")
public class SC2Controller {

    @Autowired
    private SC2PulseService sc2PulseService;

    @GetMapping("/search")
    public List<Map<String, Object>> searchCharacters(@RequestParam String name) {
        return sc2PulseService.searchCharacters(name);
    }

    @GetMapping("/mmr/{characterId}")
    public Map<String, Object> getMMR(@PathVariable Long characterId,
                                       @RequestParam(defaultValue = "LOTV_1V1") String queue) {
        List<Map<String, Object>> teams = sc2PulseService.getCharacterTeams(characterId, queue);
        if (!teams.isEmpty()) {
            return teams.get(0);
        }
        return Map.of("error", "No data found");
    }

    @GetMapping("/streams")
    public List<Map<String, Object>> getStreams() {
        return sc2PulseService.getStreams();
    }

    @GetMapping("/character-id")
    public Map<String, Object> findCharacterId(@RequestParam String battleTag) {
        Long id = sc2PulseService.findCharacterId(battleTag);
        if (id != null) {
            return Map.of("characterId", id);
        }
        return Map.of("error", "Character not found");
    }
}
