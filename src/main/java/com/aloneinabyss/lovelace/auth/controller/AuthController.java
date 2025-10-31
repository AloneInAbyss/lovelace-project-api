package com.aloneinabyss.lovelace.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aloneinabyss.lovelace.auth.dto.AuthResponse;
import com.aloneinabyss.lovelace.auth.dto.AuthTokens;
import com.aloneinabyss.lovelace.auth.dto.ChangePasswordRequest;
import com.aloneinabyss.lovelace.auth.dto.ForgotPasswordRequest;
import com.aloneinabyss.lovelace.auth.dto.LoginRequest;
import com.aloneinabyss.lovelace.auth.dto.MessageResponse;
import com.aloneinabyss.lovelace.auth.dto.RefreshTokenRequest;
import com.aloneinabyss.lovelace.auth.dto.RegisterRequest;
import com.aloneinabyss.lovelace.auth.dto.RegisterResponse;
import com.aloneinabyss.lovelace.auth.dto.ResendVerificationRequest;
import com.aloneinabyss.lovelace.auth.dto.ResetPasswordRequest;
import com.aloneinabyss.lovelace.auth.service.AuthService;
import com.aloneinabyss.lovelace.config.JwtProperties;
import com.aloneinabyss.lovelace.security.CookieUtil;
import com.aloneinabyss.lovelace.security.SecurityUtils;
import com.aloneinabyss.lovelace.shared.exception.AuthenticationException;
import com.aloneinabyss.lovelace.shared.exception.ErrorCode;
import com.aloneinabyss.lovelace.shared.service.MessageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final JwtProperties jwtProperties;
    private final MessageService messageService;
    
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokens authTokens = authService.login(request);
        
        // Set refresh token as httpOnly secure cookie
        // Get refresh token expiration from JWT properties (convert ms to seconds)
        long refreshTokenMaxAge = jwtProperties.getRefreshTokenExpirationSeconds();
        cookieUtil.addRefreshTokenCookie(response, authTokens.getRefreshToken(), refreshTokenMaxAge);
        
        return ResponseEntity.ok(authTokens.getAuthResponse());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.email.verified"))
            .build());    
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.email.verification.sent"))
            .build());
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Get refresh token from cookie only
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(httpRequest)
                .orElseThrow(() -> new AuthenticationException(
                    ErrorCode.REFRESH_TOKEN_MISSING
                ));
        
        // Refresh the token
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken);
        AuthTokens authTokens = authService.refreshToken(tokenRequest);
        
        // Set new refresh token as httpOnly secure cookie
        long refreshTokenMaxAge = jwtProperties.getRefreshTokenExpirationSeconds();
        cookieUtil.addRefreshTokenCookie(httpResponse, authTokens.getRefreshToken(), refreshTokenMaxAge);
        
        return ResponseEntity.ok(authTokens.getAuthResponse());
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.password.reset.sent"))
            .build());
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.password.reset.success"))
            .build());
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse httpResponse
    ) {
        // Get the authenticated username from SecurityContext
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new AuthenticationException(
                ErrorCode.AUTHENTICATION_REQUIRED
            );
        }
        
        // Change the password
        authService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
        
        // Delete the refresh token cookie
        cookieUtil.deleteRefreshTokenCookie(httpResponse);
        
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.password.change.success"))
            .build());
    }
    
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Extract access token from Authorization header
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7).trim();
        }
        
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(MessageResponse.builder()
                .message(messageService.getMessage("auth.logout.no.token"))
                .build());
        }
        
        // Get refresh token from cookie only
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(httpRequest)
                .orElse(null);
        
        // Logout and blacklist tokens
        authService.logout(accessToken, refreshToken);
        
        // Delete the refresh token cookie
        cookieUtil.deleteRefreshTokenCookie(httpResponse);
        
        return ResponseEntity.ok(MessageResponse.builder()
            .message(messageService.getMessage("auth.logout.success"))
            .build());
    }
    
}
