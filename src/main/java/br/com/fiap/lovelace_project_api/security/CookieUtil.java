package br.com.fiap.lovelace_project_api.security;

import br.com.fiap.lovelace_project_api.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for managing JWT refresh token cookies.
 * Handles creation and deletion of httpOnly, secure cookies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtil {
    
    private final JwtProperties jwtProperties;
    
    /**
     * Create a refresh token cookie with httpOnly and secure flags.
     *
     * @param refreshToken The refresh token value
     * @param maxAge Maximum age in seconds
     * @return ResponseCookie configured with security settings
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAge) {
        JwtProperties.Cookie cookieConfig = jwtProperties.getCookie();
        
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(cookieConfig.getName(), refreshToken)
                .httpOnly(true)  // Prevents XSS attacks
                .secure(cookieConfig.isSecure())  // Only sent over HTTPS in production
                .path(cookieConfig.getPath())
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite(cookieConfig.getSameSite());
        
        // Add domain if configured
        if (cookieConfig.getDomain() != null && !cookieConfig.getDomain().isEmpty()) {
            cookieBuilder.domain(cookieConfig.getDomain());
        }
        
        ResponseCookie cookie = cookieBuilder.build();
        
        log.debug("Created refresh token cookie: name={}, path={}, secure={}, sameSite={}, maxAge={}s",
                cookieConfig.getName(), cookieConfig.getPath(), cookieConfig.isSecure(),
                cookieConfig.getSameSite(), maxAge);
        
        return cookie;
    }
    
    /**
     * Create a cookie to clear/delete the refresh token.
     *
     * @return ResponseCookie configured to delete the refresh token cookie
     */
    public ResponseCookie createDeleteRefreshTokenCookie() {
        JwtProperties.Cookie cookieConfig = jwtProperties.getCookie();
        
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(cookieConfig.getName(), "")
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path(cookieConfig.getPath())
                .maxAge(0)  // Expire immediately
                .sameSite(cookieConfig.getSameSite());
        
        // Add domain if configured
        if (cookieConfig.getDomain() != null && !cookieConfig.getDomain().isEmpty()) {
            cookieBuilder.domain(cookieConfig.getDomain());
        }
        
        ResponseCookie cookie = cookieBuilder.build();
        
        log.debug("Created delete cookie for refresh token: name={}", cookieConfig.getName());
        
        return cookie;
    }
    
    /**
     * Add a refresh token cookie to the HTTP response.
     *
     * @param response HTTP response
     * @param refreshToken The refresh token value
     * @param maxAge Maximum age in seconds
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAge) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken, maxAge);
        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Added refresh token cookie to response");
    }
    
    /**
     * Delete the refresh token cookie from the HTTP response.
     *
     * @param response HTTP response
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createDeleteRefreshTokenCookie();
        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Deleted refresh token cookie from response");
    }
    
    /**
     * Extract the refresh token from the request cookies.
     *
     * @param request HTTP request
     * @return Optional containing the refresh token if found
     */
    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies == null) {
            log.debug("No cookies found in request");
            return Optional.empty();
        }
        
        String cookieName = jwtProperties.getCookie().getName();
        
        Optional<String> refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
        
        if (refreshToken.isPresent()) {
            log.debug("Found refresh token in cookie: {}", cookieName);
        } else {
            log.debug("Refresh token cookie not found: {}", cookieName);
        }
        
        return refreshToken;
    }
}
