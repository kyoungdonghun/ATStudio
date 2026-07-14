package com.atstudio.atstudio.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PublicThumbnailHeaderFilter 테스트")
class PublicThumbnailHeaderFilterTest {

    private final PublicThumbnailHeaderFilter filter = new PublicThumbnailHeaderFilter();

    @Test
    @DisplayName("playlist thumbnail responses receive fixed safe headers")
    void doFilter_playlistThumbnail_setsSafeHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/uploads/playlists/thumbnails/generated.jpg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("Content-Security-Policy"))
                .isEqualTo("default-src 'none'; sandbox");
        assertThat(response.getHeader("Cross-Origin-Resource-Policy")).isEqualTo("same-origin");
    }

    @Test
    @DisplayName("non-playlist uploads stay outside the thumbnail header boundary")
    void doFilter_otherUpload_doesNotSetThumbnailHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/uploads/tracks/thumbnail/generated.jpg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isNull();
        assertThat(response.getHeader("Content-Security-Policy")).isNull();
    }
}
