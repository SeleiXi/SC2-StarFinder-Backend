package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.PublicReport;
import com.starfinder.mapper.PublicReportMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public-report")
public class PublicReportController {

    private static final String UPLOAD_DIR = "/root/coding/starfinder/uploads/report-images/";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    private PublicReportMapper publicReportMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<PublicReport>> list(@RequestParam(required = false) String search) {
        List<PublicReport> reports;
        if (search != null && !search.isEmpty()) {
            reports = publicReportMapper.searchByGameId(search);
        } else {
            reports = publicReportMapper.findAll();
        }
        return Result.success(reports);
    }

    @PostMapping
    public Result<PublicReport> create(@RequestBody Map<String, Object> body) {
        String gameId = (String) body.get("gameId");
        String description = (String) body.get("description");
        
        if (gameId == null || gameId.trim().isEmpty()) {
            return Result.BadRequest("游戏ID不能为空");
        }
        if (description == null || description.trim().isEmpty()) {
            return Result.BadRequest("描述不能为空");
        }
        if (description.length() > 2000) {
            return Result.BadRequest("描述不能超过2000字");
        }
        if (gameId.length() > 100) {
            return Result.BadRequest("游戏ID过长");
        }

        PublicReport report = new PublicReport();
        report.setGameId(gameId.trim());
        report.setDescription(description.trim());

        Object mmrMinObj = body.get("mmrMin");
        Object mmrMaxObj = body.get("mmrMax");
        if (mmrMinObj instanceof Number) report.setMmrMin(((Number) mmrMinObj).intValue());
        if (mmrMaxObj instanceof Number) report.setMmrMax(((Number) mmrMaxObj).intValue());

        Object userIdObj = body.get("userId");
        if (userIdObj instanceof Number) report.setReportedById(((Number) userIdObj).longValue());

        String imageUrl = (String) body.get("imageUrl");
        if (imageUrl != null && !imageUrl.trim().isEmpty() && imageUrl.length() <= 500) {
            report.setImageUrl(imageUrl.trim());
        }

        publicReportMapper.insert(report);
        return Result.success(report);
    }

    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) return Result.BadRequest("文件不能为空");
        if (file.getSize() > MAX_IMAGE_SIZE) return Result.BadRequest("图片大小不能超过5MB");

        String originalName = file.getOriginalFilename();
        if (originalName == null) return Result.BadRequest("无效文件");
        String lower = originalName.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")
                && !lower.endsWith(".gif") && !lower.endsWith(".webp")) {
            return Result.BadRequest("只支持 jpg/png/gif/webp 格式的图片");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String ext = originalName.substring(originalName.lastIndexOf('.'));
            String safeFileName = UUID.randomUUID().toString() + ext;
            Path filePath = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), filePath);

            String url = "/api/public-report/image/" + safeFileName;
            return Result.success(Map.of("url", url));
        } catch (IOException e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/image/{fileName}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String fileName) {
        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            if (!Files.exists(filePath)) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath);
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam(required = false) Long adminId) {
        if (adminId == null) return Result.BadRequest("需要管理员权限");
        User admin = userMapper.findById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) return Result.BadRequest("无权限");
        publicReportMapper.deleteById(id);
        return Result.success("已删除");
    }
}
