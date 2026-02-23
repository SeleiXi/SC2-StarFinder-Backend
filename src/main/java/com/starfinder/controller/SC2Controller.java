package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.service.SC2PulseService;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
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

    @GetMapping("/full-mmr")
    public Result<List<Map<String, Object>>> getFullMMR(@RequestParam String battleTag) {
        try {
            List<Map<String, Object>> searchResults = new ArrayList<>();

            if (battleTag.contains("#")) {
                // Exact battleTag lookup: find by name then verify
                Long characterId = sc2PulseService.findCharacterId(battleTag);
                if (characterId != null) {
                    searchResults.add(buildCharacterData(characterId, battleTag));
                }
            } else {
                // Name-only search: use rich /api/characters?query= endpoint
                List<Map<String, Object>> richResults = sc2PulseService.searchCharactersRich(battleTag);
                for (Map<String, Object> charInfo : richResults) {
                    try {
                        // Extract data directly from the rich search response
                        Map<String, Object> result = buildFromRichSearchResult(charInfo);
                        searchResults.add(result);
                    } catch (Exception ignored) {}
                }
                // Limit to top 10
                if (searchResults.size() > 10) searchResults = searchResults.subList(0, 10);
            }

            if (searchResults.isEmpty()) {
                return Result.error("未找到对应角色");
            }

            return Result.success(searchResults);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Build result map from the /api/characters?query= rich response. 
     * Used for name-only searches to avoid extra API round-trips.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildFromRichSearchResult(Map<String, Object> charInfo) {
        Map<String, Object> data = new HashMap<>();

        Object membersObj = charInfo.get("members");
        Map<String, Object> member = (membersObj instanceof Map) ? (Map<String, Object>) membersObj : new HashMap<>();

        // BattleTag
        Object account = member.get("account");
        String battleTag = "Unknown";
        if (account instanceof Map) {
            Object bt = ((Map<?,?>)account).get("battleTag");
            if (bt != null && !"null".equalsIgnoreCase(String.valueOf(bt))) battleTag = String.valueOf(bt);
        }

        // Character ID
        Object character = member.get("character");
        Long characterId = null;
        String region = "Unknown";
        if (character instanceof Map) {
            Object idObj = ((Map<?,?>)character).get("id");
            if (idObj instanceof Number) characterId = ((Number)idObj).longValue();
            region = toRegion(((Map<?,?>)character).get("region"));
        }

        // Race
        String race = extractDominantRace(member);

        // Stats from rich search
        Object ratingMaxObj = charInfo.get("ratingMax");
        int ratingMax = (ratingMaxObj instanceof Number) ? ((Number)ratingMaxObj).intValue() : 0;
        Object leagueMaxObj = charInfo.get("leagueMax");
        String bestLeague = (leagueMaxObj instanceof Number)
            ? LEAGUE_MAP.getOrDefault(((Number)leagueMaxObj).intValue(), String.valueOf(leagueMaxObj))
            : "None";
        Object totalGamesObj = charInfo.get("totalGamesPlayed");
        int totalGames = (totalGamesObj instanceof Number) ? ((Number)totalGamesObj).intValue() : 0;

        data.put("characterId", characterId);
        data.put("battleTag", battleTag);
        data.put("region", region);
        data.put("totalGames", totalGames);
        data.put("bestAllMmr", ratingMax);
        data.put("bestLeague", bestLeague);
        data.put("race", race);

        // For per-mode MMR, make a secondary call only if we have a character ID
        Map<String, Object> mmrGroups = new LinkedHashMap<>();
        List<Map<String, Object>> v1v1Results = new ArrayList<>();
        Integer last1v1Mmr = null;
        if (characterId != null) {
            List<Map<String, Object>> allTeams = sc2PulseService.getCharacterTeams(characterId, null);
            Map<String, Integer> bestPerRace = new LinkedHashMap<>();
            for (Map<String, Object> team : allTeams) {
                Object leagueObj = team.get("league");
                int qt = -1;
                if (leagueObj instanceof Map) {
                    Object qtObj = ((Map<?,?>)leagueObj).get("queueType");
                    qt = (qtObj instanceof Number) ? ((Number)qtObj).intValue() : -1;
                }
                if (qt < 201 || qt > 204) continue;
                Object ratingObj = team.get("rating");
                int rating = (ratingObj instanceof Number) ? ((Number)ratingObj).intValue() : 0;
                if (qt == 201) {
                    List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
                    if (members != null && !members.isEmpty()) {
                        String raceStr = extractDominantRace(members.get(0));
                        if (raceStr != null) {
                            bestPerRace.merge(raceStr, rating, Math::max);
                            if (last1v1Mmr == null || rating > last1v1Mmr) last1v1Mmr = rating;
                        }
                    }
                } else if (qt == 202) {
                    if (!mmrGroups.containsKey("2v2") || rating > (int)mmrGroups.get("2v2"))
                        mmrGroups.put("2v2", rating);
                } else if (qt == 203) {
                    if (!mmrGroups.containsKey("3v3") || rating > (int)mmrGroups.get("3v3"))
                        mmrGroups.put("3v3", rating);
                } else if (qt == 204) {
                    if (!mmrGroups.containsKey("4v4") || rating > (int)mmrGroups.get("4v4"))
                        mmrGroups.put("4v4", rating);
                }
            }
            bestPerRace.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> t = new HashMap<>();
                    t.put("race", e.getKey());
                    t.put("rating", e.getValue());
                    v1v1Results.add(t);
                });
        }
        data.put("last1v1Mmr", last1v1Mmr != null ? last1v1Mmr : ratingMax);
        data.put("last1v1Games", 0);
        data.put("mmrGroups", mmrGroups);
        mmrGroups.put("1v1", v1v1Results);
        return data;
    }

    private static final Map<Integer, String> LEAGUE_MAP = Map.of(
        0, "青铜", 1, "白银", 2, "黄金", 3, "铂金", 4, "钻石", 5, "大师", 6, "宗师"
    );
    private static final Map<String, String> REGION_STR_MAP = new java.util.HashMap<>(Map.of(
        "NA", "美服 (NA)", "US", "美服 (NA)",
        "EU", "欧服 (EU)",
        "KR", "韩服 (KR)",
        "1", "美服 (NA)", "2", "欧服 (EU)", "3", "韩服 (KR)"
    ));
    // Derive region string from a raw value (String or Integer)
    private String toRegion(Object raw) {
        if (raw == null) return "Unknown";
        return REGION_STR_MAP.getOrDefault(String.valueOf(raw).toUpperCase(), String.valueOf(raw));
    }

    private Map<String, Object> buildCharacterData(Long characterId, String battleTag) {
        Map<String, Object> data = new HashMap<>();
        data.put("characterId", characterId);
        
        // Use all queues to get better summary
        List<Map<String, Object>> allTeams = sc2PulseService.getCharacterTeams(characterId, null);
        
        String finalBattleTag = battleTag;
        String region = "Unknown";
        int totalGames = 0;
        int bestAllMmr = 0;
        String bestLeague = "None";
        Integer last1v1Mmr = null;
        int last1v1Games = 0;
        
        Map<String, Object> mmrGroups = new LinkedHashMap<>();
        // bestPerRace: race -> best rating for 1v1 deduplication
        Map<String, Integer> bestPerRace = new LinkedHashMap<>();

        for (Map<String, Object> team : allTeams) {
            // queueType is inside league object, NOT at top level
            Object leagueObj = team.get("league");
            int qt = -1;
            int leagueType = -1;
            if (leagueObj instanceof Map) {
                Object qtObj = ((Map<?,?>)leagueObj).get("queueType");
                Object ltObj = ((Map<?,?>)leagueObj).get("type");
                qt = (qtObj instanceof Number) ? ((Number)qtObj).intValue() : -1;
                leagueType = (ltObj instanceof Number) ? ((Number)ltObj).intValue() : -1;
            }
            if (qt < 201 || qt > 204) continue; // Skip non-LOTV / unknown queues

            Object ratingObj = team.get("rating");
            int rating = (ratingObj instanceof Number) ? ((Number) ratingObj).intValue() : 0;

            Object winsObj = team.get("wins");
            Object lossesObj = team.get("losses");
            int wins = (winsObj instanceof Number) ? ((Number)winsObj).intValue() : 0;
            int losses = (lossesObj instanceof Number) ? ((Number)lossesObj).intValue() : 0;
            totalGames += wins + losses;

            if (rating > bestAllMmr) {
                bestAllMmr = rating;
                bestLeague = LEAGUE_MAP.getOrDefault(leagueType, String.valueOf(leagueType));
            }
            if ("Unknown".equals(region)) {
                region = toRegion(team.get("region"));
            }

            // Extract BattleTag from first member if not provided
            if (finalBattleTag == null) {
                Object membersObj = team.get("members");
                if (membersObj instanceof List && !((List<?>)membersObj).isEmpty()) {
                    Object member = ((List<?>)membersObj).get(0);
                    if (member instanceof Map) {
                        Object account = ((Map<?,?>)member).get("account");
                        if (account instanceof Map) {
                            Object bt = ((Map<?,?>)account).get("battleTag");
                            if (bt != null && !"null".equalsIgnoreCase(String.valueOf(bt))) {
                                finalBattleTag = String.valueOf(bt);
                            }
                        }
                    }
                }
            }

            // Grouping for 1v1 vs Teams
            if (qt == 201) {
                last1v1Games += wins + losses;

                List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
                if (members != null && !members.isEmpty()) {
                    // Race: derive from raceGames map (pick race with most games)
                    String raceStr = extractDominantRace(members.get(0));
                    if (raceStr != null) {
                        bestPerRace.merge(raceStr, rating, Math::max);
                        if (last1v1Mmr == null || rating > last1v1Mmr) {
                            last1v1Mmr = rating;
                        }
                    }
                }
            } else if (qt == 202) {
                if (!mmrGroups.containsKey("2v2") || rating > (int)mmrGroups.get("2v2"))
                    mmrGroups.put("2v2", rating);
            } else if (qt == 203) {
                if (!mmrGroups.containsKey("3v3") || rating > (int)mmrGroups.get("3v3"))
                    mmrGroups.put("3v3", rating);
            } else if (qt == 204) {
                if (!mmrGroups.containsKey("4v4") || rating > (int)mmrGroups.get("4v4"))
                    mmrGroups.put("4v4", rating);
            }
        }

        // Build deduplicated, sorted 1v1 results (by descending rating)
        List<Map<String, Object>> v1v1Results = new ArrayList<>();
        bestPerRace.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> {
                Map<String, Object> t = new HashMap<>();
                t.put("race", e.getKey());
                t.put("rating", e.getValue());
                v1v1Results.add(t);
            });

        data.put("battleTag", finalBattleTag != null ? finalBattleTag : "Unknown#" + characterId);
        data.put("region", region);
        data.put("totalGames", totalGames);
        data.put("bestAllMmr", bestAllMmr);
        data.put("bestLeague", bestLeague);
        data.put("last1v1Mmr", last1v1Mmr);
        data.put("last1v1Games", last1v1Games);
        data.put("mmrGroups", mmrGroups);
        mmrGroups.put("1v1", v1v1Results);

        return data;
    }

    /**
     * Derive the dominant (most-played) race from a team member map.
     * Checks both the legacy xxxGamesPlayed fields and the raceGames map.
     */
    @SuppressWarnings("unchecked")
    private String extractDominantRace(Map<String, Object> member) {
        // Try raceGames map first (e.g. {"ZERG":155,"PROTOSS":5})
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
        // Fallback: check individual xxxGamesPlayed fields
        Map<String, String> fieldToRace = new LinkedHashMap<>();
        fieldToRace.put("zergGamesPlayed", "ZERG");
        fieldToRace.put("terranGamesPlayed", "TERRAN");
        fieldToRace.put("protossGamesPlayed", "PROTOSS");
        fieldToRace.put("randomGamesPlayed", "RANDOM");
        String bestRace = null;
        int bestCount = 0;
        for (Map.Entry<String, String> e : fieldToRace.entrySet()) {
            Object v = member.get(e.getKey());
            int count = (v instanceof Number) ? ((Number)v).intValue() : 0;
            if (count > bestCount) { bestCount = count; bestRace = e.getValue(); }
        }
        return bestRace;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchCharacters(@RequestParam String name) {
        return sc2PulseService.searchCharactersRich(name);
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

    private List<Map<String, Object>> cachedStreams = null;
    private long lastStreamsFetchTime = 0;
    private static final long STREAMS_CACHE_DURATION_MS = 60000; // 1 minute cache

    @SuppressWarnings("unchecked")
    @GetMapping("/streams")
    public List<Map<String, Object>> getStreams() {
        long currentTime = System.currentTimeMillis();
        if (cachedStreams != null && (currentTime - lastStreamsFetchTime) < STREAMS_CACHE_DURATION_MS) {
            return cachedStreams;
        }

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
                    flat.put("userName", u.getBattleTag() != null ? u.getBattleTag() : u.getEmail());
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

        cachedStreams = result;
        lastStreamsFetchTime = currentTime;

        return result;
    }

    @PostMapping("/update-all-mmr")
    public Result<String> updateAllMMR() {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (!principal.isAdmin()) return Result.BadRequest("无权限");
        try {
            sc2PulseService.updateAllUsersMMR();
            return Result.success("已启动全量MMR更新");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
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
