package br.com.fiap.lovelace_project_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.lovelace_project_api.dto.AuthResponse;
import br.com.fiap.lovelace_project_api.dto.AuthTokens;
import br.com.fiap.lovelace_project_api.dto.ForgotPasswordRequest;
import br.com.fiap.lovelace_project_api.dto.LoginRequest;
import br.com.fiap.lovelace_project_api.dto.MessageResponse;
import br.com.fiap.lovelace_project_api.dto.RefreshTokenRequest;
import br.com.fiap.lovelace_project_api.dto.RegisterRequest;
import br.com.fiap.lovelace_project_api.dto.ResendVerificationRequest;
import br.com.fiap.lovelace_project_api.dto.ResetPasswordRequest;
import br.com.fiap.lovelace_project_api.security.CookieUtil;
import br.com.fiap.lovelace_project_api.service.AuthService;
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
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
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
        long refreshTokenMaxAge = authService.getRefreshTokenExpirationSeconds();
        cookieUtil.addRefreshTokenCookie(response, authTokens.getRefreshToken(), refreshTokenMaxAge);
        
        return ResponseEntity.ok(authTokens.getAuthResponse());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(MessageResponse.builder()
            .message("Email verified successfully! You can now log in.")
            .build());    
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
            .message("Verification email sent!")
            .build());
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Get refresh token from cookie only
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(httpRequest)
                .orElse(null);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                    .message("Refresh token cookie not found or expired")
                    .build());
        }
        
        // Refresh the token
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken);
        AuthTokens authTokens = authService.refreshToken(tokenRequest);
        
        // Set new refresh token as httpOnly secure cookie
        long refreshTokenMaxAge = authService.getRefreshTokenExpirationSeconds();
        cookieUtil.addRefreshTokenCookie(httpResponse, authTokens.getRefreshToken(), refreshTokenMaxAge);
        
        return ResponseEntity.ok(authTokens.getAuthResponse());
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
            .message("If an account exists with that email, a password reset link has been sent.")
            .build());
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
            .message("Password has been reset successfully! You can now log in with your new password.")
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
                .message("No access token provided")
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
            .message("Logged out successfully")
            .build());
    }
    
}
