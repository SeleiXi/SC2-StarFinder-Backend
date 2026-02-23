package com.starfinder.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow non-API routes (static, etc.)
        if (path == null || !path.startsWith("/api/")) {
            return true;
        }

        // Always exclude H2 console
        if (path.startsWith("/h2-console")) {
            return true;
        }

        boolean isAdminPath = path.startsWith("/api/admin/");
        boolean isApproveAction = path.endsWith("/approve") || path.contains("/approve/");

        // Public endpoints
        if (isPublicEndpoint(method, path)) {
            return true;
        }

        // Read token
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = extractBearer(auth);
        if (token == null) {
            writeJson(response, 401, Map.of("code", 401, "msg", "未登录或登录已过期"));
            return false;
        }

        AuthPrincipal principal;
        try {
            principal = jwtService.verifyToken(token);
        } catch (JWTVerificationException e) {
            writeJson(response, 401, Map.of("code", 401, "msg", "未登录或登录已过期"));
            return false;
        }

        AuthContext.set(principal);

        if (isAdminPath || isApproveAction) {
            if (!principal.isAdmin()) {
                writeJson(response, 403, Map.of("code", 403, "msg", "无权限"));
                return false;
            }
        }

        // For write operations (non-public), require auth
        if (isWriteMethod(method)) {
            return true;
        }

        // For GET requests: allow by default except /api/admin/** (already handled)
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private static boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
    }

    private static boolean isPublicEndpoint(String method, String path) {
        // Auth & email verification
        if ("POST".equalsIgnoreCase(method) && (
                "/api/user/login".equals(path) ||
                "/api/user/register".equals(path) ||
                "/api/user/login/code".equals(path) ||
                "/api/user/password/reset".equals(path) ||
                "/api/email/send".equals(path)
        )) {
            return true;
        }

        // Public read-only APIs
        if ("GET".equalsIgnoreCase(method)) {
            // Admin GET is not public
            return !path.startsWith("/api/admin/");
        }

        return false;
    }

    private static String extractBearer(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) return null;
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static void writeJson(HttpServletResponse response, int status, Map<String, Object> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        MAPPER.writeValue(response.getWriter(), body);
    }
}
