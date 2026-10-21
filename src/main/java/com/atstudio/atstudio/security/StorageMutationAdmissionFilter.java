package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.StorageMutationAdmissionProperties;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** Bounds HTTP storage work before body parsing and transaction acquisition, per application instance. */
public final class StorageMutationAdmissionFilter extends OncePerRequestFilter {

    private static final String LEASE_ATTRIBUTE = StorageMutationAdmissionFilter.class.getName() + ".lease";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Set<String> DOMAINS = Set.of(
            "tracks", "albums", "playlists", "notices", "questions", "company-certifications");

    private final StorageMutationAdmissionProperties properties;
    private final TrustedClientIdentityResolver identityResolver;
    private final HttpFirewall firewall;
    private final byte[] fingerprintSalt = new byte[32];
    private final Map<String, Integer> activeClients = new HashMap<>();
    private int activeRequests;

    public StorageMutationAdmissionFilter(StorageMutationAdmissionProperties properties,
            TrustedClientIdentityResolver identityResolver, HttpFirewall firewall) {
        this.properties = properties;
        this.identityResolver = identityResolver;
        this.firewall = firewall;
        new SecureRandom().nextBytes(fingerprintSalt);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        Lease lease = (Lease) request.getAttribute(LEASE_ATTRIBUTE);
        if (lease == null) {
            // Reuse the security chain's firewall before any servlet-level form/multipart parsing.
            try {
                firewall.getFirewalledRequest(request);
            } catch (RequestRejectedException exception) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (!properties.isEnabled() || !isStorageMutation(request)) {
                chain.doFilter(request, response);
                return;
            }
            lease = tryAcquire(fingerprint(identityResolver.resolve(request)));
            if (lease == null) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Retry-After", Integer.toString(properties.getRetryAfterSeconds()));
                response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Storage mutation capacity exceeded.\"}");
                return;
            }
            request.setAttribute(LEASE_ATTRIBUTE, lease);
        }

        lease.enter();
        try {
            chain.doFilter(new AdmissionRequest(request, response, lease), response);
        } finally {
            lease.exit();
        }
    }

    static boolean isStorageMutation(HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return false;
        }
        // StrictHttpFirewall already rejects ambiguous separators, dot segments and matrix parameters.
        // Decode allowed percent-encoded letters exactly once, just as MVC path matching does.
        String path = UriUtils.decode(request.getRequestURI().substring(request.getContextPath().length()),
                StandardCharsets.UTF_8);
        if ("POST".equals(request.getMethod()) && "/api/tracks/batch".equals(path)) {
            return false; // Read-only player hydration, not a storage mutation.
        }
        if (!path.startsWith("/api/")) {
            return false;
        }
        int end = path.indexOf('/', 5);
        String domain = end < 0 ? path.substring(5) : path.substring(5, end);
        return DOMAINS.contains(domain);
    }

    private synchronized Lease tryAcquire(String client) {
        int clientCount = activeClients.getOrDefault(client, 0);
        if (activeRequests >= properties.getMaxConcurrent()
                || clientCount >= properties.getMaxConcurrentPerIp()) {
            return null;
        }
        activeRequests++;
        activeClients.put(client, clientCount + 1);
        return new Lease(client);
    }

    private String fingerprint(String client) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fingerprintSalt);
            return HexFormat.of().formatHex(digest.digest(client.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private synchronized void release(String client) {
        activeRequests--;
        activeClients.computeIfPresent(client, (key, count) -> count == 1 ? null : count - 1);
    }

    synchronized int activeRequestCount() {
        return activeRequests;
    }

    synchronized int trackedClientCount() {
        return activeClients.size();
    }

    private final class Lease implements AsyncListener {
        private final String client;
        private int dispatches;
        private boolean async;
        private boolean terminal;
        private boolean released;

        private Lease(String client) {
            this.client = client;
        }

        synchronized void enter() {
            dispatches++;
        }

        synchronized void started(AsyncContext context) {
            if (!async) {
                // Register before returning startAsync to application code, closing the completion race.
                context.addListener(this);
                async = true;
            }
        }

        synchronized void exit() {
            dispatches--;
            releaseIfFinished();
        }

        private synchronized void finish() {
            terminal = true;
            releaseIfFinished();
        }

        private void releaseIfFinished() {
            if (!released && dispatches == 0 && (!async || terminal)) {
                released = true;
                release(client);
            }
        }

        @Override
        public void onComplete(AsyncEvent event) {
            finish();
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            // Timeout listeners may dispatch or keep working. Only onComplete ends ownership.
        }

        @Override
        public void onError(AsyncEvent event) {
            // Error notification may precede an error dispatch and transaction cleanup.
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            event.getAsyncContext().addListener(this);
        }
    }

    private static final class AdmissionRequest extends HttpServletRequestWrapper {
        private final HttpServletResponse response;
        private final Lease lease;

        private AdmissionRequest(HttpServletRequest request, HttpServletResponse response, Lease lease) {
            super(request);
            this.response = response;
            this.lease = lease;
        }

        @Override
        public AsyncContext startAsync() {
            return startAsync(this, response);
        }

        @Override
        public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
            AsyncContext context = super.startAsync(request, response);
            lease.started(context);
            return context;
        }
    }
}
