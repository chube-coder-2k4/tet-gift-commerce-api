package com.tetgift.controller;


import com.tetgift.dto.request.*;
import com.tetgift.dto.response.LoginResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.AuthenticationService;
import com.tetgift.service.OtpVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<ResponseData<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Login successful",
                authenticationService.login(request)
        ));
    }

    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseData<LoginResponse>> refreshToken(
            @Parameter(description = "HTTP request containing x-refresh-token header", hidden = true)
            HttpServletRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Token refreshed successfully",
                authenticationService.refreshToken(request)
        ));
    }

    @Operation(summary = "User logout", description = "Invalidate refresh token and logout user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ResponseData<String>> logout(
            @Parameter(description = "HTTP request containing x-refresh-token header", hidden = true)
            HttpServletRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Logout successful",
                authenticationService.logout(request)
        ));
    }

    @Operation(summary = "Forgot password", description = "Send password reset token to user's email")
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseData<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "If the email is registered, a password reset token has been sent",
                authenticationService.forgotPassword(request.getEmail())
        ));
    }

    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseData<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Password reset successfully",
                authenticationService.resetPassword(request)
        ));
    }

    @Operation(summary = "Change password", description = "Change user password using secret key (reset token)")
    @PostMapping("/change-password")
    public ResponseEntity<ResponseData<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Password changed successfully",
                authenticationService.changePassword(request)

        ));
    }

    @Operation(summary = "Verify OTP for email", description = "Verify the OTP sent to user's email for account activation")
    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseData<String>> verifyOtp(@Valid @RequestBody VerifyRequest request) {
        otpVerifyService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "OTP verified successfully",
                null
        ));
    }

    @Operation(summary = "Resend OTP for email", description = "Resend the OTP to user's email for account activation")

    @PostMapping("/resend-otp")
    public ResponseEntity<ResponseData<String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        otpVerifyService.resendOtp(request.getEmail());
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "OTP resent successfully if the email is registered and not yet verified",
                null
        ));
    }

}
