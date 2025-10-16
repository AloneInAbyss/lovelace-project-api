package br.com.fiap.lovelace_project_api.service;

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
    
    @Value("${app.base.url}")
    private String baseUrl;
    
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
            
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
    
}