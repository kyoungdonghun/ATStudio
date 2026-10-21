package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.StorageMutationAdmissionProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class StorageMutationAdmissionFilterTest {

    private StorageMutationAdmissionProperties properties;
    private StorageMutationAdmissionFilter filter;

    @BeforeEach
    void setUp() {
        properties = new StorageMutationAdmissionProperties();
        TrustedClientIdentityProperties identity = new TrustedClientIdentityProperties();
        identity.setEnabled(true);
        filter = new StorageMutationAdmissionFilter(properties,
                new TrustedClientIdentityResolver(identity, new AcceptanceProperties()), new StrictHttpFirewall());
    }

    @ParameterizedTest
    @CsvSource({
            "POST,/api/tracks", "PUT,/api/tracks/1", "DELETE,/api/tracks/1",
            "POST,/api/tracks/admin/1/audio-processing/retry",
            "POST,/api/albums", "PUT,/api/albums/1", "DELETE,/api/albums/1",
            "POST,/api/playlists", "PUT,/api/playlists/1", "DELETE,/api/playlists/1",
            "POST,/api/notices", "PUT,/api/notices/1", "DELETE,/api/notices/1",
            "POST,/api/questions", "DELETE,/api/questions/1",
            "POST,/api/company-certifications", "POST,/api/company-certifications/me/documents",
            "PUT,/api/company-certifications/1", "PATCH,/api/notices/1"
    })
    void coversEveryStorageDomainIndependentlyOfContentType(String method, String path) throws Exception {
        MockAsyncContext held = hold("192.0.2.1");
        try {
            for (String type : List.of("application/json", "multipart/form-data; boundary=x",
                    "application/x-www-form-urlencoded", "text/plain")) {
                MockHttpServletRequest request = spy(request(method, path, "192.0.2.1"));
                request.setContentType(type);
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(request, response, (req, res) -> {
                    throw new AssertionError("Rejected request reached body/service work");
                });
                assertThat(response.getStatus()).isEqualTo(429);
                assertThat(response.getHeader("Retry-After")).isEqualTo("2");
                assertThat(response.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
                verify(request, never()).getInputStream();
                verify(request, never()).getReader();
                verify(request, never()).getParts();
                verify(request, never()).getParameterMap();
            }
        } finally {
            held.complete();
        }
        assertEmpty();
    }

    @Test
    void rejectsBeforeServletFormContentFilterReadsPutBody() throws Exception {
        MockAsyncContext held = hold("192.0.2.1");
        MockHttpServletRequest request = spy(request("PUT", "/api/notices/1", "192.0.2.1"));
        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("deleteAttachments=1".getBytes());
        try {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> new FormContentFilter().doFilter(req, res,
                    (parsed, result) -> { throw new AssertionError("Body parser reached"); }));
            assertThat(response.getStatus()).isEqualTo(429);
            verify(request, never()).getInputStream();
        } finally {
            held.complete();
        }
    }

    @Test
    void globalAndPerIpBudgetsAreSharedAcrossRoutesAndRecovered() throws Exception {
        MockAsyncContext first = hold("192.0.2.1");
        MockAsyncContext second = hold("192.0.2.2");
        assertRejected("192.0.2.3");
        first.complete();
        assertRejected("192.0.2.2");
        MockAsyncContext third = hold("192.0.2.3");
        second.complete();
        third.complete();
        assertEmpty();
        for (int i = 1; i <= 100; i++) {
            filter.doFilter(request("DELETE", "/api/notices/1", "198.51.100." + i),
                    new MockHttpServletResponse(), (req, res) -> {});
        }
        assertEmpty();
    }

    @Test
    void simultaneousRequestsNeverExceedGlobalBoundAndRejectedRequestsDoNotWait() throws Exception {
        var executor = Executors.newFixedThreadPool(12);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch admitted = new CountDownLatch(2);
        CountDownLatch rejected = new CountDownLatch(10);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        var futures = new ArrayList<java.util.concurrent.Future<?>>();
        try {
            for (int i = 1; i <= 12; i++) {
                String ip = "192.0.2." + i;
                futures.add(executor.submit(() -> {
                    assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    filter.doFilter(request("POST", "/api/tracks", ip), response, (req, res) -> {
                        peak.accumulateAndGet(active.incrementAndGet(), Math::max);
                        admitted.countDown();
                        try {
                            assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new ServletException(exception);
                        } finally {
                            active.decrementAndGet();
                        }
                    });
                    if (response.getStatus() == 429) rejected.countDown();
                    return null;
                }));
            }
            start.countDown();
            assertThat(admitted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(rejected.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(peak.get()).isEqualTo(2);
            release.countDown();
            for (var future : futures) future.get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
        assertEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"io", "servlet", "runtime", "error"})
    void synchronousFailureReturnsPermit(String failure) {
        Throwable expected = switch (failure) {
            case "io" -> new IOException("synthetic disconnect");
            case "servlet" -> new ServletException("synthetic");
            case "runtime" -> new IllegalStateException("synthetic");
            default -> new AssertionError("synthetic");
        };
        assertThatThrownBy(() -> filter.doFilter(request("POST", "/api/tracks", "192.0.2.1"),
                new MockHttpServletResponse(), (req, res) -> {
                    if (expected instanceof IOException e) throw e;
                    if (expected instanceof ServletException e) throw e;
                    if (expected instanceof RuntimeException e) throw e;
                    throw (Error) expected;
                })).isSameAs(expected);
        assertEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"timeout", "error"})
    void notificationDoesNotReleaseBeforeContinuedWorkRedispatchAndCompletion(String signal) throws Exception {
        MockAsyncContext context = hold("192.0.2.1");
        var listener = context.getListeners().get(0);
        AsyncEvent event = new AsyncEvent(context);
        if (signal.equals("timeout")) listener.onTimeout(event);
        else listener.onError(event);
        assertRejected("192.0.2.1");
        assertThat(filter.activeRequestCount()).isEqualTo(1);

        context.dispatch();
        MockHttpServletRequest original = (MockHttpServletRequest)
                org.springframework.web.util.WebUtils.getNativeRequest(context.getRequest(), MockHttpServletRequest.class);
        original.setDispatcherType(DispatcherType.ASYNC);
        original.setAsyncStarted(false);
        AtomicReference<AsyncContext> next = new AtomicReference<>();
        filter.doFilter(context.getRequest(), context.getResponse(), (req, res) -> {
            assertThat(filter.activeRequestCount()).isEqualTo(1);
            assertRejected("192.0.2.1");
            next.set(req.startAsync());
        });
        // Mock servlet does not emit onStartAsync automatically for the next cycle.
        listener.onStartAsync(new AsyncEvent(next.get()));
        assertRejected("192.0.2.1");
        ((MockAsyncContext) next.get()).complete();
        listener.onComplete(event);
        assertEmpty();
    }

    @Test
    void earlyAsyncCompleteStillWaitsForOriginalDispatchCleanup() throws Exception {
        filter.doFilter(request("POST", "/api/tracks", "192.0.2.1"),
                new MockHttpServletResponse(), (req, res) -> {
                    ((MockAsyncContext) req.startAsync()).complete();
                    assertThat(filter.activeRequestCount()).isEqualTo(1);
                    assertRejected("192.0.2.1");
                });
        assertEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/%74racks", "/api/tracks/", "/api/tracks/%31"})
    void decodedAndTrailingVariantsCannotBypassAdmission(String path) throws Exception {
        MockAsyncContext held = hold("192.0.2.1");
        try {
            var response = new MockHttpServletResponse();
            filter.doFilter(request("PUT", path, "192.0.2.1"), response,
                    (req, res) -> { throw new AssertionError("Variant bypassed"); });
            assertThat(response.getStatus()).isEqualTo(429);
        } finally {
            held.complete();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api//tracks", "/api/./tracks", "/api/x/../tracks", "/api/tracks;x=1",
            "/api%2ftracks", "/api%2Ftracks", "/api/%2e/tracks", "/api/%2574racks", "/api\\tracks"})
    void firewallRejectsAmbiguousVariantsBeforeChain(String path) throws Exception {
        var response = new MockHttpServletResponse();
        filter.doFilter(request("POST", path, "192.0.2.1"), response,
                (req, res) -> { throw new AssertionError("Firewall bypassed"); });
        assertThat(response.getStatus()).isEqualTo(400);
        assertEmpty();
    }

    @Test
    void contextPathIsExcludedFromClassification() throws Exception {
        MockAsyncContext held = hold("192.0.2.1");
        try {
            var request = request("DELETE", "/studio/api/notices/1", "192.0.2.1");
            request.setContextPath("/studio");
            request.setServletPath("/api/notices/1");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> { throw new AssertionError("Context bypass"); });
            assertThat(response.getStatus()).isEqualTo(429);
        } finally {
            held.complete();
        }
    }

    @Test
    void readOnlyRequestsAndHydrationRemainIndependent() throws Exception {
        MockAsyncContext first = hold("192.0.2.1");
        MockAsyncContext second = hold("192.0.2.2");
        try {
            for (String path : List.of("/api/tracks", "/api/tracks/1/stream", "/api/notices", "/api/playlists")) {
                var response = new MockHttpServletResponse();
                filter.doFilter(request("GET", path, "192.0.2.1"), response, (req, res) -> {});
                assertThat(response.getStatus()).isEqualTo(200);
            }
            var response = new MockHttpServletResponse();
            filter.doFilter(request("POST", "/api/tracks/batch", "192.0.2.1"), response, (req, res) -> {});
            assertThat(response.getStatus()).isEqualTo(200);
        } finally {
            first.complete();
            second.complete();
        }
    }

    @Test
    void onlyTrustedSingleLiteralAssertionSeparatesClients() throws Exception {
        var first = request("POST", "/api/tracks", "127.0.0.1");
        first.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "192.0.2.1");
        var second = request("POST", "/api/notices", "127.0.0.1");
        second.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "192.0.2.2");
        MockAsyncContext one = hold(first);
        MockAsyncContext two = hold(second);
        one.complete();
        two.complete();
        MockAsyncContext direct = hold("198.51.100.1");
        try {
            var spoofed = request("DELETE", "/api/albums/1", "198.51.100.1");
            spoofed.addHeader("X-Forwarded-For", "192.0.2.3");
            spoofed.addHeader(TrustedClientIdentityResolver.INTERNAL_CLIENT_IP_HEADER, "192.0.2.4");
            var response = new MockHttpServletResponse();
            filter.doFilter(spoofed, response, (req, res) -> { throw new AssertionError("Spoof bypass"); });
            assertThat(response.getStatus()).isEqualTo(429);
        } finally {
            direct.complete();
        }
        assertEmpty();
    }

    @Test
    void disabledGatePassesWithoutCounters() throws Exception {
        properties.setEnabled(false);
        filter.doFilter(request("POST", "/api/tracks", "192.0.2.1"), new MockHttpServletResponse(),
                (req, res) -> assertThat(filter.activeRequestCount()).isZero());
        assertEmpty();
    }

    @Test
    void liveCountersRetainOnlyFingerprintsAndEraseThemOnCompletion() throws Exception {
        MockAsyncContext held = hold("192.0.2.1");
        try {
            var counters = (java.util.Map<?, ?>) org.springframework.test.util.ReflectionTestUtils
                    .getField(filter, "activeClients");
            assertThat(counters).hasSize(1);
            assertThat(counters.keySet().iterator().next().toString()).matches("[0-9a-f]{64}");
            assertThat(counters.toString()).doesNotContain("192.0.2.1");
        } finally {
            held.complete();
        }
        assertEmpty();
    }

    private MockAsyncContext hold(String ip) throws Exception {
        return hold(request("POST", "/api/tracks", ip));
    }

    private MockAsyncContext hold(MockHttpServletRequest request) throws Exception {
        AtomicReference<MockAsyncContext> context = new AtomicReference<>();
        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> context.set((MockAsyncContext) req.startAsync()));
        assertThat(context.get()).isNotNull();
        return context.get();
    }

    private void assertRejected(String ip) throws IOException, ServletException {
        var response = new MockHttpServletResponse();
        filter.doFilter(request("DELETE", "/api/notices/1", ip), response,
                (req, res) -> { throw new AssertionError("Capacity exceeded"); });
        assertThat(response.getStatus()).isEqualTo(429);
    }

    private MockHttpServletRequest request(String method, String path, String ip) {
        var request = new MockHttpServletRequest(method, path);
        request.setServletPath(UriUtils.decode(path, StandardCharsets.UTF_8));
        request.setRemoteAddr(ip);
        request.setAsyncSupported(true);
        return request;
    }

    private void assertEmpty() {
        assertThat(filter.activeRequestCount()).isZero();
        assertThat(filter.trackedClientCount()).isZero();
    }
}
