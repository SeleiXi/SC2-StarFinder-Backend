package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.service.EmailService;
import com.starfinder.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.SecureRandom;
import java.time.Duration;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String EMAIL_COOLDOWN_PREFIX = "email:send:cooldown:";
    private static final String IP_RATE_PREFIX = "email:send:ip:";

    @PostMapping("/send")
    public Result<String> sendCode(@RequestParam String email,
                                   @RequestParam(required = false) Boolean requireRegistered,
                                   HttpServletRequest request) {
        if (email == null || email.isEmpty()) {
            return Result.BadRequest("邮箱不能为空");
        }

        String normalizedEmail = email.trim().toLowerCase();
        // If this send request requires the email to already be registered (login/reset flows),
        // check the DB and return a clear error if not found.
        if (Boolean.TRUE.equals(requireRegistered)) {
            try {
                if (userMapper.findByEmail(normalizedEmail) == null) {
                    return Result.BadRequest("该邮箱未注册");
                }
            } catch (Exception e) {
                // If DB check fails for unexpected reasons, log and allow send to proceed conservatively.
                System.err.println("Warning: user existence check failed: " + e.getMessage());
            }
        }
        String clientIp = getClientIp(request);

        // Cooldown per email (60s) and per-IP rate limiting — best-effort when Redis available
        String cooldownKey = EMAIL_COOLDOWN_PREFIX + normalizedEmail;
        try {
            Boolean hasCooldown = stringRedisTemplate.hasKey(cooldownKey);
            if (Boolean.TRUE.equals(hasCooldown)) {
                return Result.BadRequest("请求过于频繁，请稍后再试");
            }

            // Simple per-IP rate limit: 20/minute
            String ipKey = IP_RATE_PREFIX + clientIp + ":" + (System.currentTimeMillis() / 60000);
            Long count = stringRedisTemplate.opsForValue().increment(ipKey);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(ipKey, Duration.ofMinutes(2));
            }
            if (count != null && count > 20) {
                return Result.BadRequest("请求过于频繁，请稍后再试");
            }

            stringRedisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(60));
        } catch (Exception e) {
            // Redis unavailable — log and continue without rate-limiting to avoid 500 errors
            System.err.println("Warning: Redis unavailable for email cooldown/rate limit: " + e.getMessage());
        }

        // Generate 6-digit code
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        
        boolean success = emailService.sendVerificationCode(normalizedEmail, code);

        if (success) {
            // Never return code to the client.
            return Result.success("验证码已发送");
        } else {
            return Result.error("验证码发送失败，请检查配置或稍后再试");
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // first IP in list
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return request.getRemoteAddr();
    }
}
