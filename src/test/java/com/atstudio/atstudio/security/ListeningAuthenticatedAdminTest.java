package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.*;
import com.atstudio.atstudio.controller.ListeningCatalogController;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.ListeningCatalogService;
import com.atstudio.atstudio.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ListeningAuthenticatedAdminTest {
    final WebApplicationContextRunner base = new WebApplicationContextRunner().withUserConfiguration(Fixture.class)
            .withPropertyValues("app.listening.enabled=true", "app.listening.operator-addresses[0]=192.0.2.10");
    WebApplicationContextRunner enabled() { return base.withPropertyValues("app.listening.operator-access=AUTHENTICATED_ADMIN"); }
    static MockHttpServletRequestBuilder client(MockHttpServletRequestBuilder request) {
        return request.header("X-ATStudio-Client-IP", "198.51.100.55");
    }
    static CustomUserDetails admin() { return new CustomUserDetails(2L, "local-fixture@example.invalid", "unused", UserRole.ADMIN, false, true); }

    @Test void arbitraryClientCanLoginButEveryProtectedOperationStillRequiresAdmin() {
        enabled().run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            mvc.perform(client(get("/login"))).andExpect(status().isOk());
            mvc.perform(client(post("/api/auth/login"))).andExpect(status().isOk());
            for (String role : List.of("ANONYMOUS", "USER", "ADMIN")) {
                int expected = role.equals("ADMIN") ? 200 : role.equals("USER") ? 403 : 401;
                for (var request : List.of(get("/api/tracks/admin"), post("/api/tracks"), put("/api/tracks/7"),
                        get("/api/tracks/admin/listening-catalog"), post("/api/tracks/admin/7/audio-processing/retry"))) {
                    if (!role.equals("ANONYMOUS")) request.with(user("fixture").roles(role));
                    mvc.perform(client(request)).andExpect(status().is(expected));
                }
            }
            verify(context.getBean(ListeningCatalogService.class), times(1)).get();
            mvc.perform(client(get("/api/tracks/admin")).header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        });
    }
    @Test void actualCatalogPutAndReconcileKeepMethodSecurityAndActorBinding() {
        enabled().run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            String body = "{\"expectedRevision\":0,\"trackIds\":[],\"reason\":\"local test\",\"creditsConfirmed\":true,\"attemptId\":\"12345678-1234-1234-1234-123456789abc\"}";
            mvc.perform(client(put("/api/tracks/admin/listening-catalog")).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
            mvc.perform(client(put("/api/tracks/admin/listening-catalog")).with(user("user").roles("USER")).contentType("application/json").content(body)).andExpect(status().isForbidden());
            mvc.perform(client(put("/api/tracks/admin/listening-catalog")).with(user(admin())).contentType("application/json").content(body)).andExpect(status().isOk());
            verify(context.getBean(ListeningCatalogService.class)).replace(any(), eq(2L));
            String reconcile = "{\"attemptId\":\"12345678-1234-1234-1234-123456789abc\",\"expectedRevision\":0,\"beforeTrackIds\":[],\"beforeDigest\":\"x\",\"trackIds\":[],\"reason\":\"local test\"}";
            mvc.perform(client(post("/api/tracks/admin/listening-catalog/reconcile")).with(user(admin())).contentType("application/json").content(reconcile)).andExpect(status().isOk());
            verify(context.getBean(ListeningCatalogService.class)).reconcile(any(), eq(2L));
        });
    }
    @Test void missingDuplicateInvalidAndLoopbackProxyAssertionsStillDenyAdmin() {
        enabled().run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String value : List.of("", "127.0.0.1", "::1", "unknown", "198.51.100.55,192.0.2.10"))
                mvc.perform(get("/api/tracks/admin").header("X-ATStudio-Client-IP",value).with(user(admin()))).andExpect(status().isForbidden());
            mvc.perform(get("/api/tracks/admin").with(user(admin()))).andExpect(status().isForbidden());
            mvc.perform(get("/api/tracks/admin").header("X-ATStudio-Client-IP","198.51.100.55","192.0.2.10").with(user(admin()))).andExpect(status().isForbidden());
        });
    }
    @Test void publicVisitorTokensAreDiscardedAndUnrelatedSurfacesRemainDenied() {
        enabled().run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            clearInvocations(context.getBean(JwtTokenProvider.class)); // Ignore Spring lifecycle callback on the mock.
            mvc.perform(get("/api/tracks").header("Authorization","Bearer stale")).andExpect(status().isOk());
            mvc.perform(get("/api/tracks/7/stream").header("Authorization","Bearer stale")).andExpect(status().isOk());
            verifyNoInteractions(context.getBean(JwtTokenProvider.class));
            for (String path : List.of("/api/payments", "/api/tracks/7/download", "/uploads/tracks/audio/original.wav", "/swagger-ui/index.html", "/actuator"))
                mvc.perform(client(get(path)).with(user(admin()))).andExpect(status().isForbidden());
            mvc.perform(client(delete("/api/tracks/7")).with(user(admin()))).andExpect(status().isForbidden());
        });
    }
    @Test void loginAndRefreshRateLimitsStillUseVerifiedClientIdentity() {
        enabled().run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (int i=0;i<10;i++) mvc.perform(client(post("/api/auth/login"))).andExpect(status().isOk());
            mvc.perform(client(post("/api/auth/login"))).andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
            mvc.perform(post("/api/auth/login").header("X-ATStudio-Client-IP","198.51.100.56")).andExpect(status().isOk());
            for (int i=0;i<30;i++) mvc.perform(client(post("/api/auth/refresh"))).andExpect(status().isOk());
            mvc.perform(client(post("/api/auth/refresh"))).andExpect(status().isTooManyRequests());
        });
    }
    @Test void defaultModeAndOrdinaryModeCatalogDoNotLoseSourceRestriction() {
        base.run(context -> {
            var mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            mvc.perform(client(get("/api/tracks/admin")).with(user(admin()))).andExpect(status().isForbidden());
            mvc.perform(get("/api/tracks/admin").header("X-ATStudio-Client-IP","192.0.2.10").with(user(admin()))).andExpect(status().isOk());
        });
        enabled().withPropertyValues("app.listening.enabled=false").run(context -> {
            var mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            mvc.perform(client(get("/api/tracks/admin/listening-catalog")).with(user(admin()))).andExpect(status().isForbidden());
        });
    }
    @ParameterizedTest @ValueSource(strings={"INVALID","ANY","AUTHENTICATED"})
    void unknownAccessModesFailBinding(String mode) {
        base.withPropertyValues("app.listening.operator-access="+mode).run(context -> assertThat(context).hasFailed());
    }
    @Configuration(proxyBeanMethods=false) @EnableWebMvc
    @EnableConfigurationProperties(ListeningProperties.class)
    @Import({SecurityConfig.class, ListeningSecurityConfiguration.class, ListeningCatalogController.class})
    static class Fixture {
        @Bean TrackRepository tracks() { return mock(TrackRepository.class); }
        @Bean AdminOperationAuditLogRepository auditLogs() { return mock(AdminOperationAuditLogRepository.class); }
        @Bean ListeningCatalogService catalog() { return mock(ListeningCatalogService.class); }
        @Bean JwtTokenProvider tokens() { return mock(JwtTokenProvider.class); }
        @Bean CustomUserDetailsService users() { return mock(CustomUserDetailsService.class); }
        @Bean JwtAuthenticationFilter jwt(JwtTokenProvider tokens,CustomUserDetailsService users) {return new JwtAuthenticationFilter(tokens,users);}
        @Bean TrustedClientIdentityResolver resolver() {
            var p=new TrustedClientIdentityProperties();p.setEnabled(true);
            return new TrustedClientIdentityResolver(p,new AcceptanceProperties());
        }
        @Bean AuthRateLimitFilter rate(TrustedClientIdentityResolver resolver) { return new AuthRateLimitFilter(new AuthRateLimitProperties(),resolver); }
        @Bean AcceptanceHostFilter host() {return new AcceptanceHostFilter(new AcceptanceProperties());}
        @Bean @Primary CorsConfigurationSource corsConfigurationSource() {var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",new CorsConfiguration());return s;}
        @Bean Handler handler() {return new Handler();}
    }
    @RestController static class Handler { @RequestMapping("/**") String ok(){return "local fixture";} }
}
