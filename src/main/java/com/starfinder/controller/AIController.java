package com.starfinder.controller;

import com.starfinder.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.model:deepseek-chat}")
    private String modelName;

    private static final String SYSTEM_PROMPT = "You are a professional StarCraft II tactical advisor. " +
            "You have expert knowledge of game mechanics, build orders, unit counters, and map strategies. " +
            "Provide concise, strategic advice in the language the user uses. " +
            "If asked about units, explain their strengths and weaknesses.";

    @PostMapping("/query")
    public Result<String> askAI(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return Result.BadRequest("Prompt cannot be empty");
        }

        if (apiKey == null || apiKey.equals("NONE") || apiKey.isEmpty()) {
            return Result.error("AI service is not configured (API Key is missing)");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.add(Map.of("role", "user", "content", prompt));
            
            body.put("messages", messages);
            body.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    return Result.success((String) message.get("content"));
                }
            }
            return Result.error("AI service returned an empty response");
        } catch (Exception e) {
            return Result.error("AI service failure: " + e.getMessage());
        }
    }
}
