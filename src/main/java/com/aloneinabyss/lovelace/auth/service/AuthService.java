package com.aloneinabyss.lovelace.auth.service;

import com.aloneinabyss.lovelace.auth.dto.AuthResponse;
import com.aloneinabyss.lovelace.auth.dto.AuthTokens;
import com.aloneinabyss.lovelace.auth.dto.LoginRequest;
import com.aloneinabyss.lovelace.auth.dto.RefreshTokenRequest;
import com.aloneinabyss.lovelace.auth.dto.RegisterRequest;
import com.aloneinabyss.lovelace.auth.dto.RegisterResponse;
import com.aloneinabyss.lovelace.auth.exception.EmailNotVerifiedException;
import com.aloneinabyss.lovelace.auth.exception.ForgotPasswordMailPending;
import com.aloneinabyss.lovelace.auth.exception.TokenReuseException;
import com.aloneinabyss.lovelace.auth.model.User;
import com.aloneinabyss.lovelace.auth.repository.UserRepository;
import com.aloneinabyss.lovelace.config.JwtProperties;
import com.aloneinabyss.lovelace.security.JwtTokenProvider;
import com.aloneinabyss.lovelace.security.UserPrincipal;
import com.aloneinabyss.lovelace.security.service.TokenBlacklistService;
import com.aloneinabyss.lovelace.shared.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;
    
    /**
     * Get the refresh token expiration time in seconds.
     * Used for setting cookie max-age.
     *
     * @return Refresh token expiration in seconds
     */
    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshExpiration() / 1000;
    }
    
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        String verificationToken = UUID.randomUUID().toString();
        
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("ROLE_USER"))
                .enabled(false) // User must verify email to enable account
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(now.plusHours(24))
                .passwordChangedAt(now) // Set initial password change timestamp
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        return RegisterResponse.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("User registered successfully. Please check your email for verification instructions.")
                .build();
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email has already been verified");
        }

        if (user.getEmailVerificationTokenExpiry() == null || 
            user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        if (checkIfRecentTokenExists(user)) {
            throw new RuntimeException(
                "There is a pending verification email. " +
                "If you didn't receive it, you can request a new one in a few minutes."
            );
        }
        
        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    private boolean checkIfRecentTokenExists(User user) {
        if (user.getEmailVerificationToken() == null || user.getEmailVerificationTokenExpiry() == null) {
            return false;
        }
        
        // Check if token was created within last 5 minutes by checking if it's still far from expiry
        // Token expires in 24 hours, so if it has more than 23 hours and 55 minutes left, it's recent
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = user.getEmailVerificationTokenExpiry();
        
        if (expiryDate.isBefore(now)) {
            // Token has already expired
            return false;
        }
        
        long minutesUntilExpiry = ChronoUnit.MINUTES.between(now, expiryDate);
        return minutesUntilExpiry > (24 * 60 - 5); // More than 23:55 remaining means created within last 5 minutes
    }
    
    public AuthTokens login(LoginRequest request) {
        // Try to find user by username or email
        User user = userRepository.findByUsername(request.getIdentity())
            .or(() -> userRepository.findByEmail(request.getIdentity()))
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEmailVerified()) {
            if (checkIfRecentTokenExists(user)) {
                throw new EmailNotVerifiedException(
                    "Email not verified. Please check your inbox for the verification email. " +
                    "If you didn't receive it, you can request a new one in a few minutes."
                );
            } else {
                resendVerificationEmail(user.getEmail());
                throw new EmailNotVerifiedException(
                    "Email not verified. A new verification email has been sent to you."
                );
            }
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(), // Always use username for authentication
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwt)
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(userPrincipal.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet()))
                .build();
        
        return AuthTokens.builder()
                .authResponse(authResponse)
                .refreshToken(refreshToken)
                .build();
    }
    
    public AuthTokens refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Check if the refresh token has been blacklisted (already used or revoked)
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            // This is a security breach - someone is trying to reuse an old refresh token
            // This could indicate token theft
            try {
                String username = jwtTokenProvider.extractUsername(refreshToken);
                log.error("SECURITY ALERT: Attempt to reuse blacklisted refresh token for user: {}", username);
                
                // Optional: Invalidate all tokens for this user by forcing re-login
                // For now, we'll just reject the request
                throw new TokenReuseException("Refresh token has been revoked. Please login again.");
            } catch (TokenReuseException e) {
                // Re-throw TokenReuseException
                throw e;
            } catch (Exception e) {
                log.error("Failed to extract username from blacklisted token: {}", e.getMessage());
                throw new TokenReuseException("Invalid refresh token. Please login again.");
            }
        }
        
        // Extract username from refresh token
        String username = jwtTokenProvider.extractUsername(refreshToken);
        
        // Load user details
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        
        // Validate the refresh token with password change timestamp check
        if (!jwtTokenProvider.validateToken(refreshToken, userPrincipal, userPrincipal.getPasswordChangedAt())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        // Blacklist the old refresh token immediately (rotation)
        // This ensures one-time use of refresh tokens
        tokenBlacklistService.blacklistToken(refreshToken);
        log.debug("Old refresh token blacklisted for user: {}", username);
        
        // Generate new access token and refresh token
        String newAccessToken = jwtTokenProvider.generateToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        
        log.info("Token refresh successful for user: {}", username);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(newAccessToken)
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(userPrincipal.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet()))
                .build();
        
        return AuthTokens.builder()
                .authResponse(authResponse)
                .refreshToken(newRefreshToken)
                .build();
    }
    
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Check if there's a recent password reset request (within 5 minutes)
        if (user.getPasswordResetTokenExpiry() != null && 
            user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now().minusMinutes(5))) {
            throw new ForgotPasswordMailPending(
                "A password reset email was recently sent. Please check your inbox or wait a few minutes before requesting another one."
            );
        }
        
        // Generate password reset token
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }
    
    public void resetPassword(String token, String newPassword) {
        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        
        // Check if token is expired
        if (user.getPasswordResetTokenExpiry() == null || 
            user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired");
        }
        
        // Validate that new password is different from the current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Update password and set passwordChangedAt timestamp
        // This will invalidate all existing JWT tokens issued before this moment
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setPasswordChangedAt(now);
        user.setUpdatedAt(now);
        
        userRepository.save(user);
        
        log.info("Password reset successful for user: {}. All existing tokens invalidated.", user.getUsername());
        
        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }
    
    /**
     * Change password for an authenticated user.
     * Validates current password, updates to new password, and invalidates all existing tokens.
     *
     * @param username The username of the authenticated user
     * @param currentPassword The current password for verification
     * @param newPassword The new password
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate that new password is different from the current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Update password and set passwordChangedAt timestamp
        // This will invalidate all existing JWT tokens issued before this moment
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(now);
        user.setUpdatedAt(now);
        
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}. All existing tokens invalidated.", user.getUsername());
        
        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }
    
    /**
     * Logout a user by blacklisting their access token and optionally their refresh token.
     * Blacklisted tokens cannot be used for authentication until they naturally expire.
     *
     * @param accessToken The access token to blacklist
     * @param refreshToken The refresh token to blacklist (optional)
     */
    public void logout(String accessToken, String refreshToken) {
        // Blacklist the access token
        tokenBlacklistService.blacklistToken(accessToken);
        
        // Blacklist the refresh token if provided
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                tokenBlacklistService.blacklistToken(refreshToken);
            } catch (Exception e) {
                // Log but don't fail - access token is already blacklisted
                // which is the primary security concern
                log.warn("Failed to blacklist refresh token: {}", e.getMessage());
            }
        }
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
}
