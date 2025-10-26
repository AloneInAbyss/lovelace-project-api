package com.aloneinabyss.lovelace.security.service;

import com.aloneinabyss.lovelace.security.JwtTokenProvider;
import com.aloneinabyss.lovelace.shared.exception.ErrorCode;
import com.aloneinabyss.lovelace.shared.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing JWT token blacklist using Redis.
 * Blacklisted tokens are stored with automatic expiration based on their original expiry time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    /**
     * Blacklist a JWT token until its natural expiration time.
     * The token will be automatically removed from Redis after expiration.
     *
     * @param token The JWT token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            Date expirationDate = jwtTokenProvider.extractExpiration(token);
            long currentTime = System.currentTimeMillis();
            long expirationTime = expirationDate.getTime();
            
            // Calculate time until token expires
            long timeToLive = expirationTime - currentTime;
            
            // Only blacklist if token hasn't expired yet
            if (timeToLive > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "blacklisted", timeToLive, TimeUnit.MILLISECONDS);
                log.info("Token blacklisted successfully. Will expire in {} ms", timeToLive);
            } else {
                log.debug("Token is already expired, no need to blacklist");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, "error.internal", e);
        }
    }
    
    /**
     * Check if a token is blacklisted.
     *
     * @param token The JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist status: {}", e.getMessage(), e);
            // Fail securely - if we can't check, treat as blacklisted
            return true;
        }
    }
}
