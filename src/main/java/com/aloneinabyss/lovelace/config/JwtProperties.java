package com.aloneinabyss.lovelace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private String secret;
    
    private Long expiration;
    
    private Long refreshExpiration;
    
    /**
     * Cookie configuration for refresh tokens
     */
    private Cookie cookie = new Cookie();
    
    @Data
    public static class Cookie {
        /**
         * Name of the cookie that stores the refresh token
         */
        private String name;
        
        /**
         * Cookie path
         */
        private String path;
        
        /**
         * Whether the cookie should only be sent over HTTPS
         * Should be true in production
         */
        private boolean secure;
        
        /**
         * SameSite attribute for the cookie
         * Options: Strict, Lax, None
         */
        private String sameSite;
        
        /**
         * Cookie domain (optional)
         * Leave empty to use the request domain
         */
        private String domain;
    }
    
}
