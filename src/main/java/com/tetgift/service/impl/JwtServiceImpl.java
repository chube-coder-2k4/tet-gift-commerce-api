package com.tetgift.service.impl;

import com.tetgift.enums.TokenType;
import com.tetgift.model.Users;
import com.tetgift.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secretKey}")
    private String secretKey;
    @Value("${jwt.refreshKey}")
    private String refreshKey;
    @Value("${jwt.resetKey}")
    private String resetPasswordKey;
    @Value("${jwt.timeout}")
    private long expirationTime;
    @Value("${jwt.expiryDay}")
    private long expirationDays;


    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        return buildToken(claims, userDetails, TokenType.ACCESS, expirationTime);
    }


    private Key getJwtSecretKey(TokenType tokenType) {
        switch (tokenType) {
            case ACCESS -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
            }
            case REFRESH -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            case RESET_PASSWORD -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(resetPasswordKey));
            }
            default -> throw new IllegalArgumentException("Invalid token type");
        }
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        return buildToken(claims, userDetails, TokenType.REFRESH, expirationDays * 24 * 60 * 60 * 1000);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        final String username = extractUsername(token, tokenType);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, tokenType);
    }

    private boolean isTokenExpired(String token, TokenType tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    public Date extractExpiration(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getExpiration);
    }

    @Override
    public String generateResetPasswordToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        long expirationTimeReset = 10 * 60 * 1000;
        return buildToken(claims, userDetails, TokenType.RESET_PASSWORD, expirationTimeReset);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(getJwtSecretKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token, TokenType tokenType, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    private Map<String, Object> buildClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities()

                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put("username", userDetails.getUsername());
        if(userDetails instanceof Users user){
            claims.put("userId", user.getId().toString());
        }
        return claims;
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, TokenType type, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getJwtSecretKey(type), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public List<String> extractRole(String token, TokenType tokenType) {
        Claims claims = extractAllClaims(token, tokenType);
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof List<?> rolesList && !rolesList.isEmpty()) {
            return rolesList.stream()
                    .map(Object::toString)
                    .toList();
        } else {
            return List.of();
        }
    }

    @Override
    public Long extractUserId(String token, TokenType tokenType) {
        Claims claims = extractAllClaims(token, tokenType);
        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }



}
