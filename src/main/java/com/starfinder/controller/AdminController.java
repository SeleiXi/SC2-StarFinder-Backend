package com.starfinder.controller;

import com.starfinder.dto.*;
import com.starfinder.entity.*;
import com.starfinder.mapper.*;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CheaterMapper cheaterMapper;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private TutorialMapper tutorialMapper;
    @Autowired
    private StreamMapper streamMapper;
    @Autowired
    private ClanRecruitmentMapper clanRecruitmentMapper;
    @Autowired
    private CoachingPostMapper coachingPostMapper;
    @Autowired
    private PublicReportMapper publicReportMapper;
    @Autowired
    private TextTutorialMapper textTutorialMapper;
    @Autowired
    private ReplayFileMapper replayFileMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CheaterService cheaterService;
    @Autowired
    private EventService eventService;
    @Autowired
    private TutorialService tutorialService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STREAMS_CACHE_KEY = "cache:sc2:streams";

    // ============ Auth Check ============
    private boolean isAdmin(Long userId) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return false;
        if (userId != null && !userId.equals(principal.userId())) return false;
        return principal.isAdmin();
    }

    private boolean isSuperAdmin(Long userId) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return false;
        if (userId != null && !userId.equals(principal.userId())) return false;
        return principal.isSuperAdmin();
    }

    // ============ Users CRUD ============
    @GetMapping("/users")
    public Result<List<User>> listUsers(@RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        List<User> users = userMapper.findAll();
        for (User u : users) {
            if (u != null) u.setPassword(null);
        }
        return Result.success(users);
    }

    @PutMapping("/users/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody ProfileUpdateDTO dto,
            @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        
        // Prevent normal admin from updating other admins' profile? 
        // The requirement is mostly about role change.
        return userService.updateProfile(id, dto);
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        
        User target = userMapper.findById(id);
        if (target != null && ("admin".equals(target.getRole()) || "super_admin".equals(target.getRole()))) {
            if (!isSuperAdmin(adminId)) {
                return Result.BadRequest("普通管理员不能删除其他管理员");
            }
            if (target.getId().equals(adminId)) {
                return Result.BadRequest("不能删除自己");
            }
        }

        userMapper.deleteById(id);
        
        // Invalidate SC2 streams cache
        try {
            stringRedisTemplate.delete(STREAMS_CACHE_KEY);
        } catch (Exception ignored) {}

        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<User> setUserRole(@PathVariable Long id, @RequestParam String role,
            @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        
        User target = userMapper.findById(id);
        if (target == null)
            return Result.BadRequest("用户不存在");

        if (target.getId().equals(adminId)) {
            return Result.BadRequest("不能修改自己的角色");
        }

        boolean targetIsAdmin = "admin".equals(target.getRole()) || "super_admin".equals(target.getRole());
        boolean targetIsSuperAdmin = "super_admin".equals(target.getRole());
        boolean newRoleIsSuperAdmin = "super_admin".equals(role);

        if (!isSuperAdmin(adminId)) {
            // Normal admin permissions
            if (targetIsAdmin) {
                return Result.BadRequest("普通管理员不能修改其他管理员的角色");
            }
            if (targetIsSuperAdmin) {
                return Result.BadRequest("普通管理员不能修改超级管理员的角色");
            }
            if (!"user".equals(role) && !"admin".equals(role)) {
                return Result.BadRequest("普通管理员只能设置 user 或 admin 角色");
            }
            if (newRoleIsSuperAdmin) {
                return Result.BadRequest("普通管理员无法设置超级管理员角色");
            }
        } else {
            // Super admin permissions
            // Cannot demote another super admin unless there are other super admins
            if (targetIsSuperAdmin && !newRoleIsSuperAdmin) {
                // Check if there are other super admins
                long superAdminCount = userMapper.countByRole("super_admin");
                if (superAdminCount <= 1) {
                    return Result.BadRequest("无法取消最后一个超级管理员");
                }
            }
        }

        target.setRole(role);
        userMapper.update(target);
        target.setPassword(null);
        return Result.success(target);
    }

    // ============ Cheaters CRUD ============
    @GetMapping("/cheaters")
    public Result<List<Cheater>> listAllCheaters(@RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return Result.success(cheaterMapper.findAll());
    }

    @PutMapping("/cheaters/{id}/approve")
    public Result<Cheater> approveCheater(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return cheaterService.approveCheater(id);
    }

    @PutMapping("/cheaters/{id}/reject")
    public Result<Cheater> rejectCheater(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        Cheater cheater = cheaterMapper.findById(id);
        if (cheater == null)
            return Result.BadRequest("记录不存在");
        cheaterMapper.updateStatus(id, "rejected");
        cheater.setStatus("rejected");
        return Result.success(cheater);
    }

    @DeleteMapping("/cheaters/{id}")
    public Result<Void> deleteCheater(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        cheaterMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/cheaters/{id}")
    public Result<Cheater> updateCheater(@PathVariable Long id, @RequestBody Cheater body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        Cheater existing = cheaterMapper.findById(id);
        if (existing == null) return Result.BadRequest("记录不存在");
        if (body.getBattleTag() != null) existing.setBattleTag(body.getBattleTag());
        if (body.getCheatType() != null) existing.setCheatType(body.getCheatType());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getStatus() != null) existing.setStatus(body.getStatus());
        if (body.getMmr() != null) existing.setMmr(body.getMmr());
        if (body.getRace() != null) existing.setRace(body.getRace());
        cheaterMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Events CRUD ============
    @GetMapping("/events")
    public Result<List<Event>> listAllEvents(@RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return Result.success(eventMapper.findAll());
    }

    @PutMapping("/events/{id}/approve")
    public Result<Event> approveEvent(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return eventService.approveEvent(id);
    }

    @PutMapping("/events/{id}/reject")
    public Result<Event> rejectEvent(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        Event event = eventMapper.findById(id);
        if (event == null)
            return Result.BadRequest("赛事不存在");
        eventMapper.updateStatus(id, "rejected");
        event.setStatus("rejected");
        return Result.success(event);
    }

    @DeleteMapping("/events/{id}")
    public Result<Void> deleteEvent(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        eventMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/events/{id}")
    public Result<Event> updateEvent(@PathVariable Long id, @RequestBody Event body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        Event existing = eventMapper.findById(id);
        if (existing == null) return Result.BadRequest("赛事不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getRules() != null) existing.setRules(body.getRules());
        if (body.getRewards() != null) existing.setRewards(body.getRewards());
        if (body.getContactLink() != null) existing.setContactLink(body.getContactLink());
        if (body.getGroupLink() != null) existing.setGroupLink(body.getGroupLink());
        if (body.getStatus() != null) existing.setStatus(body.getStatus());
        if (body.getRegion() != null) existing.setRegion(body.getRegion());
        if (body.getStartTime() != null) existing.setStartTime(body.getStartTime());
        eventMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Tutorials CRUD ============
    @GetMapping("/tutorials")
    public Result<List<Tutorial>> listAllTutorials(@RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return Result.success(tutorialMapper.findAll());
    }

    @PostMapping("/tutorials")
    public Result<Tutorial> createTutorial(@RequestBody TutorialDTO dto, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return tutorialService.createTutorial(dto);
    }

    @DeleteMapping("/tutorials/{id}")
    public Result<Void> deleteTutorial(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId))
            return Result.BadRequest("无管理员权限");
        return tutorialService.deleteTutorial(id);
    }

    @PutMapping("/tutorials/{id}")
    public Result<Tutorial> updateTutorial(@PathVariable Long id, @RequestBody Tutorial body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        Tutorial existing = tutorialMapper.findById(id);
        if (existing == null) return Result.BadRequest("教程不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getUrl() != null) existing.setUrl(body.getUrl());
        if (body.getCategory() != null) existing.setCategory(body.getCategory());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getAuthor() != null) existing.setAuthor(body.getAuthor());
        tutorialMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Streams ============
    @GetMapping("/streams")
    public Result<List<Stream>> listAllStreams(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(streamMapper.findAll());
    }

    @DeleteMapping("/streams/{id}")
    public Result<Void> deleteStream(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        streamMapper.deleteById(id);
        
        // Invalidate SC2 streams cache
        try {
            stringRedisTemplate.delete(STREAMS_CACHE_KEY);
        } catch (Exception ignored) {}

        return Result.success();
    }

    @PutMapping("/streams/{id}")
    public Result<Stream> updateStream(@PathVariable Long id, @RequestBody Stream body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        Stream existing = streamMapper.findById(id);
        if (existing == null) return Result.BadRequest("直播不存在");
        if (body.getName() != null) existing.setName(body.getName());
        if (body.getStreamUrl() != null) existing.setStreamUrl(body.getStreamUrl());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getMmr() != null) existing.setMmr(body.getMmr());
        if (body.getRace() != null) existing.setRace(body.getRace());
        if (body.getPlatform() != null) existing.setPlatform(body.getPlatform());
        streamMapper.update(existing);

        // Invalidate SC2 streams cache
        try {
            stringRedisTemplate.delete(STREAMS_CACHE_KEY);
        } catch (Exception ignored) {}

        return Result.success(existing);
    }

    // ============ Clan Recruitments ============
    @GetMapping("/clan-recruitments")
    public Result<List<ClanRecruitment>> listAllClanRecruitments(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(clanRecruitmentMapper.findAll());
    }

    @DeleteMapping("/clan-recruitments/{id}")
    public Result<Void> deleteClanRecruitment(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        clanRecruitmentMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/clan-recruitments/{id}")
    public Result<ClanRecruitment> updateClanRecruitment(@PathVariable Long id, @RequestBody ClanRecruitment body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        ClanRecruitment existing = clanRecruitmentMapper.findById(id);
        if (existing == null) return Result.BadRequest("招募不存在");
        if (body.getClanName() != null) existing.setClanName(body.getClanName());
        if (body.getClanTag() != null) existing.setClanTag(body.getClanTag());
        if (body.getRegion() != null) existing.setRegion(body.getRegion());
        if (body.getMinMmr() != null) existing.setMinMmr(body.getMinMmr());
        if (body.getMaxMmr() != null) existing.setMaxMmr(body.getMaxMmr());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getContact() != null) existing.setContact(body.getContact());
        clanRecruitmentMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Coaching Posts ============
    @GetMapping("/coaching-posts")
    public Result<List<CoachingPost>> listAllCoachingPosts(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(coachingPostMapper.findAll());
    }

    @DeleteMapping("/coaching-posts/{id}")
    public Result<Void> deleteCoachingPost(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        coachingPostMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/coaching-posts/{id}")
    public Result<CoachingPost> updateCoachingPost(@PathVariable Long id, @RequestBody CoachingPost body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        CoachingPost existing = coachingPostMapper.findById(id);
        if (existing == null) return Result.BadRequest("教练帖不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getRace() != null) existing.setRace(body.getRace());
        if (body.getMmr() != null) existing.setMmr(body.getMmr());
        if (body.getPriceInfo() != null) existing.setPriceInfo(body.getPriceInfo());
        if (body.getContact() != null) existing.setContact(body.getContact());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getPostType() != null) existing.setPostType(body.getPostType());
        coachingPostMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Public Reports ============
    @GetMapping("/public-reports")
    public Result<List<PublicReport>> listAllPublicReports(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(publicReportMapper.findAll());
    }

    @DeleteMapping("/public-reports/{id}")
    public Result<Void> deletePublicReport(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        publicReportMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/public-reports/{id}")
    public Result<PublicReport> updatePublicReport(@PathVariable Long id, @RequestBody PublicReport body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        PublicReport existing = publicReportMapper.findById(id);
        if (existing == null) return Result.BadRequest("报告不存在");
        if (body.getGameId() != null) existing.setGameId(body.getGameId());
        if (body.getMmrMin() != null) existing.setMmrMin(body.getMmrMin());
        if (body.getMmrMax() != null) existing.setMmrMax(body.getMmrMax());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        publicReportMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Text Tutorials ============
    @GetMapping("/text-tutorials")
    public Result<List<TextTutorial>> listAllTextTutorials(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(textTutorialMapper.findAll());
    }

    @DeleteMapping("/text-tutorials/{id}")
    public Result<Void> deleteTextTutorial(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        textTutorialMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/text-tutorials/{id}")
    public Result<TextTutorial> updateTextTutorial(@PathVariable Long id, @RequestBody TextTutorial body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        TextTutorial existing = textTutorialMapper.findById(id);
        if (existing == null) return Result.BadRequest("文字教程不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getCategory() != null) existing.setCategory(body.getCategory());
        if (body.getContent() != null) existing.setContent(body.getContent());
        if (body.getAuthorTag() != null) existing.setAuthorTag(body.getAuthorTag());
        textTutorialMapper.update(existing);
        return Result.success(existing);
    }

    // ============ Replays ============
    @GetMapping("/replays")
    public Result<List<ReplayFile>> listAllReplays(@RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        return Result.success(replayFileMapper.findAll());
    }

    @DeleteMapping("/replays/{id}")
    public Result<Void> deleteReplay(@PathVariable Long id, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        replayFileMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/replays/{id}")
    public Result<ReplayFile> updateReplay(@PathVariable Long id, @RequestBody ReplayFile body, @RequestParam Long adminId) {
        if (!isAdmin(adminId)) return Result.BadRequest("无管理员权限");
        ReplayFile existing = replayFileMapper.findById(id);
        if (existing == null) return Result.BadRequest("录像不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getCategory() != null) existing.setCategory(body.getCategory());
        replayFileMapper.update(existing);
        return Result.success(existing);
    }
}
