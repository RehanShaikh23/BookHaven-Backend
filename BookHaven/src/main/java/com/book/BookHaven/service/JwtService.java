package com.book.BookHaven.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.rememberMe.expiration}")
    private long rememberMeExpiration;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of(role));
        long exp = rememberMe ? rememberMeExpiration : expiration;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractUsername(token);
            return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}