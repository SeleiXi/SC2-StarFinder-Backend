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
        // Save to Redis with 5-minute expiration
        stringRedisTemplate.opsForValue().set(EMAIL_PREFIX + email, code, 5, TimeUnit.MINUTES);

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
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (cachedCode != null && cachedCode.equals(code)) {
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
