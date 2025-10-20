package br.com.fiap.lovelace_project_api.security;

import br.com.fiap.lovelace_project_api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
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
