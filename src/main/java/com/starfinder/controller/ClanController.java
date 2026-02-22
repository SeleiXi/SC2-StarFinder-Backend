package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.ClanRecruitment;
import com.starfinder.entity.User;
import com.starfinder.mapper.ClanRecruitmentMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.service.SC2PulseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/clan")
public class ClanController {

    @Autowired
    private ClanRecruitmentMapper clanRecruitmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SC2PulseService sc2PulseService;

    @Value("${sc2pulse.base-url}")
    private String sc2PulseBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private static final Map<Integer, String> LEAGUE_MAP = Map.of(
        0, "青铜", 1, "白银", 2, "黄金", 3, "铂金", 4, "钻石", 5, "大师", 6, "宗师"
    );

    // Cache for clan ranking (1 hour)
    private List<Map<String, Object>> cachedRanking = null;
    private long lastRankingFetch = 0;
    private static final long CACHE_TTL = 3600000L;

    @SuppressWarnings("unchecked")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getClanRanking(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "US") String region) {
        try {
            String cacheKey = query + "_" + region;
            long now = System.currentTimeMillis();

            // Use cache for empty query
            if (query.isEmpty() && cachedRanking != null && (now - lastRankingFetch) < CACHE_TTL) {
                return Result.success(cachedRanking);
            }

            String url = sc2PulseBaseUrl + "/clans";
            if (!query.isEmpty()) {
                url += "?query=" + java.net.URLEncoder.encode(query, "UTF-8");
            } else {
                url += "?ids=";
            }

            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            Object body = response.getBody();

            List<Map<String, Object>> rawList = new ArrayList<>();
            if (body instanceof List) {
                rawList = (List<Map<String, Object>>) body;
            } else if (body instanceof Map && ((Map<?,?>)body).containsKey("result")) {
                rawList = (List<Map<String, Object>>) ((Map<?,?>)body).get("result");
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> clan : rawList) {
                Map<String, Object> r = new LinkedHashMap<>(clan);
                // Map league type
                Object lt = r.get("avgLeagueType");
                if (lt instanceof Number) {
                    r.put("avgLeagueName", LEAGUE_MAP.getOrDefault(((Number)lt).intValue(), String.valueOf(lt)));
                }
                result.add(r);
            }

            // Sort by avgRating descending (for non-query requests)
            if (query.isEmpty()) {
                result.sort((a, b) -> {
                    Number ra = (Number) a.get("avgRating");
                    Number rb = (Number) b.get("avgRating");
                    int va = ra != null ? ra.intValue() : 0;
                    int vb = rb != null ? rb.intValue() : 0;
                    return Integer.compare(vb, va);
                });
                cachedRanking = result;
                lastRankingFetch = now;
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取战队数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/recruitment")
    public Result<List<ClanRecruitment>> getRecruitments() {
        return Result.success(clanRecruitmentMapper.findAll());
    }

    @PostMapping("/recruitment")
    public Result<ClanRecruitment> createRecruitment(@RequestBody Map<String, Object> body) {
        Object userIdObj = body.get("userId");
        if (userIdObj == null) return Result.BadRequest("需要登录");

        String clanName = (String) body.get("clanName");
        if (clanName == null || clanName.trim().isEmpty()) return Result.BadRequest("战队名称不能为空");
        if (clanName.length() > 100) return Result.BadRequest("战队名称不能超过100字");

        Long userId = ((Number) userIdObj).longValue();
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        ClanRecruitment recruitment = new ClanRecruitment();
        recruitment.setUserId(userId);
        recruitment.setClanName(clanName.trim());
        recruitment.setAuthorTag(user.getBattleTag() != null ? user.getBattleTag() : user.getEmail());

        String clanTag = (String) body.get("clanTag");
        if (clanTag != null && clanTag.length() <= 20) recruitment.setClanTag(clanTag.trim());

        String region = (String) body.get("region");
        if (region != null) recruitment.setRegion(region);

        Object minMmrObj = body.get("minMmr");
        Object maxMmrObj = body.get("maxMmr");
        if (minMmrObj instanceof Number) recruitment.setMinMmr(((Number) minMmrObj).intValue());
        if (maxMmrObj instanceof Number) recruitment.setMaxMmr(((Number) maxMmrObj).intValue());

        String description = (String) body.get("description");
        if (description != null && description.length() <= 3000) recruitment.setDescription(description.trim());

        String contact = (String) body.get("contact");
        if (contact != null && contact.length() <= 200) recruitment.setContact(contact.trim());

        clanRecruitmentMapper.insert(recruitment);
        return Result.success(recruitment);
    }

    @DeleteMapping("/recruitment/{id}")
    public Result<String> deleteRecruitment(@PathVariable Long id, @RequestParam Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");
        clanRecruitmentMapper.deleteById(id);
        return Result.success("已删除");
    }
}
