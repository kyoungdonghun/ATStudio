package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.AuthRateLimitProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        AuthRateLimitProperties rateLimitProperties = new AuthRateLimitProperties();
        rateLimitProperties.setLogin(new AuthRateLimitProperties.Rule(1, 60));

        TrustedClientIdentityProperties identityProperties = new TrustedClientIdentityProperties();
        identityProperties.setEnabled(true);
        identityProperties.setTrustedProxyAddresses(List.of("127.0.0.1"));
        TrustedClientIdentityResolver resolver = new TrustedClientIdentityResolver(
                identityProperties,
                new AcceptanceProperties()
        );
        filter = new AuthRateLimitFilter(rateLimitProperties, resolver);
    }

    @Test
    void separatesValidatedClientIdentitiesBehindTrustedViteProxy() throws Exception {
        assertThat(invoke("127.0.0.1", "198.51.100.24").getStatus()).isEqualTo(200);
        assertThat(invoke("127.0.0.1", "198.51.100.25").getStatus()).isEqualTo(200);

        MockHttpServletResponse repeatedFirstClient = invoke("127.0.0.1", "198.51.100.24");
        assertThat(repeatedFirstClient.getStatus()).isEqualTo(429);
        assertThat(repeatedFirstClient.getHeader("Retry-After")).isNotBlank();
    }

    @Test
    void directSpoofedHeadersCannotSplitOneRemoteIdentity() throws Exception {
        assertThat(invoke("203.0.113.8", "198.51.100.24").getStatus()).isEqualTo(200);

        MockHttpServletResponse spoofedSecondIdentity = invoke("203.0.113.8", "198.51.100.25");
        assertThat(spoofedSecondIdentity.getStatus()).isEqualTo(429);
    }

    private MockHttpServletResponse invoke(String remoteAddress, String assertedAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(remoteAddress);
        request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, assertedAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
