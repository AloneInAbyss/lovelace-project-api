package br.com.fiap.lovelace_project_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    
    /**
     * Optional refresh token to blacklist along with the access token.
     * If not provided, only the access token (from Authorization header) will be blacklisted.
     */
    private String refreshToken;
}
