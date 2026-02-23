package com.starfinder.dto;

import com.starfinder.entity.User;

public class AuthResponseDTO {
    private User user;
    private String token;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
