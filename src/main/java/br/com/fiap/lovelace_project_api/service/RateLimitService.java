package br.com.fiap.lovelace_project_api.service;

import br.com.fiap.lovelace_project_api.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

/**
 * Simple Redis-backed sliding-window approximated rate limiter.
 * Uses Redis INCR + TTL for atomicity. Falls back to a local in-memory
 * limiter if Redis is unavailable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties props;

    // In-memory fallback: map key -> (count, windowStart)
    private final Map<String, AtomicInteger> localCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> localWindows = new ConcurrentHashMap<>();

    /**
     * Try to consume one token for the given key with capacity/window parameters.
     * Returns an array: [allowed (0/1), remaining, resetSeconds]
     */
    public long[] consume(String key, int capacity, long windowMs) {
        try {
            String redisKey = "ratelimit:" + key;
            Long countObj = redisTemplate.opsForValue().increment(redisKey);
            long count = countObj != null ? countObj.longValue() : 0L;
            if (count == 1L) {
                // First increment, set TTL
                redisTemplate.expire(redisKey, windowMs, TimeUnit.MILLISECONDS);
            }
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            long remaining = Math.max(0L, capacity - count);
            long reset = ttl != null ? ttl : 0L;
            long allowed = count <= capacity ? 1L : 0L;
            return new long[]{allowed, remaining, reset};
        } catch (Exception e) {
            // Redis unavailable - fallback to local in-memory limiter
            log.warn("Redis unavailable for rate limiting, using local fallback: {}", e.getMessage());
            return consumeLocal(key, capacity, windowMs);
        }
    }

    // Small convenience access to properties (avoid unused field warning elsewhere)
    public RateLimitProperties getProps() {
        return props;
    }

    private long[] consumeLocal(String key, int capacity, long windowMs) {
        long now = Instant.now().toEpochMilli();
        localWindows.putIfAbsent(key, new AtomicLong(now));
        localCounts.putIfAbsent(key, new AtomicInteger(0));

        AtomicLong windowStart = localWindows.get(key);
        AtomicInteger count = localCounts.get(key);

        synchronized (windowStart) {
            if (now - windowStart.get() >= windowMs) {
                windowStart.set(now);
                count.set(0);
            }
            int newCount = count.incrementAndGet();
            long remaining = Math.max(0, capacity - newCount);
            long reset = windowMs / 1000; // approximate seconds until reset
            long allowed = newCount <= capacity ? 1L : 0L;
            return new long[]{allowed, remaining, reset};
        }
    }
}
