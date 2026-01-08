package com.tetgift.service;

import org.springframework.web.multipart.MultipartFile;

public interface MailService {
    String sendMail(String toWho, String subject, String body, MultipartFile[] files);
    String sendOtpMail(String email, String otp);
}
