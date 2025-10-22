package br.com.fiap.lovelace_project_api.security;

import br.com.fiap.lovelace_project_api.config.RateLimitProperties;
import br.com.fiap.lovelace_project_api.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 5) // run early
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties props;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String key;
        int capacity;
        long windowMs;

        if (path.startsWith("/api/auth/login")) {
            key = "login:ip:" + getClientIP(request);
            capacity = props.getLoginCapacity();
            windowMs = props.getLoginWindowMs();
        } else if (path.startsWith("/api/auth/refresh")) {
            // use userId if present (JWT), otherwise fallback to IP
            String userId = jwtUtils.extractUserIdFromRequest(request);
            if (userId != null) {
                key = "refresh:user:" + userId;
            } else {
                key = "refresh:ip:" + getClientIP(request);
            }
            capacity = props.getRefreshCapacity();
            windowMs = props.getRefreshWindowMs();
        } else {
            key = "global:ip:" + getClientIP(request);
            capacity = props.getGlobalCapacity();
            windowMs = props.getGlobalWindowMs();
        }

        long[] result = rateLimitService.consume(key, capacity, windowMs);
        long allowed = result[0];
        long remaining = result[1];
        long reset = result[2];

        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(reset));

        if (allowed == 1L) {
            filterChain.doFilter(request, response);
        } else {
            // Too many requests
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(reset));
            response.setContentType("application/json");
            String body = String.format("{\"message\":\"Too many requests. Retry after %d seconds.\"}", reset);
            response.getWriter().write(body);
            log.warn("Rate limit exceeded for key={} path={}", key, path);
        }
    }

    private String getClientIP(HttpServletRequest req) {
        String xfHeader = req.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return req.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
