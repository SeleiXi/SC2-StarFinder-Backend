package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.PublicReport;
import com.starfinder.mapper.PublicReportMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public-report")
public class PublicReportController {

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

        publicReportMapper.insert(report);
        return Result.success(report);
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
