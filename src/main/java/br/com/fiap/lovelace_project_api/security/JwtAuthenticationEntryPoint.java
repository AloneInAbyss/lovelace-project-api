package br.com.fiap.lovelace_project_api.security;

import br.com.fiap.lovelace_project_api.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom authentication entry point for handling JWT authentication failures.
 * Returns structured error responses with error codes for frontend handling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        log.error("Unauthorized access attempt: {}", authException.getMessage());
        
        // Check if there's a JWT exception stored in request attributes
        // (set by JwtAuthenticationFilter when catching JWT exceptions)
        Exception jwtException = (Exception) request.getAttribute("jwtException");
        
        ErrorResponse errorResponse;
        
        if (jwtException instanceof ExpiredJwtException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode("TOKEN_EXPIRED")
                    .message("JWT token has expired. Please refresh your token or login again.")
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException instanceof MalformedJwtException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode("TOKEN_MALFORMED")
                    .message("JWT token is malformed or invalid.")
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException instanceof SignatureException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode("TOKEN_SIGNATURE_INVALID")
                    .message("JWT signature validation failed.")
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException != null) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode("TOKEN_INVALID")
                    .message("JWT token is invalid: " + jwtException.getMessage())
                    .path(request.getRequestURI())
                    .build();
        } else {
            // Generic authentication failure
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode("AUTHENTICATION_REQUIRED")
                    .message("Authentication is required to access this resource.")
                    .path(request.getRequestURI())
                    .build();
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
