package com.starfinder.security;

public record AuthPrincipal(Long userId, String role) {
    public boolean isAdmin() {
        return role != null && ("admin".equals(role) || "super_admin".equals(role));
    }

    public boolean isSuperAdmin() {
        return "super_admin".equals(role);
    }
}
