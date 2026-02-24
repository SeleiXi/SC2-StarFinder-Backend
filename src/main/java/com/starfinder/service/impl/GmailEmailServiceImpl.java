package com.starfinder.service.impl;

import com.starfinder.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class GmailEmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${app.system.email}")
    private String fromEmail;

    private static final String EMAIL_PREFIX = "email:code:";

    @Override
    public boolean sendVerificationCode(String email, String code) {
        // Save to Redis with 5-minute expiration (best-effort; don't fail if Redis unavailable)
        try {
            stringRedisTemplate.opsForValue().set(EMAIL_PREFIX + email, code, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Warning: Redis unavailable when storing email code: " + e.getMessage());
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("StarFinder 验证码");
            message.setText("您的验证码是：" + code + "，有效期为 5 分钟。如非本人操作请忽略。");
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            // In case of failure, we still keep it in redis for a while, 
            // but the user won't get the email.
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String key = EMAIL_PREFIX + email;
        try {
            String cachedCode = stringRedisTemplate.opsForValue().get(key);
            if (cachedCode != null && cachedCode.equals(code)) {
                try { stringRedisTemplate.delete(key); } catch (Exception ignore) {}
                return true;
            }
            return false;
        } catch (Exception e) {
            // If Redis is unavailable, do not throw — verification simply fails.
            System.err.println("Warning: Redis unavailable when verifying email code: " + e.getMessage());
            return false;
        }
    }
}
