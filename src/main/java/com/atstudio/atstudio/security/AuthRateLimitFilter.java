package com.atstudio.atstudio.security;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.AuthRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    private static final BUSINESS_ERROR RATE_LIMIT_ERROR = BUSINESS_ERROR.RATE_LIMIT_EXCEEDED;
    private static final int FINGERPRINT_SALT_BYTES = 32;

    private final AuthRateLimitProperties properties;
    private final TrustedClientIdentityResolver clientIdentityResolver;
    private final LongSupplier currentTimeMillis;
    private final byte[] fingerprintSalt;
    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();

    @Autowired
    public AuthRateLimitFilter(AuthRateLimitProperties properties,
                               TrustedClientIdentityResolver clientIdentityResolver) {
        this(properties, clientIdentityResolver, System::currentTimeMillis, createFingerprintSalt());
    }

    AuthRateLimitFilter(AuthRateLimitProperties properties,
                        TrustedClientIdentityResolver clientIdentityResolver,
                        LongSupplier currentTimeMillis,
                        byte[] fingerprintSalt) {
        this.properties = properties;
        this.clientIdentityResolver = clientIdentityResolver;
        this.currentTimeMillis = currentTimeMillis;
        this.fingerprintSalt = fingerprintSalt.clone();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateLimitTarget target = resolveTarget(request);
        if (!properties.isEnabled() || target == null || !target.isConfigured()) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = currentTimeMillis.getAsLong();
        maybeCleanup(now);

        String keyBase = buildKeyBase(request);
        Decision decision = tryAcquire(keyBase + ":scope=client", target.clientRule(), now);

        if (!decision.allowed()) {
            observeRateLimitExceeded(target, decision.retryAfterSeconds());
            writeRateLimitResponse(response, decision.retryAfterSeconds());
            return;
        }

        if (target.identifierRule() != null) {
            String identifierFingerprint = fingerprint(
                    "identifier\0" + target.identifierKind() + "\0" + normalizeIdentifier(request, target)
            );
            decision = tryAcquire(
                    keyBase + ":scope=identifier:identifier=" + identifierFingerprint,
                    target.identifierRule(),
                    now
            );
            if (!decision.allowed()) {
                observeRateLimitExceeded(target, decision.retryAfterSeconds());
                writeRateLimitResponse(response, decision.retryAfterSeconds());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Decision tryAcquire(String key, AuthRateLimitProperties.Rule rule, long now) {
        SlidingWindow window = windows.computeIfAbsent(key, ignored -> new SlidingWindow());
        return window.tryAcquire(rule.getLimit(), Duration.ofSeconds(rule.getWindowSeconds()), now);
    }

    private RateLimitTarget resolveTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (HttpMethod.POST.matches(request.getMethod())) {
            if ("/api/users".equals(uri)) {
                return RateLimitTarget.clientOnly(properties.getRegistration(), "POST /api/users");
            }
            if ("/api/auth/login".equals(uri)) {
                return RateLimitTarget.clientOnly(properties.getLogin(), "POST /api/auth/login");
            }
            if ("/api/auth/forgot-password".equals(uri)) {
                return RateLimitTarget.clientOnly(properties.getForgotPassword(), "POST /api/auth/forgot-password");
            }
            if ("/api/auth/reset-password".equals(uri)) {
                return RateLimitTarget.clientOnly(properties.getResetPassword(), "POST /api/auth/reset-password");
            }
            if ("/api/auth/refresh".equals(uri)) {
                return RateLimitTarget.clientOnly(properties.getRefresh(), "POST /api/auth/refresh");
            }
        }
        if (HttpMethod.GET.matches(request.getMethod())) {
            if ("/api/utils/check-email".equals(uri)) {
                return RateLimitTarget.identifier(
                        properties.getEmailAvailability(),
                        "email",
                        IdentifierKind.EMAIL,
                        "GET /api/utils/check-email"
                );
            }
            if ("/api/utils/check-phone".equals(uri)) {
                return RateLimitTarget.identifier(
                        properties.getPhoneAvailability(),
                        "phone",
                        IdentifierKind.PHONE,
                        "GET /api/utils/check-phone"
                );
            }
            if ("/api/utils/check-nickname".equals(uri)) {
                return RateLimitTarget.identifier(
                        properties.getNicknameAvailability(),
                        "nickname",
                        IdentifierKind.NICKNAME,
                        "GET /api/utils/check-nickname"
                );
            }
        }
        return null;
    }

    private String buildKeyBase(HttpServletRequest request) {
        String clientFingerprint = fingerprint("client\0" + clientIdentityResolver.resolve(request));
        return request.getMethod() + ':' + request.getRequestURI() + ":client=" + clientFingerprint;
    }

    private String normalizeIdentifier(HttpServletRequest request, RateLimitTarget target) {
        String[] values = request.getParameterValues(target.identifierParameter());
        String value = values != null && values.length == 1 ? values[0] : "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).strip();

        return switch (target.identifierKind()) {
            case EMAIL, NICKNAME -> normalized.toLowerCase(Locale.ROOT);
            case PHONE -> normalized.replaceAll("[^0-9]", "");
        };
    }

    private String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fingerprintSalt);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static byte[] createFingerprintSalt() {
        byte[] salt = new byte[FINGERPRINT_SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private void maybeCleanup(long now) {
        if ((requestCounter.incrementAndGet() & 255) != 0) {
            return;
        }

        long staleBefore = now - Duration.ofSeconds(properties.maxWindowSeconds()).toMillis();
        windows.entrySet().removeIf(entry -> entry.getValue().isStale(staleBefore));
    }

    int trackedWindowCount() {
        return windows.size();
    }

    private void observeRateLimitExceeded(RateLimitTarget target, long retryAfterSeconds) {
        LOGGER.warn(
                "Rate limit exceeded endpoint={} retryAfterSeconds={}",
                target.endpointScope(),
                Math.max(1L, retryAfterSeconds)
        );
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

    private record RateLimitTarget(AuthRateLimitProperties.Rule clientRule,
                                   AuthRateLimitProperties.Rule identifierRule,
                                   String identifierParameter,
                                   IdentifierKind identifierKind,
                                   String endpointScope) {

        private static RateLimitTarget clientOnly(AuthRateLimitProperties.Rule rule, String endpointScope) {
            return new RateLimitTarget(rule, null, null, null, endpointScope);
        }

        private static RateLimitTarget identifier(AuthRateLimitProperties.AvailabilityRule rule,
                                                  String identifierParameter,
                                                  IdentifierKind identifierKind,
                                                  String endpointScope) {
            return new RateLimitTarget(
                    rule.getClient(),
                    rule.getIdentifier(),
                    identifierParameter,
                    identifierKind,
                    endpointScope
            );
        }

        private boolean isConfigured() {
            return clientRule.isConfigured() && (identifierRule == null || identifierRule.isConfigured());
        }
    }

    private enum IdentifierKind {
        EMAIL,
        PHONE,
        NICKNAME
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
