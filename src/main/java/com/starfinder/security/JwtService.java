package com.starfinder.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret:}")
    private String configuredSecret;

    @Value("${app.security.jwt.issuer:starfinder}")
    private String issuer;

    @Value("${app.security.jwt.ttl-seconds:604800}")
    private long ttlSeconds;

    private volatile Algorithm algorithm;
    private volatile JWTVerifier verifier;

    @PostConstruct
    public void init() {
        String secret = configuredSecret;
        if (secret == null || secret.trim().isEmpty() || "CHANGE_ME".equals(secret)) {
            // Generate an ephemeral secret to keep dev environments runnable.
            // In production, set APP_JWT_SECRET / app.security.jwt.secret.
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            secret = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            System.err.println("[SECURITY] app.security.jwt.secret is not set (or is CHANGE_ME). Generated an ephemeral secret; tokens will be invalid after restart. Configure a persistent secret for production.");
        }

        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
    }

    public String createToken(Long userId, String role) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        String safeRole = (role == null || role.isBlank()) ? "user" : role;
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(Math.max(60, ttlSeconds));

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(String.valueOf(userId))
                .withClaim("role", safeRole)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    public AuthPrincipal verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifier.verify(token);
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (Exception e) {
            throw new JWTVerificationException("Invalid subject");
        }
        String role = jwt.getClaim("role").asString();
        return new AuthPrincipal(userId, role);
    }
}
