package com.aloneinabyss.lovelace.security;

import com.aloneinabyss.lovelace.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtProperties jwtProperties;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    /**
     * Extract the issued-at timestamp from a JWT token as LocalDateTime.
     *
     * @param token The JWT token
     * @return The issued-at timestamp as LocalDateTime
     */
    public LocalDateTime extractIssuedAtAsLocalDateTime(String token) {
        Date issuedAt = extractIssuedAt(token);
        return LocalDateTime.ofInstant(issuedAt.toInstant(), ZoneId.systemDefault());
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Validates a JWT token against user details and password change timestamp.
     * This method ensures that tokens issued before a password change are invalidated.
     *
     * @param token The JWT token to validate
     * @param userDetails The user details to validate against
     * @param passwordChangedAt The timestamp when the password was last changed
     * @return true if token is valid and was issued after the password change
     */
    public Boolean validateToken(String token, UserDetails userDetails, LocalDateTime passwordChangedAt) {
        final String username = extractUsername(token);
        
        // Basic validation
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            return false;
        }
        
        // Check if token was issued before password change
        if (passwordChangedAt != null) {
            LocalDateTime tokenIssuedAt = extractIssuedAtAsLocalDateTime(token);
            // Token is invalid if it was issued before the password was changed
            if (tokenIssuedAt.isBefore(passwordChangedAt)) {
                return false;
            }
        }
        
        return true;
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        return createToken(claims, userDetails.getUsername(), jwtProperties.getExpiration());
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        return createToken(claims, userDetails.getUsername(), jwtProperties.getRefreshExpiration());
    }
    
    /**
     * Build claims map with userId and roles from UserDetails.
     * This allows for authorization checks without database lookups.
     *
     * @param userDetails The user details containing userId and roles
     * @return Map of claims to be included in the JWT
     */
    private Map<String, Object> buildClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add userId if UserPrincipal (which has the id field)
        if (userDetails instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            claims.put("userId", userPrincipal.getId());
        }
        
        // Add roles/authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        
        return claims;
    }
    
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }
    
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
