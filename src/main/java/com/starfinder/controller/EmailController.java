package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.service.EmailService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Random;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    @PostMapping("/send")
    public Result<String> sendCode(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return Result.BadRequest("邮箱不能为空");
        }

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        boolean success = emailService.sendVerificationCode(email, code);

        if (success) {
            return Result.success(code);
        } else {
            return Result.error("验证码发送失败，请检查配置或稍后再试");
        }
    }
}
