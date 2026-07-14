package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AcceptanceHostFilterTest {

    @Test
    void acceptsOnlyLoopbackBackendHostsInAcceptanceMode() throws Exception {
        AcceptanceProperties properties = new AcceptanceProperties();
        properties.setEnabled(true);
        AcceptanceHostFilter filter = new AcceptanceHostFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracks");
        request.addHeader("Host", "127.0.0.1:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsUnknownHostInAcceptanceMode() throws Exception {
        AcceptanceProperties properties = new AcceptanceProperties();
        properties.setEnabled(true);
        AcceptanceHostFilter filter = new AcceptanceHostFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracks");
        request.addHeader("Host", "attacker.example");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("Invalid Host header");
    }

    @Test
    void leavesHostPolicyInactiveOutsideAcceptance() throws Exception {
        AcceptanceProperties properties = new AcceptanceProperties();
        AcceptanceHostFilter filter = new AcceptanceHostFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracks");
        request.addHeader("Host", "custom.local.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }
}
