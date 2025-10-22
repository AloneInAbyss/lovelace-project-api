package br.com.fiap.lovelace_project_api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utility to extract JWT claims from Authorization header without performing full authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtTokenProvider jwtTokenProvider;

    public String extractUserIdFromRequest(@NonNull HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        try {
            return jwtTokenProvider.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
