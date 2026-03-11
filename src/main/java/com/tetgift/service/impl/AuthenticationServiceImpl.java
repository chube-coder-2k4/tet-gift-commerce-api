package com.tetgift.service.impl;

import com.tetgift.dto.request.ChangePasswordRequest;
import com.tetgift.dto.request.LoginRequest;
import com.tetgift.dto.request.ResetPasswordRequest;
import com.tetgift.dto.response.LoginResponse;
import com.tetgift.enums.TokenType;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.UnauthorizedException;
import com.tetgift.exception.UserNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.redismodel.RefreshToken;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.AuthenticationService;
import com.tetgift.service.JwtService;
import com.tetgift.service.MailService;
import com.tetgift.service.RefreshTokenService;
import com.tetgift.util.AuthenticationUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationUtils utils;
    private final MailService mailService;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }

        var user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new UserNotFoundException("User not found")));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveRefreshToken(RefreshToken.builder()
                .token(refreshToken)
                .usersId(user.getId())
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    @Override
    public LoginResponse refreshToken(HttpServletRequest request) {
        String token = getRefreshToken(request);
        var user = getUserFromRefreshToken(token);
        validateRefreshToken(token, user);

        String accessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(token)
                .userId(user.getId())
                .build();
    }

    @Override
    public String logout(HttpServletRequest request) {
        String refresh = getRefreshToken(request);
        var user = getUserFromRefreshToken(refresh);
        validateRefreshToken(refresh, user);
        tokenService.deleteByUserId(user.getId());
        return "Logout successful";
    }

    @Override
    public String forgotPassword(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new InvalidDataException("User is not enabled");
        }

        String resetToken = jwtService.generateResetPasswordToken(user);
        mailService.sendResetPasswordMail(email, resetToken);
        log.info("Password reset token sent to email: {}", email);

        return "Password reset token has been sent to your email";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        // Validate password match (only once)
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("New password and confirm password do not match");
        }

        final String username = jwtService.extractUsername(request.getToken(), TokenType.RESET_PASSWORD);
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!jwtService.isTokenValid(request.getToken(), user, TokenType.RESET_PASSWORD)) {
            throw new InvalidDataException("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successful for user: {}", username);
        return "Password reset successful";
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        Users user = utils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidDataException("Old password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all refresh tokens for this user after password change
        tokenService.deleteByUserId(user.getId());

        return "Change password successful";
    }

    private String getRefreshToken(HttpServletRequest request) {
        String token = request.getHeader("x-refresh-token");
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Refresh token must not be blank");
        }
        return token;
    }

    private Users getUserFromRefreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void validateRefreshToken(String refreshToken, Users user) {
        if (!jwtService.isTokenValid(refreshToken, user, TokenType.REFRESH)) {
            throw new InvalidDataException("Invalid refresh token");
        }
    }
}
