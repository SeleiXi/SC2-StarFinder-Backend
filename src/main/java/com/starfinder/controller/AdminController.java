package com.starfinder.controller;

import com.starfinder.dto.*;
import com.starfinder.entity.*;
import com.starfinder.mapper.*;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserService userService;
    @Autowired
    private CheaterService cheaterService;
    @Autowired
    private EventService eventService;
    @Autowired
    private TutorialService tutorialService;

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

        if (!isSuperAdmin(adminId)) {
            // Normal admin
            if (targetIsAdmin) {
                return Result.BadRequest("普通管理员不能修改其他管理员的角色");
            }
            if (!"user".equals(role) && !"admin".equals(role)) {
                return Result.BadRequest("普通管理员只能设置 user 或 admin 角色");
            }
            if ("admin".equals(role)) {
                // Admin can promote to admin? "管理员不应该能取消掉自己或者其他人的管理员身份"
                // So admin can promote, but not demote.
            }
        } else {
            // Super admin
            // Can change anyone's role except self (already checked above)
            // But maybe prevent removing the last super admin?
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
}
