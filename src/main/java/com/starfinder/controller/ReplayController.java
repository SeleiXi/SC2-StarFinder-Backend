package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.ReplayFile;
import com.starfinder.entity.User;
import com.starfinder.mapper.ReplayFileMapper;
import com.starfinder.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/replay")
public class ReplayController {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB per file
    private static final long MAX_TOTAL_SIZE_PER_USER = 100 * 1024 * 1024; // 100MB per user
    private static final int MAX_FILES_PER_USER = 20;
    private static final String UPLOAD_DIR = "/root/coding/starfinder/uploads/replays/";

    @Autowired
    private ReplayFileMapper replayFileMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<ReplayFile>> list(@RequestParam(required = false) String category) {
        List<ReplayFile> replays;
        if (category != null && !category.isEmpty()) {
            replays = replayFileMapper.findByCategory(category);
        } else {
            replays = replayFileMapper.findAll();
        }
        return Result.success(replays);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ReplayFile> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category) {

        // Validate user
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        // Validate file
        if (file == null || file.isEmpty()) return Result.BadRequest("文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) return Result.BadRequest("文件大小不能超过20MB");

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".sc2replay")) {
            return Result.BadRequest("只支持 .SC2Replay 格式的文件");
        }

        // Validate title
        if (title == null || title.trim().isEmpty()) return Result.BadRequest("标题不能为空");
        if (title.length() > 200) return Result.BadRequest("标题不能超过200字");

        // Check user quota
        int fileCount = replayFileMapper.getFileCountByUser(userId);
        if (fileCount >= MAX_FILES_PER_USER) {
            return Result.BadRequest("每位用户最多上传" + MAX_FILES_PER_USER + "个Replay文件");
        }
        Long totalSize = replayFileMapper.getTotalFileSizeByUser(userId);
        if (totalSize != null && totalSize + file.getSize() > MAX_TOTAL_SIZE_PER_USER) {
            return Result.BadRequest("您的Replay存储空间已满（最大100MB）");
        }

        // Save file
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String safeFileName = UUID.randomUUID().toString() + ".SC2Replay";
            Path filePath = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), filePath);

            ReplayFile replay = new ReplayFile();
            replay.setUserId(userId);
            replay.setTitle(title.trim());
            replay.setFileName(originalName);
            replay.setFilePath("/api/replay/download/" + safeFileName);
            replay.setFileSize(file.getSize());
            replay.setAuthorTag(user.getBattleTag() != null ? user.getBattleTag() : user.getEmail());

            if (description != null && !description.trim().isEmpty() && description.length() <= 2000) {
                replay.setDescription(description.trim());
            }
            if (category != null && !category.trim().isEmpty() && category.length() <= 50) {
                replay.setCategory(category.trim());
            }

            replayFileMapper.insert(replay);
            return Result.success(replay);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable String fileName) {
        try {
            // Prevent path traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            if (!fileName.toLowerCase().endsWith(".sc2replay")) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            if (!Files.exists(filePath)) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath);
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");
        replayFileMapper.deleteById(id);
        return Result.success("已删除");
    }
}
