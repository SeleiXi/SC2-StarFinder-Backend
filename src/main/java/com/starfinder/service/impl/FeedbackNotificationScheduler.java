package com.starfinder.service.impl;

import com.starfinder.entity.Feedback;
import com.starfinder.entity.User;
import com.starfinder.mapper.FeedbackMapper;
import com.starfinder.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class FeedbackNotificationScheduler {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private UserMapper userMapper;

    @Resource
    private JavaMailSender mailSender;

    @Value("${app.system.email}")
    private String fromEmail;

    /**
     * Run every day at 09:00 AM (server time).
     * If pending feedbacks >= 5, send email digest to admins who opted in.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPendingFeedbackDigest() {
        try {
            int pendingCount = feedbackMapper.countPending();
            if (pendingCount < 5) {
                return;
            }

            List<Feedback> pendingFeedbacks = feedbackMapper.findByStatus("pending");
            List<User> admins = userMapper.findAdminsWithNotifications();

            if (admins.isEmpty()) {
                return;
            }

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            StringBuilder body = new StringBuilder();
            body.append("StarFinder 管理后台通知\n\n");
            body.append("日期: ").append(today).append("\n");
            body.append("当前待处理反馈数量: ").append(pendingCount).append("\n\n");
            body.append("--- 待处理反馈列表 ---\n\n");

            int index = 1;
            for (Feedback fb : pendingFeedbacks) {
                body.append(index++).append(". ");
                body.append("[#").append(fb.getId()).append("] ");
                body.append("提交者: ").append(fb.getAuthorTag() != null ? fb.getAuthorTag() : "未知").append("\n");
                String content = fb.getContent();
                if (content != null && content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                body.append("   内容: ").append(content).append("\n");
                body.append("   时间: ").append(fb.getCreatedAt() != null ? fb.getCreatedAt().toString() : "未知").append("\n\n");
            }

            body.append("请登录管理后台处理这些反馈。\n");
            body.append("如不希望继续接收此通知，请在管理后台关闭邮件通知。");

            for (User admin : admins) {
                if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                    try {
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setFrom(fromEmail);
                        message.setTo(admin.getEmail());
                        message.setSubject("StarFinder 待处理反馈通知 (" + pendingCount + "条) - " + today);
                        message.setText(body.toString());
                        mailSender.send(message);
                    } catch (Exception e) {
                        System.err.println("Failed to send notification to " + admin.getEmail() + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("Feedback notification sent to " + admins.size() + " admin(s). Pending count: " + pendingCount);

        } catch (Exception e) {
            System.err.println("FeedbackNotificationScheduler error: " + e.getMessage());
        }
    }
}
