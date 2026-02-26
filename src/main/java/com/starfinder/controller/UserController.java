package com.starfinder.controller;

import com.starfinder.dto.ProfileUpdateDTO;
import com.starfinder.dto.AuthResponseDTO;
import com.starfinder.dto.RegisterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.User;
import com.starfinder.mapper.UserMapper;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.security.JwtService;
import com.starfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/uploads/avatars/";
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/register")
    public Result<AuthResponseDTO> createUser(@RequestBody RegisterDTO registerDTO) {
        Result<User> result = userService.createUser(registerDTO);
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            return Result.BadRequest(result != null ? result.getMsg() : "注册失败");
        }
        User user = result.getData();
        String token = jwtService.createToken(user.getId(), user.getRole());
        return Result.success(new AuthResponseDTO(user, token));
    }

    @PostMapping("/login")
    public Result<AuthResponseDTO> login(@RequestBody RegisterDTO loginDTO) {
        Result<User> result = userService.verifyUser(loginDTO);
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            return Result.BadRequest(result != null ? result.getMsg() : "登录失败");
        }
        User user = result.getData();
        String token = jwtService.createToken(user.getId(), user.getRole());
        return Result.success(new AuthResponseDTO(user, token));
    }

    @PostMapping("/login/code")
    public Result<AuthResponseDTO> loginByCode(@RequestParam String email, @RequestParam String code) {
        Result<User> result = userService.verifyUserByCode(email, code);
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            return Result.BadRequest(result != null ? result.getMsg() : "登录失败");
        }
        User user = result.getData();
        String token = jwtService.createToken(user.getId(), user.getRole());
        return Result.success(new AuthResponseDTO(user, token));
    }

    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword) {
        return userService.resetPassword(email, code, newPassword);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/profile")
    public Result<User> updateProfile(@PathVariable Long id, @RequestBody ProfileUpdateDTO dto) {
        return userService.updateProfile(id, dto);
    }

    @PostMapping("/{id}/avatar")
    public Result<String> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (!principal.userId().equals(id)) return Result.BadRequest("无权限");

        if (file == null || file.isEmpty()) return Result.BadRequest("文件不能为空");
        if (file.getSize() > MAX_AVATAR_SIZE) return Result.BadRequest("文件大小不能超过2MB");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) return Result.BadRequest("只支持图片文件");

        String origName = file.getOriginalFilename();
        String ext = (origName != null && origName.contains(".")) ? origName.substring(origName.lastIndexOf(".")).toLowerCase() : ".jpg";
        if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
            return Result.BadRequest("只支持 jpg/png/gif/webp 格式");
        }

        String fileName = "avatar_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            return Result.BadRequest("文件保存失败: " + e.getMessage());
        }

        String avatarUrl = "/uploads/avatars/" + fileName;
        // Update user's avatar field
        User user = userMapper.findById(id);
        if (user != null) {
            user.setAvatar(avatarUrl);
            userMapper.update(user);
        }
        return Result.success(avatarUrl);
    }

    @PutMapping("/{id}/avatar/preset")
    public Result<String> setPresetAvatar(@PathVariable Long id, @RequestParam String preset) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (!principal.userId().equals(id)) return Result.BadRequest("无权限");
        String avatarUrl = "preset:" + preset;
        User user = userMapper.findById(id);
        if (user == null) return Result.BadRequest("用户不存在");
        user.setAvatar(avatarUrl);
        userMapper.update(user);
        return Result.success(avatarUrl);
    }

    @GetMapping("/match")
    public List<User> findMatches(@RequestParam int mmr, @RequestParam int range,
            @RequestParam(required = false) String race,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) Integer minLevel) {
        return userService.findMatches(mmr, range, race, mode, minLevel);
    }
}