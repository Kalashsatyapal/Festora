package com.festora_backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${festora.frontend.url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    public void sendResetEmail(String email, String fullName, String token) {
        String resetUrl = frontendUrl
                + "/reset-password?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Reset your Festora password");
        message.setText(
                "Hello " + fullName + ",\n\n"
                        + "We received a request to reset your Festora password.\n\n"
                        + "Use the link below to choose a new password:\n"
                        + resetUrl + "\n\n"
                        + "This link expires in 15 minutes.\n\n"
                        + "If you did not request this, you can ignore this email.\n\n"
                        + "Festora Support"
        );

        mailSender.send(message);
    }
}