package com.aloneinabyss.lovelace.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

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
    
    public void sendVerificationEmail(String toEmail, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        sendVerificationEmailAsync(toEmail, token, locale);
    }
    
    @Async
    private void sendVerificationEmailAsync(String toEmail, String token, Locale locale) {
        try {
            String verificationUrl = clientUrl + "/verify-email?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.verification.subject", locale));
            message.setText(messageService.getMessage("email.verification.body", locale, verificationUrl));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException(messageService.getMessage("email.send.failed", locale), e);
        }
    }
    
    public void sendWelcomeEmail(String toEmail, String username) {
        Locale locale = LocaleContextHolder.getLocale();
        sendWelcomeEmailAsync(toEmail, username, locale);
    }
    
    @Async
    private void sendWelcomeEmailAsync(String toEmail, String username, Locale locale) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.welcome.subject", locale));
            message.setText(messageService.getMessage("email.welcome.body", locale, username));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        sendPasswordResetEmailAsync(toEmail, token, locale);
    }
    
    @Async
    private void sendPasswordResetEmailAsync(String toEmail, String token, Locale locale) {
        try {
            String resetUrl = clientUrl + "/reset-password?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.password.reset.subject", locale));
            message.setText(messageService.getMessage("email.password.reset.body", locale, resetUrl));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException(messageService.getMessage("email.send.failed", locale), e);
        }
    }
    
    public void sendPasswordChangedEmail(String toEmail, String username) {
        Locale locale = LocaleContextHolder.getLocale();
        sendPasswordChangedEmailAsync(toEmail, username, locale);
    }
    
    @Async
    private void sendPasswordChangedEmailAsync(String toEmail, String username, Locale locale) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageService.getMessage("email.password.changed.subject", locale));
            message.setText(messageService.getMessage("email.password.changed.body", locale, username));
            
            mailSender.send(message);
            log.info("Password changed notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {}", toEmail, e);
        }
    }
    
}
