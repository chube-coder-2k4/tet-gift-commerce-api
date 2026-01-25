package com.tetgift.service;


import com.tetgift.dto.request.ChangePasswordRequest;
import com.tetgift.dto.request.LoginRequest;
import com.tetgift.dto.request.ResetPasswordRequest;
import com.tetgift.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(HttpServletRequest request);
    String logout(HttpServletRequest request);
    String forgotPassword(String email);
    String resetPassword(ResetPasswordRequest request);
    String changePassword(ChangePasswordRequest request);
}
