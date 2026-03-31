package com.saasproject.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * 
 * Limits requests per IP address to prevent abuse.
 * Default: 100 requests/minute with burst capacity of 20.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitConfig {

    @Value("${app.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.burst-capacity:20}")
    private int burstCapacity;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));

        Bandwidth burst = Bandwidth.classic(
                burstCapacity,
                Refill.intervally(burstCapacity, Duration.ofSeconds(1)));

        return Bucket.builder()
                .addLimit(limit)
                .addLimit(burst)
                .build();
    }

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createBucket());
    }

    public class RateLimitFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            // Skip rate limiting for certain paths
            String path = request.getRequestURI();
            if (path.contains("/actuator") || path.contains("/swagger") || path.contains("/docs")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get client identifier (IP address or authenticated user)
            String clientId = getClientIdentifier(request);
            Bucket bucket = resolveBucket(clientId);

            if (bucket.tryConsume(1)) {
                // Add rate limit headers
                response.setHeader("X-Rate-Limit-Remaining",
                        String.valueOf(bucket.getAvailableTokens()));

                filterChain.doFilter(request, response);
            } else {
                log.warn("Rate limit exceeded for client: {}", clientId);

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "success": false,
                            "message": "Rate limit exceeded. Please try again later.",
                            "data": null
                        }
                        """);
            }
        }

        private String getClientIdentifier(HttpServletRequest request) {
            // Try to get user from authentication
            if (request.getUserPrincipal() != null) {
                return "user:" + request.getUserPrincipal().getName();
            }

            // Fall back to IP address
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return "ip:" + xForwardedFor.split(",")[0].trim();
            }

            return "ip:" + request.getRemoteAddr();
        }
    }
}
