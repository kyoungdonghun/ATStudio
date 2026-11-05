package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.*;
import com.atstudio.atstudio.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.URI;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** No Boot context, datasource, mail or provider: synthetic MVC endpoints + real security chain. */
@SpringJUnitWebConfig(ListeningSecurityTest.Config.class)
@TestPropertySource(properties = "app.listening.enabled=true")
class ListeningSecurityTest {
    @Autowired WebApplicationContext context;
    @Autowired TrackRepository tracks;
    @Autowired JwtTokenProvider tokens;
    @Autowired com.atstudio.atstudio.service.TrackService trackService;
    MockMvc mvc;

    @BeforeEach void setup() {
        reset(tracks, tokens, trackService);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/tracks", "/api/tracks/1", "/api/tracks/1/stream", "/api/tags", "/api/tags/available"})
    void staleVisitorTokenNeverGetsParsed(String path) throws Exception {
        mvc.perform(get(path).header("Authorization", "Bearer stale-fixture")).andExpect(status().isOk());
        verifyNoInteractions(tokens);
    }

    @Test void readOnlyBatchRemainsPublic() throws Exception {
        mvc.perform(post("/api/tracks/batch")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/payments", "/api/users/me", "/api/tracks/2/download", "/api/albums/2",
            "/swagger-ui/index.html", "/v3/api-docs", "/actuator", "/uploads/tracks/audio/a.mp3",
            "/uploads/TRACKS/thumbnail/a.jpg", "/api/tracks/admin", "/api/tracks/admin/listening-catalog", "/api/tracks/admin/2/thumbnail", "/login"})
    void visitorsAreDeniedEvenWithAdminSession(String path) throws Exception {
        mvc.perform(get(path).with(user("fixture").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/tracks/1/", "/api/tracks/01", "/api/tracks/1;v=2", "/api/tracks/%31",
            "/api//tracks/1", "/api/tracks/../users/me", "/API/tracks/1"})
    void nonCanonicalVariantsNeverReachEndpoint(String path) throws Exception {
        int status = mvc.perform(get(URI.create(path))).andReturn().getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(status).isIn(400, 403);
    }

    @Test void methodAndOperatorRoleRestrictions() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").with(user("fixture").roles("ADMIN"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isForbidden());
        mvc.perform(put("/api/tracks/1").with(user("fixture").roles("USER"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isForbidden());
        mvc.perform(put("/api/tracks/1").with(user("fixture").roles("ADMIN"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isOk());
        mvc.perform(put("/api/tracks/admin/listening-catalog").with(user("fixture").roles("USER"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isForbidden());
        mvc.perform(put("/api/tracks/admin/listening-catalog").with(user("fixture").roles("ADMIN"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isOk());
        mvc.perform(get("/api/tags/admin").with(user("fixture").roles("ADMIN"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isOk());
        mvc.perform(get("/api/tracks/admin/2/thumbnail").with(user("fixture").roles("USER"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isForbidden());
        mvc.perform(get("/api/tracks/admin/2/thumbnail").with(user("fixture").roles("ADMIN"))
                .header("X-ATStudio-Client-IP", "192.0.2.10")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").header("X-ATStudio-Client-IP", "192.0.2.10"))
                .andExpect(status().isOk());
    }

    @Test void forwardedSpoofAndMissingOrDuplicateAssertionsFailClosed() throws Exception {
        mvc.perform(post("/api/auth/login").header("X-Forwarded-For", "192.0.2.10"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").header("X-ATStudio-Client-IP", "192.0.2.10", "192.0.2.10"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").header("X-ATStudio-Client-IP", "192.0.2.10")
                .with(req -> { req.setRemoteAddr("198.51.100.1"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test void onlySelectedActiveCoverIsServed() throws Exception {
        var selected = new org.springframework.core.io.ByteArrayResource(new byte[]{1, 2});
        when(trackService.getPublicThumbnail("selected.jpg")).thenReturn(selected);
        when(trackService.getPublicThumbnail("hidden.jpg"))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        mvc.perform(get("/uploads/tracks/thumbnail/selected.jpg")).andExpect(status().isOk());
        mvc.perform(get("/uploads/tracks/thumbnail/hidden.jpg")).andExpect(status().isNotFound());
        verify(trackService).getPublicThumbnail("selected.jpg");
        verify(trackService).getPublicThumbnail("hidden.jpg");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc @EnableWebSecurity
    @Import({ListeningSecurityConfiguration.class, com.atstudio.atstudio.controller.ListeningThumbnailController.class})
    static class Config {
        @Bean ListeningProperties listeningProperties() {
            var p = new ListeningProperties(); p.setEnabled(true); p.setTrackIds(Set.of(1L));
            p.setOperatorAddresses(Set.of("192.0.2.10")); return p;
        }
        @Bean TrustedClientIdentityResolver resolver() {
            var p = new TrustedClientIdentityProperties(); p.setEnabled(true);
            return new TrustedClientIdentityResolver(p, new AcceptanceProperties());
        }
        @Bean com.atstudio.atstudio.service.TrackService trackService() { return mock(com.atstudio.atstudio.service.TrackService.class); }
        @Bean com.atstudio.atstudio.service.ListeningCatalogService catalog() { return mock(com.atstudio.atstudio.service.ListeningCatalogService.class); }
        @Bean com.atstudio.atstudio.repository.AdminOperationAuditLogRepository audits() { return mock(com.atstudio.atstudio.repository.AdminOperationAuditLogRepository.class); }
        @Bean TrackRepository tracks() { return mock(TrackRepository.class); }
        @Bean JwtTokenProvider tokens() { return mock(JwtTokenProvider.class); }
        @Bean JwtAuthenticationFilter jwt(JwtTokenProvider tokens) {
            return new JwtAuthenticationFilter(tokens, mock(CustomUserDetailsService.class));
        }
        @Bean AuthRateLimitFilter rate(TrustedClientIdentityResolver resolver) {
            var p = new AuthRateLimitProperties(); p.setEnabled(false);
            return new AuthRateLimitFilter(p, resolver);
        }
        @Bean AcceptanceHostFilter host() { return new AcceptanceHostFilter(new AcceptanceProperties()); }
        @Bean FixtureController fixture() { return new FixtureController(); }
    }

    @RestController
    static class FixtureController {
        @RequestMapping("/**") String ok() { return "synthetic fixture"; }
    }
}
