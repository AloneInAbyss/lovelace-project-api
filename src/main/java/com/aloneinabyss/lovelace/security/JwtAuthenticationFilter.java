package com.aloneinabyss.lovelace.security;

import com.aloneinabyss.lovelace.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // Skip JWT processing if no Authorization header or not a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract token from Authorization header
            final String jwt = authHeader.substring(7).trim();
            
            // Check if token is blacklisted
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                log.warn("Attempt to use blacklisted token");
                filterChain.doFilter(request, response);
                return;
            }
            
            // Extract claims from token
            final String username = jwtTokenProvider.extractUsername(jwt);
            
            // Authenticate if username is valid and no existing authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from database to get passwordChangedAt timestamp
                UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
                
                // Validate token with password change timestamp check
                if (jwtTokenProvider.validateToken(jwt, userPrincipal, userPrincipal.getPasswordChangedAt())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user: {}", username);
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                }
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            log.debug("Expired token details - Claims: {}", e.getClaims());
            request.setAttribute("jwtException", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
            request.setAttribute("jwtException", e);
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            request.setAttribute("jwtException", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT token is invalid or empty: {}", e.getMessage());
            request.setAttribute("jwtException", e);
        } catch (Exception e) {
            log.error("JWT authentication failed with unexpected error: {}", e.getMessage(), e);
            request.setAttribute("jwtException", e);
        }
        
        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}
