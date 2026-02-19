package com.starfinder.controller;

import com.starfinder.service.SC2PulseService;
import com.starfinder.mapper.UserMapper;
import com.starfinder.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sc2")
public class SC2Controller {

    @Autowired
    private SC2PulseService sc2PulseService;

    @Autowired
    private UserMapper userMapper;

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

    @SuppressWarnings("unchecked")
    @GetMapping("/streams")
    public List<Map<String, Object>> getStreams() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. Fetch and flatten SC2 Pulse streams
        try {
            List<Map<String, Object>> rawStreams = sc2PulseService.getStreams();
            for (Map<String, Object> entry : rawStreams) {
                try {
                    Map<String, Object> flat = new HashMap<>();

                    // Extract stream info
                    Object streamObj = entry.get("stream");
                    if (streamObj instanceof Map) {
                        Map<String, Object> stream = (Map<String, Object>) streamObj;
                        flat.put("url", stream.get("url"));
                        flat.put("streamUrl", stream.get("url"));
                        flat.put("service", stream.get("service"));
                        flat.put("userName", stream.get("userName"));
                    }

                    // Extract pro player info
                    Object proObj = entry.get("proPlayer");
                    if (proObj instanceof Map) {
                        Map<String, Object> pro = (Map<String, Object>) proObj;
                        flat.put("proNickname", pro.getOrDefault("nickname", pro.get("proNickname")));
                        flat.put("proTeam", pro.getOrDefault("team", pro.get("proTeam")));
                    }

                    // Extract team/MMR info (could be "ladderTeam" or "team")
                    Object teamObj = entry.get("ladderTeam");
                    if (teamObj == null)
                        teamObj = entry.get("team");
                    if (teamObj instanceof Map) {
                        Map<String, Object> team = (Map<String, Object>) teamObj;
                        flat.put("rating", team.get("rating"));
                        Object members = team.get("members");
                        if (members instanceof List && !((List<?>) members).isEmpty()) {
                            Object firstMember = ((List<?>) members).get(0);
                            if (firstMember instanceof Map) {
                                Map<String, Object> member = (Map<String, Object>) firstMember;
                                Object race = member.get("favoriteRace");
                                if (race == null)
                                    race = member.get("race");
                                flat.put("race", race);
                            }
                        }
                    }

                    // Fallback: if entry itself has flat fields
                    if (!flat.containsKey("userName") && entry.containsKey("userName"))
                        flat.put("userName", entry.get("userName"));
                    if (!flat.containsKey("url") && entry.containsKey("url"))
                        flat.put("url", entry.get("url"));
                    if (!flat.containsKey("rating") && entry.containsKey("rating"))
                        flat.put("rating", entry.get("rating"));

                    flat.put("source", "sc2pulse");
                    result.add(flat);
                } catch (Exception ignored) {
                    // Skip malformed entries
                }
            }
        } catch (Exception ignored) {
            // SC2 Pulse unavailable
        }

        // 2. Also add local users who have streamUrl set
        try {
            List<User> allUsers = userMapper.findAll();
            for (User u : allUsers) {
                if (u.getStreamUrl() != null && !u.getStreamUrl().isEmpty()) {
                    Map<String, Object> flat = new HashMap<>();
                    flat.put("userName", u.getName());
                    flat.put("url", u.getStreamUrl());
                    flat.put("streamUrl", u.getStreamUrl());
                    flat.put("rating", u.getMmr());
                    flat.put("race", u.getRace());
                    flat.put("service", "社区");
                    flat.put("source", "local");
                    result.add(flat);
                }
            }
        } catch (Exception ignored) {
        }

        return result;
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
