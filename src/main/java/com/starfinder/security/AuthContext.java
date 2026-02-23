package com.starfinder.security;

public final class AuthContext {
    private AuthContext() {}

    private static final ThreadLocal<AuthPrincipal> PRINCIPAL = new ThreadLocal<>();

    public static void set(AuthPrincipal principal) {
        PRINCIPAL.set(principal);
    }

    public static AuthPrincipal get() {
        return PRINCIPAL.get();
    }

    public static Long getUserId() {
        AuthPrincipal principal = PRINCIPAL.get();
        return principal == null ? null : principal.userId();
    }

    public static String getRole() {
        AuthPrincipal principal = PRINCIPAL.get();
        return principal == null ? null : principal.role();
    }

    public static void clear() {
        PRINCIPAL.remove();
    }
}
