package com.starfinder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SC2PulseService {

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
     * Get MMR for a character
     */
    public Integer getMMR(Long characterId) {
        List<Map<String, Object>> teams = getCharacterTeams(characterId, "LOTV_1V1");
        if (!teams.isEmpty()) {
            Object rating = teams.get(0).get("rating");
            if (rating instanceof Number) {
                return ((Number) rating).intValue();
            }
        }
        return null;
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
            if (idObj == null) continue;
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
