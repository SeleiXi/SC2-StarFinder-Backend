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
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SC2PulseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search for characters by name - returns only IDs (legacy)
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
     * Rich character search using /api/characters?query= 
     * Returns full data: ratingMax, leagueMax, totalGamesPlayed, members (battleTag, region, raceGames)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchCharactersRich(String query) {
        try {
            String url = baseUrl + "/characters?query=" + java.net.URLEncoder.encode(query, "UTF-8") + "&limit=10";
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
        final String cacheKey = "cache:sc2:streams";
        try {
            // Try Redis cache first
            try {
                String cached = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cached != null && !cached.isBlank()) {
                    List<Map<String, Object>> cachedList = objectMapper.readValue(cached, List.class);
                    return cachedList;
                }
            } catch (Exception ignored) { }

            String url = baseUrl + "/streams";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> body = response.getBody() != null ? response.getBody() : Collections.emptyList();

            try {
                String json = objectMapper.writeValueAsString(body);
                stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(24));
            } catch (Exception ignored) { }

            return body;
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

        // Update 1v1 MMR per race
        List<Map<String, Object>> teams1v1 = getCharacterTeams(charId, null);
        int max1v1 = 0;
        String bestRace = user.getRace();
        java.util.Map<String, Integer> perRaceBest = new java.util.HashMap<>();

        for (Map<String, Object> team : teams1v1) {
            // queueType is inside league.queueType
            Object leagueObj = team.get("league");
            int qt = -1;
            if (leagueObj instanceof Map) {
                Object qtObj = ((Map<?,?>)leagueObj).get("queueType");
                qt = (qtObj instanceof Number) ? ((Number)qtObj).intValue() : -1;
            }
            if (qt != 201) continue; // Only 1v1

            Object ratingObj = team.get("rating");
            if (!(ratingObj instanceof Number)) continue;
            int r = ((Number) ratingObj).intValue();

            List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
            if (members == null || members.isEmpty()) continue;

            // Get race from raceGames map
            String raceStr = getDominantRace(members.get(0));
            if (raceStr != null) {
                // Map full name to single char (ZERG→Z, TERRAN→T, PROTOSS→P, RANDOM→R)
                String raceCode = raceStr.substring(0, 1).toUpperCase();
                perRaceBest.merge(raceCode, r, Math::max);
            }

            if (r > max1v1) {
                max1v1 = r;
                if (raceStr != null) bestRace = raceStr.substring(0, 1).toUpperCase();
            }
        }

        if (max1v1 > 0) {
            user.setMmr(max1v1);
            if (bestRace != null) user.setRace(bestRace);
        }
        if (perRaceBest.containsKey("T")) user.setMmrTerran(perRaceBest.get("T"));
        if (perRaceBest.containsKey("Z")) user.setMmrZerg(perRaceBest.get("Z"));
        if (perRaceBest.containsKey("P")) user.setMmrProtoss(perRaceBest.get("P"));
        if (perRaceBest.containsKey("R")) user.setMmrRandom(perRaceBest.get("R"));

        // Update 2v2, 3v3, 4v4
        Integer mmr2v2 = getMMR(charId, "LOTV_2V2");
        if (mmr2v2 != null) user.setMmr2v2(mmr2v2);
        Integer mmr3v3 = getMMR(charId, "LOTV_3V3");
        if (mmr3v3 != null) user.setMmr3v3(mmr3v3);
        Integer mmr4v4 = getMMR(charId, "LOTV_4V4");
        if (mmr4v4 != null) user.setMmr4v4(mmr4v4);

        userMapper.update(user);
    }

    /** Derive dominant race from a member map (from raceGames or xxxGamesPlayed fields). */
    @SuppressWarnings("unchecked")
    private String getDominantRace(Map<String, Object> member) {
        Object raceGamesObj = member.get("raceGames");
        if (raceGamesObj instanceof Map) {
            Map<String, Object> raceGames = (Map<String, Object>) raceGamesObj;
            if (!raceGames.isEmpty()) {
                return raceGames.entrySet().stream()
                    .max(java.util.Comparator.comparingInt(e -> e.getValue() instanceof Number ? ((Number)e.getValue()).intValue() : 0))
                    .map(e -> e.getKey().toUpperCase())
                    .filter(r -> !r.isEmpty() && !r.equals("NULL") && !r.equals("UNKNOWN"))
                    .orElse(null);
            }
        }
        String[] fields = {"zergGamesPlayed", "terranGamesPlayed", "protossGamesPlayed", "randomGamesPlayed"};
        String[] races = {"ZERG", "TERRAN", "PROTOSS", "RANDOM"};
        String best = null; int bestCount = 0;
        for (int i = 0; i < fields.length; i++) {
            Object v = member.get(fields[i]);
            int c = (v instanceof Number) ? ((Number)v).intValue() : 0;
            if (c > bestCount) { bestCount = c; best = races[i]; }
        }
        return best;
    }

    /**
     * Get 1v1 MMR for a character (legacy)
     */
    public Integer getMMR(Long characterId) {
        return getMMR(characterId, "LOTV_1V1");
    }

    /**
     * Find characterId by battleTag
     * Uses the rich /api/characters?query= endpoint first to avoid extra round trips.
     */
    @SuppressWarnings("unchecked")
    public Long findCharacterId(String battleTag) {
        String playerName = battleTag.split("#")[0];

        // First: try the rich query endpoint which returns battleTag directly
        List<Map<String, Object>> richResults = searchCharactersRich(playerName);
        for (Map<String, Object> charInfo : richResults) {
            Object membersObj = charInfo.get("members");
            if (membersObj instanceof Map) {
                Map<?,?> member = (Map<?,?>) membersObj;
                Object accountObj = member.get("account");
                if (accountObj instanceof Map) {
                    Object bt = ((Map<?,?>)accountObj).get("battleTag");
                    if (battleTag.equals(bt)) {
                        // Match found - extract character ID
                        Object character = member.get("character");
                        if (character instanceof Map) {
                            Object idObj = ((Map<?,?>)character).get("id");
                            if (idObj instanceof Number) return ((Number)idObj).longValue();
                        }
                    }
                }
            }
        }

        // Fallback: use id-only search then verify via character-teams
        List<Map<String, Object>> characters = searchCharacters(playerName);
        for (Map<String, Object> charInfo : characters) {
            Object idObj = charInfo.get("id");
            if (idObj == null) continue;
            Long charId = ((Number) idObj).longValue();

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
