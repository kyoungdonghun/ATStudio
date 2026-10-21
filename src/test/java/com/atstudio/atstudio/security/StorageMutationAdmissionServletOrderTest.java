package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.AuthRateLimitProperties;
import com.atstudio.atstudio.config.SecurityConfig;
import com.atstudio.atstudio.config.StorageMutationAdmissionConfiguration;
import com.atstudio.atstudio.config.StorageMutationAdmissionProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.servlet.filter.OrderedFormContentFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** No application scan, Boot auto-configuration, DB, scheduler, storage, or listening server. */
class StorageMutationAdmissionServletOrderTest {

    private AnnotationConfigWebApplicationContext context;
    private List<FilterRegistrationBean<?>> registrations;
    private StorageMutationAdmissionFilter admission;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        context.register(IsolatedSecurity.class);
        context.refresh();
        // The same Boot collector used for embedded-container servlet initializer ordering.
        registrations = new ArrayList<>();
        for (var initializer : new ServletContextInitializerBeans(context.getBeanFactory())) {
            if (initializer instanceof FilterRegistrationBean<?> registration) registrations.add(registration);
        }
        admission = (StorageMutationAdmissionFilter) registrations.get(0).getFilter();
    }

    @AfterEach
    void close() {
        SecurityContextHolder.clearContext();
        if (context != null) context.close();
    }

    @Test
    void bootRegistrationPrecedesFormSecurityAndAutoRegisteredComponentCopies() {
        assertThat(registrations.get(0).getFilter()).isInstanceOf(StorageMutationAdmissionFilter.class);
        assertThat(registrations.get(0).getOrder()).isLessThan(OrderedFormContentFilter.DEFAULT_ORDER);
        assertThat(registrations.stream().filter(r -> r.getFilter() instanceof StorageMutationAdmissionFilter)).hasSize(1);
        assertThat(registrations.stream().map(r -> r.getFilter().getClass().getSimpleName()).toList())
                .contains("OrderedFormContentFilter",
                        "AuthRateLimitFilter", "AcceptanceHostFilter", "JwtAuthenticationFilter");
        assertThat(registrations.get(2).getFilter()).isInstanceOf(FilterChainProxy.class);
        assertThat(registrations.get(2).getOrder()).isEqualTo(-100);
        assertThat(context.getBeansOfType(StorageMutationAdmissionFilter.class)).isEmpty();
        var proxy = context.getBean(FilterChainProxy.class);
        assertThat(proxy.getFilterChains().stream().flatMap(chain -> chain.getFilters().stream()))
                .noneMatch(StorageMutationAdmissionFilter.class::isInstance);
        assertThat(context.getBeansOfType(StrictHttpFirewall.class)).hasSize(1);
    }

    @Test
    void collectedServletChainRejectsFormAndMultipartBeforeBodyOrAuthenticationLookup() throws Exception {
        MockAsyncContext held = hold();
        try {
            for (String method : List.of("PUT", "PATCH", "DELETE")) {
                for (String type : List.of("application/x-www-form-urlencoded", "multipart/form-data; boundary=x",
                        "application/json")) {
                    var request = spy(request(method, "/api/notices/1", null));
                    request.setContentType(type);
                    request.setContent("attachments=1".getBytes());
                    var response = new MockHttpServletResponse();
                    runCollectedChain(request, response, (req, res) -> { throw new AssertionError("Body reached"); });
                    assertThat(response.getStatus()).isEqualTo(429);
                    verify(request, never()).getInputStream();
                    verify(request, never()).getReader();
                    verify(request, never()).getParts();
                    verify(request, never()).getParameterMap();
                }
            }
            verifyNoInteractions(context.getBean(CustomUserDetailsService.class));
        } finally {
            held.complete();
        }
        assertThat(admission.activeRequestCount()).isZero();
    }

    @Test
    void actualAuthorizationRulesAndPrivateMediaDenialArePreserved() throws Exception {
        for (String role : List.of("anonymous", "USER", "ADMIN")) {
            String token = role.equals("anonymous") ? null : role;
            var response = new MockHttpServletResponse();
            runCollectedChain(request("POST", "/api/tracks", token), response, (req, res) -> {});
            assertThat(response.getStatus()).isEqualTo(switch (role) {
                case "anonymous" -> 401;
                case "USER" -> 403;
                default -> 200;
            });
            assertThat(admission.activeRequestCount()).isZero();
            var privateResponse = new MockHttpServletResponse();
            runCollectedChain(request("GET", "/uploads/tracks/audio/private.mp3", token), privateResponse,
                    (req, res) -> { throw new AssertionError("Private media delivered"); });
            assertThat(privateResponse.getStatus()).isEqualTo(role.equals("anonymous") ? 401 : 403);
        }
    }

    @Test
    void normalSecurityFirewallStillRejectsWhenAdmissionDisabled() throws Exception {
        context.getBean(StorageMutationAdmissionProperties.class).setEnabled(false);
        var response = new MockHttpServletResponse();
        context.getBean(FilterChainProxy.class).doFilter(request("PUT", "/api//tracks/1", "ADMIN"), response,
                (req, res) -> { throw new AssertionError("Ambiguous path delivered"); });
        assertThat(response.getStatus()).isEqualTo(400);
    }

    private MockAsyncContext hold() throws Exception {
        AtomicReference<MockAsyncContext> result = new AtomicReference<>();
        admission.doFilter(request("POST", "/api/tracks", null), new MockHttpServletResponse(),
                (req, res) -> result.set((MockAsyncContext) req.startAsync()));
        return result.get();
    }

    private MockHttpServletRequest request(String method, String path, String token) {
        var request = new MockHttpServletRequest(context.getServletContext(), method, path);
        request.setServletPath(path);
        request.setRemoteAddr("192.0.2.1");
        request.setAsyncSupported(true);
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void runCollectedChain(ServletRequest request, ServletResponse response, FilterChain terminal)
            throws ServletException, IOException {
        FilterChain chain = terminal;
        for (int i = registrations.size() - 1; i >= 0; i--) {
            Filter filter = registrations.get(i).getFilter();
            FilterChain next = chain;
            chain = (req, res) -> filter.doFilter(req, res, next);
        }
        chain.doFilter(request, response);
    }

    @Configuration(proxyBeanMethods = false)
    @Import({SecurityConfig.class, StorageMutationAdmissionConfiguration.class})
    static class IsolatedSecurity {
        @Bean
        AcceptanceProperties acceptanceProperties() { return new AcceptanceProperties(); }

        @Bean
        TrustedClientIdentityResolver resolver(AcceptanceProperties acceptance) {
            return new TrustedClientIdentityResolver(new TrustedClientIdentityProperties(), acceptance);
        }

        @Bean
        AuthRateLimitFilter authRateLimitFilter(TrustedClientIdentityResolver resolver) {
            return new AuthRateLimitFilter(new AuthRateLimitProperties(), resolver);
        }

        @Bean
        AcceptanceHostFilter acceptanceHostFilter(AcceptanceProperties properties) {
            return new AcceptanceHostFilter(properties);
        }

        @Bean
        JwtTokenProvider jwtTokenProvider() {
            var provider = mock(JwtTokenProvider.class);
            when(provider.validateAccessToken(any())).thenReturn(TokenValidationResult.VALID);
            when(provider.getUserID("USER")).thenReturn(1L);
            when(provider.getUserID("ADMIN")).thenReturn(2L);
            return provider;
        }

        @Bean
        CustomUserDetailsService userDetailsService() {
            var service = mock(CustomUserDetailsService.class);
            when(service.loadUserById(1L)).thenReturn(User.withUsername("synthetic-user").password("unused").roles("USER").build());
            when(service.loadUserById(2L)).thenReturn(User.withUsername("synthetic-admin").password("unused").roles("ADMIN").build());
            return service;
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider, CustomUserDetailsService users) {
            return new JwtAuthenticationFilter(provider, users);
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() { return new UrlBasedCorsConfigurationSource(); }

        @Bean
        OrderedFormContentFilter formContentFilter() { return new OrderedFormContentFilter(); }

        @Bean
        FilterRegistrationBean<FilterChainProxy> securityRegistration(FilterChainProxy springSecurityFilterChain) {
            // Mirror Boot's security proxy order without enabling application auto-configuration.
            var registration = new FilterRegistrationBean<>(springSecurityFilterChain);
            registration.setOrder(-100);
            return registration;
        }
    }
}
