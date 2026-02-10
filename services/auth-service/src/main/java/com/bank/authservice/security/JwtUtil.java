package com.bank.authservice.security;

import com.bank.authservice.entity.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret:ChangeMeToASecureSecretKey}")
    private String secret;
    @Value("${security.jwt.expirationMs:36000000}")
    private long expirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserEntity user) {
        String rolesClaim = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(Map.of("roles", rolesClaim))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return (String) extractAllClaims(token).get("roles");
    }

    public boolean validateToken(String token, UserEntity user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    String generateToken(UserEntity user, long expirationMs) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", String.join(",", user.getRoles().stream().map(r -> "ROLE_" + r.name()).toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}