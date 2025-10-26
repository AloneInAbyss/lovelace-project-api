package com.aloneinabyss.lovelace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.client.url}")
    private String clientUrl;
    
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = clientUrl + "/verify-email?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - Lovelace Project");
            message.setText(buildEmailContent(verificationUrl));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    private String buildEmailContent(String verificationUrl) {
        return "Welcome to Lovelace Project!\n\n" +
               "Please click the link below to verify your email address:\n\n" +
               verificationUrl + "\n\n" +
               "This link will expire in 24 hours.\n\n" +
               "If you didn't create an account, please ignore this email.\n\n" +
               "Best regards,\n" +
               "Lovelace Project Team";
    }
    
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Lovelace Project!");
            message.setText("Hello " + username + ",\n\n" +
                    "Your email has been verified successfully!\n\n" +
                    "Welcome to Lovelace Project.\n\n" +
                    "Best regards,\n" +
                    "Lovelace Project Team");
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = clientUrl + "/reset-password?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Lovelace Project");
            message.setText(buildPasswordResetEmailContent(resetUrl));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    private String buildPasswordResetEmailContent(String resetUrl) {
        return "Hello,\n\n" +
               "We received a request to reset your password for your Lovelace Project account.\n\n" +
               "Please click the link below to reset your password:\n\n" +
               resetUrl + "\n\n" +
               "This link will expire in 1 hour.\n\n" +
               "If you didn't request a password reset, please ignore this email and your password will remain unchanged.\n\n" +
               "Best regards,\n" +
               "Lovelace Project Team";
    }
    
    @Async
    public void sendPasswordChangedEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Changed - Lovelace Project");
            message.setText("Hello " + username + ",\n\n" +
                    "Your password has been changed successfully.\n\n" +
                    "If you didn't make this change, please contact our support team immediately.\n\n" +
                    "Best regards,\n" +
                    "Lovelace Project Team");
            
            mailSender.send(message);
            log.info("Password changed notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {}", toEmail, e);
        }
    }
    
}