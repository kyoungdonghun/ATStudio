package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.repository.TrackRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/** Runs before JWT parsing: visitors cannot trigger account lookups or token refresh behavior. */
@RequiredArgsConstructor
public class ListeningAccessFilter extends OncePerRequestFilter {
    private final ListeningProperties properties;
    private final TrustedClientIdentityResolver clientIdentity;
    private final TrackRepository tracks;

    public static boolean publicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        return (method.equals("GET") && (path.equals("/api/tracks")
                || path.matches("/api/tracks/[1-9][0-9]*(/stream)?")
                || path.equals("/api/tags") || path.equals("/api/tags/available")
                || path.matches("/uploads/tracks/thumbnail/[A-Za-z0-9_-]+\\.[A-Za-z0-9]+")
                || path.equals("/") || path.equals("/tracks") || path.matches("/tracks/[1-9][0-9]*")
                || path.startsWith("/assets/") || path.equals("/favicon.ico")))
                || (method.equals("POST") && path.equals("/api/tracks/batch"));
    }

    public static boolean operatorRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return switch (request.getMethod()) {
            case "GET" -> path.equals("/login") || path.equals("/api/users/me")
                    || path.equals("/api/utils/public-capabilities") || path.equals("/api/tags/admin") || path.equals("/api/tracks/admin/listening-catalog")
                    || path.matches("/api/tracks/admin(/[1-9][0-9]*(/(audio-processing|thumbnail))?)?")
                    || path.equals("/admin") || path.equals("/admin/track-manage")
                    || path.equals("/admin/tracks/upload") || path.matches("/admin/tracks/[1-9][0-9]*/edit");
            case "POST" -> path.equals("/api/tracks/admin/listening-catalog/reconcile") || path.matches("/api/auth/(login|refresh|logout)") || path.equals("/api/tracks")
                    || path.matches("/api/tracks/admin/[1-9][0-9]*/audio-processing/retry");
            case "PUT" -> path.equals("/api/tracks/admin/listening-catalog") || path.matches("/api/tracks/[1-9][0-9]*");
            default -> false;
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.contains("%") || path.contains(";") || path.contains("\\")
                || path.contains("//") || path.contains("/.") || !request.getContextPath().isEmpty()) {
            response.sendError(400);
            return;
        }
        if (publicRequest(request)) {
            response.setHeader("Cache-Control", "no-store");
            chain.doFilter(new HttpServletRequestWrapper(request) {
                @Override public String getHeader(String name) {
                    return name.equalsIgnoreCase("Authorization") ? null : super.getHeader(name);
                }
                @Override public Enumeration<String> getHeaders(String name) {
                    return name.equalsIgnoreCase("Authorization") ? Collections.emptyEnumeration() : super.getHeaders(name);
                }
            }, response);
            return;
        }
        boolean operator = operatorSource(request, properties, clientIdentity);
        if (!operatorRequest(request) || !operator) {
            response.sendError(403);
            return;
        }
        chain.doFilter(request, response);
    }

    public static boolean operatorSource(HttpServletRequest request, ListeningProperties properties,
            TrustedClientIdentityResolver clientIdentity) throws IOException {
        String client = clientIdentity.resolve(request);
        // A proxy's loopback fallback must never grant operator access when its assertion is absent/invalid.
        return TrustedClientIdentityResolver.parseLiteral(client).isPresent()
                && !java.net.InetAddress.getByName(client).isLoopbackAddress()
                && properties.getOperatorAddresses().contains(client);
    }
}
