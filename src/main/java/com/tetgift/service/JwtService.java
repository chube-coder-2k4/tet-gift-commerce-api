package com.tetgift.service;

import com.tetgift.enums.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);
    String extractUsername(String token, TokenType tokenType);
    String generateRefreshToken(UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType);
    String generateResetPasswordToken(UserDetails userDetails);
    Date extractExpiration(String token, TokenType tokenType);
    List<String> extractRole(String token, TokenType tokenType);
    Long extractUserId(String token, TokenType tokenType);
}
