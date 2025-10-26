package com.aloneinabyss.lovelace.security;

import com.aloneinabyss.lovelace.shared.exception.ErrorCode;
import com.aloneinabyss.lovelace.shared.exception.ErrorResponse;
import com.aloneinabyss.lovelace.shared.service.MessageService;
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
    private final MessageService messageService;
    
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
                    .errorCode(ErrorCode.TOKEN_EXPIRED.name())
                    .message(messageService.getMessage("auth.token.expired"))
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException instanceof MalformedJwtException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode(ErrorCode.TOKEN_INVALID.name())
                    .message(messageService.getMessage("auth.token.invalid"))
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException instanceof SignatureException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode(ErrorCode.TOKEN_INVALID.name())
                    .message(messageService.getMessage("auth.token.invalid"))
                    .path(request.getRequestURI())
                    .build();
        } else if (jwtException != null) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode(ErrorCode.TOKEN_INVALID.name())
                    .message(messageService.getMessage("auth.token.invalid"))
                    .path(request.getRequestURI())
                    .build();
        } else {
            // Generic authentication failure
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .errorCode(ErrorCode.AUTHENTICATION_REQUIRED.name())
                    .message(messageService.getMessage("auth.authentication.required"))
                    .path(request.getRequestURI())
                    .build();
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
