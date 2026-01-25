package com.tetgift.service.impl;

import com.tetgift.model.Users;
import com.tetgift.model.redisModel.OtpVerify;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.repository.redis.OtpVerifyRepository;
import com.tetgift.service.OtpVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpVerifyServiceImpl implements OtpVerifyService {
    private final OtpVerifyRepository otpVerifyRepository;
    private final UserRepository userRepository;



    @Override
    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpVerifyRepository.findById(email).get().getOtp();
        if(!storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        otpVerifyRepository.deleteById(email);
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setVerify(true);
        userRepository.save(user);
        return true;
    }

    @Override
    public void saveOtp(String email, String otp) {
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if(otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
        OtpVerify otpVerify = OtpVerify.builder()
                .email(email)
                .otp(otp)
                .build();
        otpVerifyRepository.save(otpVerify);
    }

    @Override
    public String getOtp(String email) {
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        OtpVerify otpVerify = otpVerifyRepository.findById((email)).orElse(null);
        if(otpVerify == null) {
            throw new IllegalArgumentException("No OTP found for the provided email");
        }
        return otpVerify.getOtp();
    }

    @Override
    public void deleteOtp(String email) {
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        otpVerifyRepository.findById(email).ifPresent(otpVerifyRepository::delete);
    }

    @Override
    public String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    @Override
    public void resendOtp(String email) {
        otpVerifyRepository.findById(email).ifPresent(otpVerifyRepository::delete);
        String newOtp = generateOtp();
        saveOtp(email, newOtp);
    }

}
