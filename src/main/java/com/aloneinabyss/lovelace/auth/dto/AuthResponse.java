package com.aloneinabyss.lovelace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Response DTO for authentication endpoints (login, register, refresh).
 * The access token is returned in the response body.
 * The refresh token is set as an httpOnly secure cookie (not in response body).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * JWT access token for API authentication
     */
    private String token;
    
    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String type = "Bearer";
    
    /**
     * Username of the authenticated user
     */
    private String username;
    
    /**
     * Email of the authenticated user
     */
    private String email;
    
    /**
     * Roles/authorities of the authenticated user
     */
    private Set<String> roles;

    /**
     * Optional message (used for registration confirmation)
     */
    private String message;
    
}
