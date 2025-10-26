package com.aloneinabyss.lovelace.shared.service;

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
    private final MessageService messageService;
    
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
            message.setSubject(messageService.getMessage("email.verification.subject"));
            message.setText(buildVerificationEmailContent(verificationUrl));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException(messageService.getMessage("email.send.failed"), e);
        }
    }
    
    private String buildVerificationEmailContent(String verificationUrl) {
        return messageService.getMessage("email.verification.body", verificationUrl);
    }
    
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.welcome.subject"));
            message.setText(messageService.getMessage("email.welcome.body", username));
            
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
            message.setSubject(messageService.getMessage("email.password.reset.subject"));
            message.setText(buildPasswordResetEmailContent(resetUrl));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException(messageService.getMessage("email.send.failed"), e);
        }
    }
    
    private String buildPasswordResetEmailContent(String resetUrl) {
        return messageService.getMessage("email.password.reset.body", resetUrl);
    }
    
    @Async
    public void sendPasswordChangedEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.password.changed.subject"));
            message.setText(messageService.getMessage("email.password.changed.body", username));
            
            mailSender.send(message);
            log.info("Password changed notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {}", toEmail, e);
        }
    }
    
}