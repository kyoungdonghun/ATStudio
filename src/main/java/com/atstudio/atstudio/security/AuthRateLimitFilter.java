package com.atstudio.atstudio.security;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.AuthRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final BUSINESS_ERROR RATE_LIMIT_ERROR = BUSINESS_ERROR.RATE_LIMIT_EXCEEDED;

    private final AuthRateLimitProperties properties;
    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        AuthRateLimitProperties.Rule rule = resolveRule(request);
        if (!properties.isEnabled() || rule == null || !rule.isConfigured()) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        maybeCleanup(now);

        String key = request.getMethod() + ":" + request.getRequestURI() + ":" + request.getRemoteAddr();
        SlidingWindow window = windows.computeIfAbsent(key, ignored -> new SlidingWindow());
        Decision decision = window.tryAcquire(rule.getLimit(), Duration.ofSeconds(rule.getWindowSeconds()), now);

        if (!decision.allowed()) {
            writeRateLimitResponse(response, decision.retryAfterSeconds());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AuthRateLimitProperties.Rule resolveRule(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }

        String uri = request.getRequestURI();
        if ("/api/auth/login".equals(uri)) {
            return properties.getLogin();
        }
        if ("/api/auth/forgot-password".equals(uri)) {
            return properties.getForgotPassword();
        }
        if ("/api/auth/reset-password".equals(uri)) {
            return properties.getResetPassword();
        }
        if ("/api/auth/refresh".equals(uri)) {
            return properties.getRefresh();
        }
        return null;
    }

    private void maybeCleanup(long now) {
        if ((requestCounter.incrementAndGet() & 255) != 0) {
            return;
        }

        long staleBefore = now - Duration.ofSeconds(properties.maxWindowSeconds() * 2).toMillis();
        windows.entrySet().removeIf(entry -> entry.getValue().isStale(staleBefore));
    }

    private void writeRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(Math.max(1L, retryAfterSeconds)));
        response.getWriter().write(buildRateLimitBody());
    }

    private String buildRateLimitBody() {
        String message = RATE_LIMIT_ERROR.getClientMessage().replace("\\", "\\\\").replace("\"", "\\\"");
        return """
                {"status":429,"error":"Too Many Requests","errorCode":"RATE_LIMIT_EXCEEDED","message":"%s"}
                """.formatted(message).trim();
    }

    private record Decision(boolean allowed, long retryAfterSeconds) {
    }

    private static final class SlidingWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private long lastSeenAt;

        synchronized Decision tryAcquire(int limit, Duration window, long now) {
            lastSeenAt = now;
            long boundary = now - window.toMillis();
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= boundary) {
                timestamps.removeFirst();
            }

            if (timestamps.size() >= limit) {
                long oldest = timestamps.peekFirst();
                long retryAfterMillis = Math.max(1000L, window.toMillis() - (now - oldest));
                long retryAfterSeconds = (retryAfterMillis + 999L) / 1000L;
                return new Decision(false, retryAfterSeconds);
            }

            timestamps.addLast(now);
            return new Decision(true, 0L);
        }

        synchronized boolean isStale(long staleBefore) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= staleBefore) {
                timestamps.removeFirst();
            }
            return timestamps.isEmpty() && lastSeenAt <= staleBefore;
        }
    }
}
