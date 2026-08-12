package com.atstudio.atstudio.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(PublicThumbnailStaticResourceTest.MvcConfig.class)
@DisplayName("Public thumbnail static-resource response security")
class PublicThumbnailStaticResourceTest {

    private static final Path PUBLIC_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"),
            "atstudio-wi039-static-" + UUID.randomUUID());

    @Autowired
    WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void storageProperties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.public-path", PUBLIC_ROOT::toString);
    }

    @BeforeAll
    static void createRetainedFixtures() throws IOException {
        write("albums/thumbnails/retained.svg", "<svg onload=alert(1)></svg>");
        write("playlists/thumbnails/retained.html", "<html><script>alert(1)</script></html>");
        write("tracks/thumbnail/unrelated.svg", "<svg></svg>");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilters(new PublicThumbnailHeaderFilter())
                .build();
    }

    @AfterAll
    static void removeRetainedFixtures() throws IOException {
        if (!Files.exists(PUBLIC_ROOT)) {
            return;
        }
        try (var paths = Files.walk(PUBLIC_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    @DisplayName("Album retained SVG is served only as fixed JPEG with safe headers")
    void albumRetainedSvg_forcesSafeThumbnailHeaders() throws Exception {
        assertFixedThumbnailHeaders("/uploads/albums/thumbnails/retained.svg");
    }

    @Test
    @DisplayName("Playlist retained HTML is served only as fixed JPEG with safe headers")
    void playlistRetainedHtml_forcesSafeThumbnailHeaders() throws Exception {
        assertFixedThumbnailHeaders("/uploads/playlists/thumbnails/retained.html");
    }

    @Test
    @DisplayName("Unrelated retained upload keeps static MIME inference and no thumbnail headers")
    void unrelatedRetainedSvg_staysOutsideThumbnailPolicy() throws Exception {
        mockMvc.perform(get("/uploads/tracks/thumbnail/unrelated.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"))
                .andExpect(header().doesNotExist("X-Content-Type-Options"))
                .andExpect(header().doesNotExist("Content-Security-Policy"))
                .andExpect(header().doesNotExist("Cross-Origin-Resource-Policy"));
    }

    private void assertFixedThumbnailHeaders(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(
                        "Content-Security-Policy",
                        "default-src 'none'; sandbox"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"));
    }

    private static void write(String relativePath, String content) throws IOException {
        Path target = PUBLIC_ROOT.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import(WebConfig.class)
    static class MvcConfig {
    }
}
