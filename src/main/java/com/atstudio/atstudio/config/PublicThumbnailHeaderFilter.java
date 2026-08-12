package com.atstudio.atstudio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class PublicThumbnailHeaderFilter extends OncePerRequestFilter {

    private static final List<String> THUMBNAIL_PATH_PREFIXES = List.of(
            "/uploads/albums/thumbnails/",
            "/uploads/playlists/thumbnails/");
    private static final String CONTENT_SECURITY_POLICY = "default-src 'none'; sandbox";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isPublicThumbnailRequest(request)) {
            HttpServletResponse fixedContentTypeResponse = new FixedJpegContentTypeResponse(response);
            fixedContentTypeResponse.setContentType(MediaType.IMAGE_JPEG_VALUE);
            fixedContentTypeResponse.setHeader("X-Content-Type-Options", "nosniff");
            fixedContentTypeResponse.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
            fixedContentTypeResponse.setHeader("Cross-Origin-Resource-Policy", "same-origin");
            filterChain.doFilter(request, fixedContentTypeResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicThumbnailRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String normalizedContextPath = contextPath == null || contextPath.isBlank()
                ? ""
                : contextPath;
        return THUMBNAIL_PATH_PREFIXES.stream()
                .map(prefix -> normalizedContextPath + prefix)
                .anyMatch(requestUri::startsWith);
    }

    private static final class FixedJpegContentTypeResponse extends HttpServletResponseWrapper {

        private FixedJpegContentTypeResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setContentType(String type) {
            super.setContentType(MediaType.IMAGE_JPEG_VALUE);
        }

        @Override
        public void setHeader(String name, String value) {
            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                super.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
                return;
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                super.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
                return;
            }
            super.addHeader(name, value);
        }

        @Override
        public String getContentType() {
            return MediaType.IMAGE_JPEG_VALUE;
        }
    }
}
