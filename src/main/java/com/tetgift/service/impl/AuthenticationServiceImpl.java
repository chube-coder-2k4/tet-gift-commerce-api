package com.tetgift.service.impl;


import com.tetgift.dto.request.ChangePasswordRequest;
import com.tetgift.dto.request.LoginRequest;
import com.tetgift.dto.request.ResetPasswordRequest;
import com.tetgift.dto.response.LoginResponse;
import com.tetgift.enums.TokenType;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.UserNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.redisModel.RefreshToken;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.AuthenticationService;
import com.tetgift.service.JwtService;
import com.tetgift.service.RefreshTokenService;
import com.tetgift.util.AuthenticationUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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


    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsernameOrEmail()).orElseThrow(() -> new UserNotFoundException("user.not.found"));
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
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("user.not.found"));
        if(!user.isEnabled()) {
            throw new InvalidDataException("user.account.not.activated");
        }

        String resetToken = jwtService.generateResetPasswordToken(user);
        
        log.info("Password reset token generated for user: {}", email);
        log.info("Reset token: {}", resetToken);

        return resetToken;
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        if(!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("new.password.confirm.password.not.match");
        }
        
        final String username = jwtService.extractUsername(request.getToken(), TokenType.RESET_PASSWORD);
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("user.not.found"));
        
        if (!jwtService.isTokenValid(request.getToken(), user, TokenType.RESET_PASSWORD)) {
            throw new InvalidDataException("invalid.or.expired.reset.password.token");
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("new.password.confirm.password.not.match");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password reset successful for user: {}", username);
        return "password.reset.success";
    }

    @Override
    public String changePassword(ChangePasswordRequest request) {
        Users user = utils.getCurrentUser();
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidDataException("Old password is incorrect");
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("New password and confirm password do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Change password successful";
    }

    private String getRefreshToken(HttpServletRequest request) {
        String token = request.getHeader("x-refresh-token");
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Invalid refresh token must be not blank");
        }
        return token;
    }

    private Users getUserFromRefreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("user.not.found"));
    }

    private void validateRefreshToken(String refreshToken, Users user) {
        if (!jwtService.isTokenValid(refreshToken, user, TokenType.REFRESH)) {
            throw new InvalidDataException("Invalid refresh token");
        }
    }




}
