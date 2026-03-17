package com.tetgift.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting filter for chatbot endpoints.
 * Uses in-memory ConcurrentHashMap to track request timestamps per IP.
 * 
 * Limits:
 * - Guest (by IP): 10 requests / minute
 * - Authenticated User: 30 requests / minute (identified by JWT subject)
 * 
 * Note: This is a single-instance solution. For multi-instance deployments,
 * consider using Redis-based rate limiting instead.
 */
@Component
@Slf4j
public class ChatbotRateLimitFilter extends OncePerRequestFilter {

    // IP -> list of request timestamps (epoch millis)
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    private static final int GUEST_LIMIT = 10;          // max requests per window
    private static final int AUTHENTICATED_LIMIT = 30;   // max requests per window
    private static final long WINDOW_MS = 60_000;        // 1 minute window

    // Target paths for rate limiting
    private static final String CHATBOT_PATH_PREFIX = "/api/v1/chatbot/chat";

    public ChatbotRateLimitFilter() {
        // Scheduled cleanup every 5 minutes to prevent memory leaks from stale entries
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanupStaleEntries, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply rate limiting to chatbot endpoints
        if (!path.startsWith(CHATBOT_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        int limit = isAuthenticated(request) ? AUTHENTICATED_LIMIT : GUEST_LIMIT;

        if (!isAllowed(clientKey, limit)) {
            log.warn("Rate limit exceeded for client: {} on path: {}", clientKey, path);
            writeRateLimitResponse(response, limit);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request is within the rate limit window.
     * Returns true if allowed, false if limit exceeded.
     */
    private boolean isAllowed(String clientKey, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, k -> new ConcurrentLinkedDeque<>());

        // Remove timestamps outside the current window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= limit) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Resolve client key: use authenticated user ID if available, otherwise client IP.
     */
    private String resolveClientKey(HttpServletRequest request) {
        // Check if user is authenticated (via SecurityContext or JWT header)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Use "auth:" prefix to separate from IP-based keys
            return "auth:" + authHeader.hashCode();
        }

        // Fall back to client IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim(); // Take first IP in chain
        } else {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    /**
     * Write a 429 Too Many Requests response with JSON body.
     */
    private void writeRateLimitResponse(HttpServletResponse response, int limit) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String body = String.format("""
            {
                "timestamp": "%s",
                "status": 429,
                "error": "Too Many Requests",
                "message": "Bạn đã gửi quá nhiều tin nhắn. Vui lòng chờ 1 phút rồi thử lại. (Giới hạn: %d tin nhắn/phút)",
                "path": "/api/v1/chatbot/chat"
            }
            """, LocalDateTime.now(), limit);

        response.getWriter().write(body);
    }

    /**
     * Remove entries that haven't had requests in the last 10 minutes.
     * This prevents unbounded memory growth.
     */
    private void cleanupStaleEntries() {
        long cutoff = System.currentTimeMillis() - (10 * 60_000); // 10 minutes ago
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
        });
        log.debug("Rate limit cleanup: {} active keys remaining", requestLog.size());
    }
}
