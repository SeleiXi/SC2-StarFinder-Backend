package com.starfinder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.starfinder.mapper.UserMapper;
import com.starfinder.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SC2PulseService {

    @Autowired
    private UserMapper userMapper;

    private final RestTemplate restTemplate;

    @Value("${sc2pulse.base-url}")
    private String baseUrl;

    public SC2PulseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search for characters by name
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchCharacters(String name) {
        try {
            String url = baseUrl + "/characters?field=id&name=" + name;
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get character teams (MMR data) by characterId
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCharacterTeams(Long characterId, String queue) {
        try {
            String url = baseUrl + "/character-teams?characterId=" + characterId;
            if (queue != null && !queue.isEmpty()) {
                url += "&queue=" + queue;
            }
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get current live streams
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getStreams() {
        try {
            String url = baseUrl + "/streams";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get MMR for a character by queue
     */
    public Integer getMMR(Long characterId, String queue) {
        List<Map<String, Object>> teams = getCharacterTeams(characterId, queue);
        if (!teams.isEmpty()) {
            Object rating = teams.get(0).get("rating");
            if (rating instanceof Number) {
                return ((Number) rating).intValue();
            }
        }
        return null;
    }

    /**
     * Get 1v1 MMR for a character by race
     */
    public Integer getMMRByRace(Long characterId, String race) {
        List<Map<String, Object>> teams = getCharacterTeams(characterId, "LOTV_1V1");
        for (Map<String, Object> team : teams) {
            List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
            if (members != null && !members.isEmpty()) {
                Object playerRace = members.get(0).get("favoriteRace");
                if (playerRace == null)
                    playerRace = members.get(0).get("race");
                if (race.equalsIgnoreCase(String.valueOf(playerRace))) {
                    Object rating = team.get("rating");
                    if (rating instanceof Number) {
                        return ((Number) rating).intValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Daily update for all users in database
     * Runs at 4 AM every day
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void updateAllUsersMMR() {
        List<User> users = userMapper.findAll();
        for (User user : users) {
            updateUserMMR(user);
        }
    }

    public void updateUserMMR(User user) {
        String battleTag = user.getBattleTag();
        if (battleTag == null || !battleTag.contains("#"))
            return;

        Long charId = user.getCharacterId();
        if (charId == null) {
            charId = findCharacterId(battleTag);
            if (charId != null) {
                user.setCharacterId(charId);
            } else {
                return;
            }
        }

        // Update 1v1 MMR (Highest among all races)
        List<Map<String, Object>> teams1v1 = getCharacterTeams(charId, "LOTV_1V1");
        int max1v1 = 0;
        String bestRace = user.getRace();
        for (Map<String, Object> team : teams1v1) {
            Object ratingObj = team.get("rating");
            if (ratingObj instanceof Number) {
                int r = ((Number) ratingObj).intValue();
                if (r > max1v1) {
                    max1v1 = r;
                    // Try to get race
                    List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
                    if (members != null && !members.isEmpty()) {
                        Object race = members.get(0).get("favoriteRace");
                        if (race == null)
                            race = members.get(0).get("race");
                        if (race != null)
                            bestRace = String.valueOf(race).substring(0, 1).toUpperCase();
                    }
                }
            }
        }
        if (max1v1 > 0) {
            user.setMmr(max1v1);
            user.setRace(bestRace);
        }

        // Update 2v2, 3v3, 4v4
        Integer mmr2v2 = getMMR(charId, "LOTV_2V2");
        if (mmr2v2 != null)
            user.setMmr2v2(mmr2v2);

        Integer mmr3v3 = getMMR(charId, "LOTV_3V3");
        if (mmr3v3 != null)
            user.setMmr3v3(mmr3v3);

        Integer mmr4v4 = getMMR(charId, "LOTV_4V4");
        if (mmr4v4 != null)
            user.setMmr4v4(mmr4v4);

        userMapper.update(user);
    }

    /**
     * Get 1v1 MMR for a character (legacy)
     */
    public Integer getMMR(Long characterId) {
        return getMMR(characterId, "LOTV_1V1");
    }

    /**
     * Find characterId by battleTag
     */
    @SuppressWarnings("unchecked")
    public Long findCharacterId(String battleTag) {
        String playerName = battleTag.split("#")[0];
        List<Map<String, Object>> characters = searchCharacters(playerName);

        for (Map<String, Object> charInfo : characters) {
            Object idObj = charInfo.get("id");
            if (idObj == null)
                continue;
            Long charId = ((Number) idObj).longValue();

            // Verify by checking teams for matching battleTag
            List<Map<String, Object>> teams = getCharacterTeams(charId, null);
            for (Map<String, Object> team : teams) {
                Object membersObj = team.get("members");
                if (membersObj instanceof List) {
                    List<Map<String, Object>> members = (List<Map<String, Object>>) membersObj;
                    for (Map<String, Object> member : members) {
                        Object accountObj = member.get("account");
                        if (accountObj instanceof Map) {
                            Map<String, Object> account = (Map<String, Object>) accountObj;
                            if (battleTag.equals(account.get("battleTag"))) {
                                return charId;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
