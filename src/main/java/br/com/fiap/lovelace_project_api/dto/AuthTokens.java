package br.com.fiap.lovelace_project_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO that includes both the AuthResponse and the refresh token.
 * Used internally by the service layer to return both tokens,
 * then the controller extracts the refresh token to set as cookie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokens {
    
    /**
     * The public auth response (includes access token, user info)
     */
    private AuthResponse authResponse;
    
    /**
     * The refresh token (to be set as httpOnly cookie)
     */
    private String refreshToken;
    
}
