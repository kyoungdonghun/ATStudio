package com.atstudio.atstudio.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.AuthRateLimitProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    private AuthRateLimitFilter filter;
    private AuthRateLimitProperties rateLimitProperties;
    private TrustedClientIdentityResolver clientIdentityResolver;
    private AtomicLong now;

    @BeforeEach
    void setUp() {
        rateLimitProperties = new AuthRateLimitProperties();
        AuthRateLimitProperties.Rule onePerMinute = new AuthRateLimitProperties.Rule(1, 60);
        rateLimitProperties.setRegistration(onePerMinute);
        rateLimitProperties.setLogin(onePerMinute);
        rateLimitProperties.setForgotPassword(onePerMinute);
        rateLimitProperties.setResetPassword(onePerMinute);
        rateLimitProperties.setRefresh(onePerMinute);
        rateLimitProperties.setEmailAvailability(availabilityRule(2, 1));
        rateLimitProperties.setPhoneAvailability(availabilityRule(2, 1));
        rateLimitProperties.setNicknameAvailability(availabilityRule(2, 1));

        TrustedClientIdentityProperties identityProperties = new TrustedClientIdentityProperties();
        identityProperties.setEnabled(true);
        identityProperties.setTrustedProxyAddresses(List.of("127.0.0.1"));
        clientIdentityResolver = new TrustedClientIdentityResolver(
                identityProperties,
                new AcceptanceProperties()
        );
        now = new AtomicLong();
        filter = createFilter();
    }

    @Test
    void separatesValidatedClientIdentitiesBehindTrustedViteProxy() throws Exception {
        assertThat(invokeLogin("127.0.0.1", "198.51.100.24").getStatus()).isEqualTo(200);
        assertThat(invokeLogin("127.0.0.1", "198.51.100.25").getStatus()).isEqualTo(200);

        MockHttpServletResponse repeatedFirstClient = invokeLogin("127.0.0.1", "198.51.100.24");
        assertRateLimited(repeatedFirstClient);
    }

    @Test
    void spoofedInvalidAndMultipleHeadersConvergeOnDirectPeer() throws Exception {
        assertThat(invokeLogin("203.0.113.8", "198.51.100.24").getStatus()).isEqualTo(200);

        MockHttpServletRequest request = request("POST", "/api/auth/login", "203.0.113.8");
        request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "198.51.100.25");
        request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "198.51.100.26");

        assertRateLimited(invoke(request));
    }

    @Test
    void registrationUsesOnlyClientFingerprintWithoutReadingBody() throws Exception {
        BodyGuardRequest first = new BodyGuardRequest("POST", "/api/users");
        first.setRemoteAddr("203.0.113.10");
        first.setContent("{\"email\":\"first@example.com\"}".getBytes());
        assertThat(invoke(first).getStatus()).isEqualTo(200);

        BodyGuardRequest second = new BodyGuardRequest("POST", "/api/users");
        second.setRemoteAddr("203.0.113.10");
        second.setContent("{\"email\":\"second@example.com\"}".getBytes());
        assertRateLimited(invoke(second));
    }

    @Test
    void normalizedEmailVariantsShareOneFingerprintBudget() throws Exception {
        assertThat(invokeAvailability(
                "/api/utils/check-email",
                "email",
                " User@Example.COM ",
                "127.0.0.1",
                "198.51.100.30"
        ).getStatus()).isEqualTo(200);

        MockHttpServletResponse normalizedRepeat = invokeAvailability(
                "/api/utils/check-email",
                "email",
                "user@example.com",
                "127.0.0.1",
                "198.51.100.30"
        );
        assertRateLimited(normalizedRepeat);
    }

    @Test
    void normalizedPhoneVariantsShareOneFingerprintBudget() throws Exception {
        assertThat(invokeAvailability(
                "/api/utils/check-phone",
                "phone",
                "010-1234-5678",
                "203.0.113.11",
                null
        ).getStatus()).isEqualTo(200);

        assertRateLimited(invokeAvailability(
                "/api/utils/check-phone",
                "phone",
                "01012345678",
                "203.0.113.11",
                null
        ));
    }

    @Test
    void differentIdentifiersAndClientsUseSeparateBudgets() throws Exception {
        assertThat(invokeAvailability(
                "/api/utils/check-nickname",
                "nickname",
                "first-user",
                "127.0.0.1",
                "198.51.100.40"
        ).getStatus()).isEqualTo(200);
        assertThat(invokeAvailability(
                "/api/utils/check-nickname",
                "nickname",
                "second-user",
                "127.0.0.1",
                "198.51.100.40"
        ).getStatus()).isEqualTo(200);
        assertThat(invokeAvailability(
                "/api/utils/check-nickname",
                "nickname",
                "first-user",
                "127.0.0.1",
                "198.51.100.41"
        ).getStatus()).isEqualTo(200);
    }

    @Test
    void rotatingIdentifiersStillHitEndpointClientBudget() throws Exception {
        assertThat(invokeAvailability(
                "/api/utils/check-email", "email", "first@example.com", "203.0.113.14", null
        ).getStatus()).isEqualTo(200);
        assertThat(invokeAvailability(
                "/api/utils/check-email", "email", "second@example.com", "203.0.113.14", null
        ).getStatus()).isEqualTo(200);

        assertRateLimited(invokeAvailability(
                "/api/utils/check-email", "email", "third@example.com", "203.0.113.14", null
        ));
    }

    @Test
    void availabilityEndpointsHaveIndependentBudgets() throws Exception {
        MockHttpServletResponse email = invokeAvailability(
                "/api/utils/check-email", "email", "same@example.com", "203.0.113.12", null
        );
        MockHttpServletResponse phone = invokeAvailability(
                "/api/utils/check-phone", "phone", "010-0000-0000", "203.0.113.12", null
        );
        MockHttpServletResponse nickname = invokeAvailability(
                "/api/utils/check-nickname", "nickname", "same", "203.0.113.12", null
        );

        assertThat(email.getStatus()).isEqualTo(200);
        assertThat(phone.getStatus()).isEqualTo(200);
        assertThat(nickname.getStatus()).isEqualTo(200);
        assertRateLimited(invokeAvailability(
                "/api/utils/check-email", "email", "same@example.com", "203.0.113.12", null
        ));
        assertRateLimited(invokeAvailability(
                "/api/utils/check-phone", "phone", "010-0000-0000", "203.0.113.12", null
        ));
        assertRateLimited(invokeAvailability(
                "/api/utils/check-nickname", "nickname", "same", "203.0.113.12", null
        ));
    }

    @Test
    void keysDoNotContainRawClientOrIdentifierValues() throws Exception {
        invokeAvailability(
                "/api/utils/check-email",
                "email",
                "private@example.com",
                "127.0.0.1",
                "198.51.100.50"
        );

        @SuppressWarnings("unchecked")
        Map<String, ?> windows = (Map<String, ?>) ReflectionTestUtils.getField(filter, "windows");

        assertThat(windows).isNotNull();
        assertThat(windows.keySet()).allSatisfy(key -> {
            assertThat(key).doesNotContain("private@example.com");
            assertThat(key).doesNotContain("198.51.100.50");
        });
    }

    @Test
    void rateLimitLogContainsOnlyEndpointScopeAndRetryInformation() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthRateLimitFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            invokeAvailability(
                    "/api/utils/check-email", "email", "private@example.com", "203.0.113.15", null
            );
            assertRateLimited(invokeAvailability(
                    "/api/utils/check-email", "email", "private@example.com", "203.0.113.15", null
            ));

            assertThat(appender.list).hasSize(1);
            assertThat(appender.list.get(0).getFormattedMessage())
                    .isEqualTo("Rate limit exceeded endpoint=GET /api/utils/check-email retryAfterSeconds=60")
                    .doesNotContain("private@example.com", "203.0.113.15");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void cleanupExpiresIdleKeysAtConfiguredMaximumWindow() throws Exception {
        rateLimitProperties.setEmailAvailability(availabilityRule(1_000, 1));
        filter = createFilter();
        invokeAvailability(
                "/api/utils/check-email", "email", "expired@example.com", "203.0.113.13", null
        );
        now.set(61_000L);

        for (int i = 0; i < 255; i++) {
            invokeAvailability(
                    "/api/utils/check-email",
                    "email",
                    "current-" + i + "@example.com",
                    "203.0.113.13",
                    null
            );
        }

        assertThat(filter.trackedWindowCount()).isEqualTo(256);
    }

    private AuthRateLimitFilter createFilter() {
        return new AuthRateLimitFilter(rateLimitProperties, clientIdentityResolver, now::get, new byte[32]);
    }

    private static AuthRateLimitProperties.AvailabilityRule availabilityRule(int clientLimit,
                                                                              int identifierLimit) {
        AuthRateLimitProperties.AvailabilityRule rule = new AuthRateLimitProperties.AvailabilityRule();
        rule.setClient(new AuthRateLimitProperties.Rule(clientLimit, 60));
        rule.setIdentifier(new AuthRateLimitProperties.Rule(identifierLimit, 60));
        return rule;
    }

    private MockHttpServletResponse invokeLogin(String remoteAddress, String assertedAddress) throws Exception {
        MockHttpServletRequest request = request("POST", "/api/auth/login", remoteAddress);
        request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, assertedAddress);
        return invoke(request);
    }

    private MockHttpServletResponse invokeAvailability(String uri,
                                                        String parameterName,
                                                        String parameterValue,
                                                        String remoteAddress,
                                                        String assertedAddress) throws Exception {
        MockHttpServletRequest request = request("GET", uri, remoteAddress);
        request.addParameter(parameterName, parameterValue);
        if (assertedAddress != null) {
            request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, assertedAddress);
        }
        return invoke(request);
    }

    private MockHttpServletRequest request(String method, String uri, String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(remoteAddress);
        return request;
    }

    private MockHttpServletResponse invoke(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private void assertRateLimited(MockHttpServletResponse response) throws Exception {
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isNotBlank();
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).contains("RATE_LIMIT_EXCEEDED");
    }

    private static final class BodyGuardRequest extends MockHttpServletRequest {

        private BodyGuardRequest(String method, String requestURI) {
            super(method, requestURI);
        }

        @Override
        public ServletInputStream getInputStream() {
            throw new AssertionError("Registration body must not be read by the rate-limit filter");
        }

        @Override
        public BufferedReader getReader() {
            throw new AssertionError("Registration body must not be read by the rate-limit filter");
        }
    }
}
