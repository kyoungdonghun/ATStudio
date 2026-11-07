package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.*;
import com.atstudio.atstudio.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Real shared + listening security configurations; synthetic handlers and no external services. */
class SecurityModeContextTest {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withUserConfiguration(Fixture.class)
            .withPropertyValues("app.listening.operator-addresses[0]=192.0.2.10");

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "MISSING"})
    void realConfigurationsStartWithExactlyOneChainAndSharedSecurityBeans(String mode) {
        mode(mode).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(SecurityFilterChain.class)
                    .hasSingleBean(PasswordEncoder.class).hasSingleBean(AuthenticationManager.class);
            FilterChainProxy proxy = context.getBean(FilterChainProxy.class);
            assertThat(proxy.getFilterChains()).hasSize(1);
            assertThat(context.containsBean("listeningSecurity")).isEqualTo(mode.equals("true"));
            assertThat(context.containsBean("filterChain")).isEqualTo(!mode.equals("true"));
        });
    }

    @Test
    void malformedModeCannotSilentlyStartWithoutApplicationSecurity() {
        mode("invalid").run(context -> assertThat(context).hasFailed());
    }

    @Test
    void retainedUnconditionalFactoryReproducesOriginalStartupFailure() {
        mode("true").withUserConfiguration(LegacyUnconditionalChain.class).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("UnreachableFilterChainException");
        });
    }

    @Test
    void listeningModeEnforcesPublicOperatorAndSensitivePathBoundaries() {
        mode("true").run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String role : List.of("ANONYMOUS", "USER", "ADMIN")) {
                mvc.perform(as(get("/api/tracks/1/stream"), role, false)).andExpect(status().isOk());
                mvc.perform(as(get("/api/tracks/admin"), role, false)).andExpect(status().isForbidden());
                for (String path : List.of("/api/payments", "/api/tracks/1/download",
                        "/uploads/tracks/audio/original.wav", "/uploads/company-docs/proof.pdf")) {
                    mvc.perform(as(get(path), role, true)).andExpect(status().isForbidden());
                }
                mvc.perform(as(post("/api/auth/login"), role, true)).andExpect(status().isOk());
                mvc.perform(as(post("/api/auth/reset-password"), role, true)).andExpect(status().isForbidden());
                mvc.perform(as(put("/api/tracks/1"), role, true))
                        .andExpect(status().is(role.equals("ADMIN") ? 200 : role.equals("USER") ? 403 : 401));
            }
            mvc.perform(get("/api/tracks/1/stream").header("Authorization", "Bearer invalid"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/tracks/admin").header("X-ATStudio-Client-IP", "192.0.2.10")
                    .header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/users/me").with(user("fixture").roles("ADMIN"))
                    .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isForbidden());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "MISSING"})
    void ordinaryModeRetainsPublicAdminUserPaymentAndRawFileRules(String mode) {
        mode(mode).run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String role : List.of("ANONYMOUS", "USER", "ADMIN")) {
                mvc.perform(as(get("/api/tracks/1/stream"), role, false)).andExpect(status().isOk());
                mvc.perform(as(post("/api/auth/login"), role, false)).andExpect(status().isOk());
                mvc.perform(as(put("/api/tracks/1"), role, false))
                        .andExpect(status().is(role.equals("ADMIN") ? 200 : role.equals("USER") ? 403 : 401));
                mvc.perform(as(get("/api/payments"), role, false))
                        .andExpect(status().is(role.equals("USER") ? 200 : role.equals("ADMIN") ? 403 : 401));
                mvc.perform(as(get("/uploads/tracks/audio/original.wav"), role, false))
                        .andExpect(status().is(role.equals("ANONYMOUS") ? 401 : 403));
            }
            mvc.perform(get("/api/users/me").with(user("fixture").roles("ADMIN")))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/payments").with(user("fixture").roles("USER", "ADMIN")))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/tracks/admin").header("Authorization", "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
        });
    }

    private WebApplicationContextRunner mode(String value) {
        return value.equals("MISSING") ? runner : runner.withPropertyValues("app.listening.enabled=" + value);
    }

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String role,
            boolean operator) {
        if (!role.equals("ANONYMOUS")) request.with(user("fixture").roles(role));
        if (operator) request.header("X-ATStudio-Client-IP", "192.0.2.10");
        return request;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableConfigurationProperties(ListeningProperties.class)
    @Import({SecurityConfig.class, ListeningSecurityConfiguration.class})
    static class Fixture {
        @Bean TrackRepository tracks() { return mock(TrackRepository.class); }
        @Bean JwtTokenProvider tokens() { return mock(JwtTokenProvider.class); }
        @Bean CustomUserDetailsService users() { return mock(CustomUserDetailsService.class); }
        @Bean JwtAuthenticationFilter jwt(JwtTokenProvider tokens, CustomUserDetailsService users) {
            return new JwtAuthenticationFilter(tokens, users);
        }
        @Bean TrustedClientIdentityResolver resolver() {
            var properties = new TrustedClientIdentityProperties(); properties.setEnabled(true);
            return new TrustedClientIdentityResolver(properties, new AcceptanceProperties());
        }
        @Bean AuthRateLimitFilter rate(TrustedClientIdentityResolver resolver) {
            var properties = new AuthRateLimitProperties(); properties.setEnabled(false);
            return new AuthRateLimitFilter(properties, resolver);
        }
        @Bean AcceptanceHostFilter host() { return new AcceptanceHostFilter(new AcceptanceProperties()); }
        @Bean @Primary CorsConfigurationSource corsConfigurationSource() {
            var source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", new CorsConfiguration()); return source;
        }
        @Bean FixtureController fixtureController() { return new FixtureController(); }
    }

    /** Calls the unchanged factory directly, recreating its prior unconditional @Bean registration. */
    @Configuration(proxyBeanMethods = false)
    static class LegacyUnconditionalChain {
        @Bean SecurityFilterChain legacyFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt,
                AcceptanceHostFilter host, AuthRateLimitFilter rate, CorsConfigurationSource cors) throws Exception {
            // Avoid the managed configuration's @Bean interceptor; retain the actual production factory body.
            return new SecurityConfig(jwt, host, rate, cors).filterChain(http);
        }
    }

    @RestController
    static class FixtureController {
        @GetMapping("/api/users/me") @PreAuthorize("denyAll()")
        String methodSecurityProbe() { return "must never be reached"; }
        @RequestMapping("/**") String ok() { return "local security fixture"; }
    }
}
