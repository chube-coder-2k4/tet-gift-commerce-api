package com.tetgift.service.impl;

import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.redismodel.OtpVerify;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.repository.redis.OtpVerifyRepository;
import com.tetgift.service.OtpVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpVerifyServiceImpl implements OtpVerifyService {
    private final OtpVerifyRepository otpVerifyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        OtpVerify otpVerify = otpVerifyRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found or expired"));

        if (!otpVerify.getOtp().equals(otp)) {
            throw new InvalidDataException("Invalid OTP");
        }

        otpVerifyRepository.deleteById(email);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerify(true);
        userRepository.save(user);

        return true;
    }

    @Override
    public void saveOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("Email cannot be null or empty");
        }
        if (otp == null || otp.trim().isEmpty()) {
            throw new InvalidDataException("OTP cannot be null or empty");
        }

        OtpVerify otpVerify = OtpVerify.builder()
                .email(email)
                .otp(otp)
                .build();
        otpVerifyRepository.save(otpVerify);
    }

    @Override
    public String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    @Override
    public void resendOtp(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.isVerify()) {
            throw new InvalidDataException("User is already verified");
        }
        otpVerifyRepository.findById(email).ifPresent(otpVerifyRepository::delete);
        String newOtp = generateOtp();
        saveOtp(email, newOtp);
    }
}
