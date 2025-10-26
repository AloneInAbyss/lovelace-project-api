package com.aloneinabyss.lovelace.auth.service;

import com.aloneinabyss.lovelace.auth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for validating tokens and checking token-related conditions.
 */
@Service
@Slf4j
public class TokenValidationService {
    
    /**
     * Check if a recent email verification token was generated within the last 5 minutes.
     * Used to prevent spam and excessive token generation requests.
     *
     * @param user The user to check for recent email verification token
     * @return true if a recent token exists, false otherwise
     */
    public boolean hasRecentEmailVerificationToken(User user) {
        if (user.getEmailVerificationToken() == null || user.getEmailVerificationTokenExpiry() == null) {
            return false;
        }
        
        // Check if the token was created within the last 5 minutes
        // by checking if the expiry is more than 23 hours and 55 minutes in the future (token expires in 24 hours)
        return user.getEmailVerificationTokenExpiry().isAfter(LocalDateTime.now().plusHours(23).plusMinutes(55));
    }
    
    /**
     * Check if a recent password reset token was generated within the last 5 minutes.
     * Used to prevent spam and excessive password reset requests.
     *
     * @param user The user to check for recent password reset token
     * @return true if a recent token exists, false otherwise
     */
    public boolean hasRecentPasswordResetToken(User user) {
        if (user.getPasswordResetToken() == null || user.getPasswordResetTokenExpiry() == null) {
            return false;
        }
        
        // Check if the token was created within the last 5 minutes
        // by checking if the expiry is more than 55 minutes in the future (token expires in 1 hour)
        return user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now().plusMinutes(55));
    }
}
