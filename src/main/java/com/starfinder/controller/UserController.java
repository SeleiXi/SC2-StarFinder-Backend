package com.starfinder.controller;

import com.starfinder.dto.ProfileUpdateDTO;
import com.starfinder.dto.RegisterDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.User;
import com.starfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> createUser(@RequestBody RegisterDTO registerDTO) {
        return userService.createUser(registerDTO);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody RegisterDTO loginDTO) {
        return userService.verifyUser(loginDTO);
    }

    @PostMapping("/login/code")
    public Result<User> loginByCode(@RequestParam String email, @RequestParam String code) {
        return userService.verifyUserByCode(email, code);
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
            @RequestParam(required = false) String race) {
        return userService.findMatches(mmr, range, race);
    }
}