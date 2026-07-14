package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AcceptanceHostFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_HOSTS = Set.of("localhost", "127.0.0.1", "::1");
    private static final String INVALID_HOST_BODY =
            "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Invalid Host header.\"}";

    private final AcceptanceProperties acceptanceProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !acceptanceProperties.isEnabled();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Enumeration<String> hostValues = request.getHeaders("Host");
        if (hostValues == null || !hostValues.hasMoreElements()) {
            reject(response);
            return;
        }

        String hostValue = hostValues.nextElement();
        if (hostValues.hasMoreElements() || !isAllowedHost(hostValue)) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedHost(String value) {
        if (value == null || value.isBlank() || value.length() > 255 || !value.equals(value.trim())) {
            return false;
        }

        try {
            URI uri = new URI("http://" + value);
            String host = uri.getHost();
            if (host == null
                    || uri.getRawUserInfo() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || uri.getPort() > 65535) {
                return false;
            }
            return ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(INVALID_HOST_BODY);
    }
}
