package com.atstudio.atstudio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PublicThumbnailHeaderFilter extends OncePerRequestFilter {

    private static final String THUMBNAIL_PATH_PREFIX = "/uploads/playlists/thumbnails/";
    private static final String CONTENT_SECURITY_POLICY = "default-src 'none'; sandbox";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isPlaylistThumbnailRequest(request)) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
            response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPlaylistThumbnailRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String prefix = contextPath == null || contextPath.isBlank()
                ? THUMBNAIL_PATH_PREFIX
                : contextPath + THUMBNAIL_PATH_PREFIX;
        return requestUri.startsWith(prefix);
    }
}
