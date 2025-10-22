package br.com.fiap.lovelace_project_api.security;

import br.com.fiap.lovelace_project_api.service.TokenBlacklistService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    
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
            final String userId = jwtTokenProvider.extractUserId(jwt);
            final List<String> roles = jwtTokenProvider.extractRoles(jwt);
            
            // Authenticate if username is valid and no existing authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create authorities from roles in JWT
                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                // Create UserPrincipal with claims from token
                UserPrincipal userPrincipal = new UserPrincipal(
                        userId,
                        username,
                        null, // email not needed for authentication
                        null, // password not needed for authentication
                        authorities,
                        true  // enabled (token wouldn't be valid if user was disabled)
                );
                
                // Validate token signature and expiration
                if (jwtTokenProvider.validateToken(jwt, userPrincipal)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user from JWT claims: {}", username);
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
