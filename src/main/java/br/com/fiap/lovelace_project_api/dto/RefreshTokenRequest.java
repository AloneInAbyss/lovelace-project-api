package br.com.fiap.lovelace_project_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal request DTO for refreshing access tokens.
 * The refresh token is extracted from httpOnly cookie by the controller
 * and wrapped in this DTO for internal service calls.
 * This DTO is not used in the public API request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
}
