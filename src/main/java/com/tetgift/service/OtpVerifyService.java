package com.tetgift.service;

public interface OtpVerifyService {
    boolean verifyOtp(String email, String otp);
    void saveOtp(String email, String otp);
    String getOtp(String email);
    void deleteOtp(String email);
    String generateOtp();
    void resendOtp(String email);
}
