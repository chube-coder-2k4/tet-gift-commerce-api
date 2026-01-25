package com.tetgift.controller;


import com.tetgift.dto.request.*;
import com.tetgift.dto.response.ApiResponse;
import com.tetgift.dto.response.LoginResponse;
import com.tetgift.service.AuthenticationService;
import com.tetgift.service.OtpVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final OtpVerifyService otpVerifyService;

    @Operation(summary = "User login", description = "Authenticate user and return access token and refresh token")
    @PostMapping("/login")
    public ApiResponse login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(authenticationService.login(request))
                .build();
    }

    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @PostMapping("/refresh-token")
    public ApiResponse refreshToken(
            @Parameter(description = "HTTP request containing x-refresh-token header", hidden = true)
            HttpServletRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Access token refreshed successfully")
                .data(authenticationService.refreshToken(request))
                .build();
    }

    @Operation(summary = "User logout", description = "Invalidate refresh token and logout user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse logout(
            @Parameter(description = "HTTP request containing x-refresh-token header", hidden = true)
            HttpServletRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(authenticationService.logout(request))
                .build();
    }

    @Operation(summary = "Forgot password", description = "Send password reset token to user's email")
    @PostMapping("/forgot-password")
    public ApiResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Password reset token sent to email if it exists")
                .data(authenticationService.forgotPassword(request.getEmail()))
                .build();
    }

    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @PostMapping("/reset-password")
    public ApiResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Password has been reset successfully")
                .data(authenticationService.resetPassword(request))
                .build();
    }

    @Operation(summary = "Change password", description = "Change user password using secret key (reset token)")
    @PostMapping("/change-password")
    public ApiResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Password has been changed successfully")
                .data(authenticationService.changePassword(request))
                .build();
    }

    @Operation(summary = "Verify OTP for email", description = "Verify the OTP sent to user's email for account activation")
    @PostMapping("/verify-otp")
    public ApiResponse verifyOtp(@Valid @RequestBody VerifyRequest request) {
        otpVerifyService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("OTP verified successfully")
                .build();
    }

    @Operation(summary = "Resend OTP for email", description = "Resend the OTP to user's email for account activation")
    @PostMapping("/resend-otp")
    public ApiResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        otpVerifyService.resendOtp(request.getEmail());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("OTP resent successfully if email exists")
                .build();
    }

}
