package com.starfinder.controller;

import com.starfinder.dto.ProfileUpdateDTO;
import com.starfinder.dto.AuthResponseDTO;
import com.starfinder.dto.RegisterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.User;
import com.starfinder.security.JwtService;
import com.starfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

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

    @GetMapping("/match")
    public List<User> findMatches(@RequestParam int mmr, @RequestParam int range,
            @RequestParam(required = false) String race,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) Integer minLevel) {
        return userService.findMatches(mmr, range, race, mode, minLevel);
    }
}