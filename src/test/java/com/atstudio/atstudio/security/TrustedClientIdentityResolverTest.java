package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedClientIdentityResolverTest {

    private TrustedClientIdentityProperties properties;
    private AcceptanceProperties acceptanceProperties;
    private TrustedClientIdentityResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new TrustedClientIdentityProperties();
        properties.setEnabled(true);
        properties.setTrustedProxyAddresses(List.of("127.0.0.1", "::1"));
        acceptanceProperties = new AcceptanceProperties();
        resolver = new TrustedClientIdentityResolver(properties, acceptanceProperties);
    }

    @Test
    void trustsOneValidatedLiteralFromConfiguredLoopbackProxy() {
        MockHttpServletRequest request = request("127.0.0.1", "198.51.100.24");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.24");
    }

    @Test
    void ignoresDirectSpoofFromNonLoopbackPeer() {
        MockHttpServletRequest request = request("203.0.113.8", "198.51.100.24");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.8");
    }

    @Test
    void ignoresHeaderWhenTrustedIdentityIsDisabledOutsideAcceptance() {
        properties.setEnabled(false);
        MockHttpServletRequest request = request("127.0.0.1", "198.51.100.24");

        assertThat(resolver.resolve(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void acceptanceModeEnablesConfiguredLoopbackTrust() {
        properties.setEnabled(false);
        acceptanceProperties.setEnabled(true);
        MockHttpServletRequest request = request("127.0.0.1", "198.51.100.24");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.24");
    }

    @Test
    void rejectsDuplicateListPortZoneAndWhitespaceValues() {
        for (String invalid : List.of(
                "198.51.100.24,198.51.100.25",
                "198.51.100.24:443",
                "fe80::1%eth0",
                " 198.51.100.24",
                "198.51.100.24 "
        )) {
            assertThat(resolver.resolve(request("127.0.0.1", invalid)))
                    .as("invalid header %s", invalid)
                    .isEqualTo("127.0.0.1");
        }

        MockHttpServletRequest duplicate = request("127.0.0.1", "198.51.100.24");
        duplicate.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "198.51.100.25");
        assertThat(resolver.resolve(duplicate)).isEqualTo("127.0.0.1");
    }

    private MockHttpServletRequest request(String remoteAddress, String assertedAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        request.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, assertedAddress);
        return request;
    }
}
