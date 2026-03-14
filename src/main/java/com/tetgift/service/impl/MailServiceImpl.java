package com.tetgift.service.impl;

import com.tetgift.service.MailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    @Value("${mail.from}")
    private String emailFrom;

    @Override
    public String sendMail(String toWho, String subject, String body, MultipartFile[] files) {
        try {
            log.info("Sending email to: {}...", toWho);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom, "FPT University");
            if(toWho.contains(",")) {
                helper.setTo(InternetAddress.parse(toWho));
            } else {
                helper.setTo(toWho);
            }
            if(files != null) {
                for (MultipartFile file : files) {
                    String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment";
                    helper.addAttachment(filename, file);
                }
            }
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent to: {}", toWho);
            return "sent";
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toWho, e.getMessage(), e);
            return "failed";
        }
    }

    @Override
    public String sendOtpMail(String email, String otp) {
        String subject = "Your OTP Code";
        String body = String.format("Your OTP is: %s (valid for 5 minutes)", otp);
        return sendMail(email, subject, body, null);
    }

    @Override
    public String sendResetPasswordMail(String email, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
                "<p>You requested a password reset.</p>" +
                "<p>Use the following token to reset your password:</p>" +
                "<p><strong>%s</strong></p>" +
                "<p>This token is valid for 10 minutes.</p>" +
                "<p>If you did not request this, please ignore this email.</p>",
                resetToken);
        return sendMail(email, subject, body, null);
    }
}
