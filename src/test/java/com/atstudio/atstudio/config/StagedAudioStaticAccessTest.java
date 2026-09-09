package com.atstudio.atstudio.config;

import com.atstudio.atstudio.security.AcceptanceHostFilter;
import com.atstudio.atstudio.security.AuthRateLimitFilter;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.security.JwtAuthenticationFilter;
import com.atstudio.atstudio.security.JwtTokenProvider;
import com.atstudio.atstudio.security.TrustedClientIdentityResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(StagedAudioStaticAccessTest.MvcConfig.class)
class StagedAudioStaticAccessTest {
    private static final String OPERATION = "00000000-0000-0000-0000-000000000026";
    private static final byte[] AUDIO = "private-audio-fixture-wi026".getBytes(StandardCharsets.UTF_8);
    private static final byte[] THUMBNAIL = new byte[]{1, 2, 3};
    @TempDir static Path publicRoot;
    @Autowired WebApplicationContext applicationContext;
    private MockMvc mvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.public-path", publicRoot::toString);
    }

    @BeforeAll
    static void fixtures() throws Exception {
        for (String extension : new String[]{"wav", "mp3"}) {
            write(".staging/" + OPERATION + "/tracks/audio/fixture." + extension, AUDIO);
        }
        write("tracks/thumbnail/public.jpg", THUMBNAIL);
        write("tracks/audio/final.wav", AUDIO);
    }

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @CsvSource({"ANONYMOUS,.staging,wav", "USER,.staging,wav", "ADMIN,.staging,wav",
            "ANONYMOUS,.staging,mp3", "USER,.staging,mp3", "ADMIN,.staging,mp3",
            "ANONYMOUS,.STAGING,wav", "USER,.STAGING,wav", "ADMIN,.STAGING,wav",
            "ANONYMOUS,.STAGING,mp3", "USER,.STAGING,mp3", "ADMIN,.STAGING,mp3"})
    void stagedAudioIsDeniedForEveryRoleAndPathCase(String role, String directory, String extension) throws Exception {
        String path = "/uploads/" + directory + "/" + OPERATION + "/tracks/audio/fixture." + extension;
        var result = mvc.perform(asRole(get(path), role)).andReturn();
        assertThat(result.getResponse().getStatus())
                .as("role=%s path=%s body=%s", role, path, result.getResponse().getContentAsString())
                .isEqualTo(role.equals("ANONYMOUS") ? 401 : 403);
        assertThat(result.getResponse().getContentAsByteArray()).isNotEqualTo(AUDIO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANONYMOUS", "USER", "ADMIN"})
    void publicThumbnailStillServesItsActualBytes(String role) throws Exception {
        mvc.perform(asRole(get("/uploads/tracks/thumbnail/public.jpg"), role))
                .andExpect(status().isOk()).andExpect(content().bytes(THUMBNAIL));
    }

    @ParameterizedTest
    @CsvSource({"ANONYMOUS,tracks/audio", "USER,tracks/audio", "ADMIN,tracks/audio",
            "ANONYMOUS,TRACKS/AUDIO", "USER,TRACKS/AUDIO", "ADMIN,TRACKS/AUDIO",
            "ANONYMOUS,TrAcKs/AuDiO", "USER,TrAcKs/AuDiO", "ADMIN,TrAcKs/AuDiO"})
    void finalRawAudioIsDeniedForEveryRoleAndPathCase(String role, String directory) throws Exception {
        String path = "/uploads/" + directory + "/final.wav";
        var result = mvc.perform(asRole(get(path), role)).andReturn();
        assertThat(result.getResponse().getStatus())
                .as("role=%s path=%s body=%s", role, path, result.getResponse().getContentAsString())
                .isEqualTo(role.equals("ANONYMOUS") ? 401 : 403);
        assertThat(result.getResponse().getContentAsByteArray()).isNotEqualTo(AUDIO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"%2Estaging/", ".staging%2F"})
    void encodedDotOrSlashIsRejectedByTheSecurityFirewall(String prefix) throws Exception {
        URI uri = URI.create("/uploads/" + prefix + OPERATION + "/tracks/audio/fixture.wav");
        mvc.perform(get(uri)).andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder asRole(MockHttpServletRequestBuilder request, String role) {
        return role.equals("ANONYMOUS") ? request : request.with(user("synthetic-reviewer").roles(role));
    }

    private static void write(String key, byte[] bytes) throws Exception {
        Path file = publicRoot.resolve(key);
        Files.createDirectories(file.getParent());
        Files.write(file, bytes);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({WebConfig.class, SecurityConfig.class})
    static class MvcConfig {
        @Bean JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(mock(JwtTokenProvider.class), mock(CustomUserDetailsService.class));
        }

        @Bean AcceptanceHostFilter acceptanceHostFilter() {
            return new AcceptanceHostFilter(new AcceptanceProperties());
        }

        @Bean AuthRateLimitFilter authRateLimitFilter() {
            return new AuthRateLimitFilter(new AuthRateLimitProperties(), mock(TrustedClientIdentityResolver.class));
        }

        @Bean CorsConfigurationSource corsConfigurationSource() {
            return request -> null;
        }
    }
}
