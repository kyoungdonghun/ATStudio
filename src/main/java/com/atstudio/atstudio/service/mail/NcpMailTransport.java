package com.atstudio.atstudio.service.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

/** Opt-in SENS MAIL v2 submission. A returned receipt means accepted, not delivered. */
@Component
public class NcpMailTransport {
    private static final String ORIGIN = "https://sens.apigw.ntruss.com";
    private static final int MAX_RESPONSE_BYTES = 65536;
    private final boolean enabled;
    private final String path;
    private final String accessKey;
    private final String secretKey;
    private final Clock clock;
    private final Exchange exchange;
    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    public NcpMailTransport(
            @Value("${app.mail.transport:smtp}") String transport,
            @Value("${app.mail.ncp.service-id:}") String serviceId,
            @Value("${app.mail.ncp.access-key:}") String accessKey,
            @Value("${app.mail.ncp.secret-key:}") String secretKey) {
        this(transport, serviceId, accessKey, secretKey, Clock.systemUTC(), defaultExchange());
    }

    NcpMailTransport(String transport, String serviceId, String accessKey, String secretKey,
                     Clock clock, Exchange exchange) {
        if (!"smtp".equals(transport) && !"ncp".equals(transport)) {
            throw new IllegalArgumentException("MAIL_TRANSPORT_INVALID");
        }
        enabled = "ncp".equals(transport);
        if (enabled && (serviceId == null || !serviceId.matches("ncp:mail:[A-Za-z0-9:_-]{1,180}")
                || !safeHeader(accessKey) || !safeHeader(secretKey))) {
            throw new IllegalArgumentException("NCP_MAIL_CONFIG_REQUIRED");
        }
        path = "/mail/v2/services/" + serviceId + "/requests";
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.clock = clock;
        this.exchange = exchange;
    }

    public boolean isEnabled() { return enabled; }

    public Accepted submit(String from, String to, String title, String body) throws IOException {
        if (!enabled) throw new IllegalStateException("NCP_MAIL_DISABLED");
        // MAIL interprets ${key} even without a saved template. Titles are plain text:
        // reject interpolation syntax rather than silently changing visible text.
        if (title == null || title.contains("${")) throw new IOException("NCP_MAIL_TITLE_INVALID");
        String timestamp = Long.toString(clock.millis());
        try {
            byte[] payload = json.writeValueAsBytes(Map.of(
                    "senderAddress", from, "title", title, "body", literalHtml(body),
                    "recipients", List.of(Map.of("address", to, "type", "TO")),
                    "individual", true, "advertising", false, "confirmAndSend", false));
            HttpRequest request = HttpRequest.newBuilder(URI.create(ORIGIN + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("x-ncp-apigw-timestamp", timestamp)
                    .header("x-ncp-iam-access-key", accessKey)
                    .header("x-ncp-apigw-signature-v2", signature(path, timestamp, accessKey, secretKey))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload)).build();
            // Exactly one application-level submission: no retry, redirect or SMTP fallback.
            HttpResponse<byte[]> response = exchange.send(request);
                if (response.statusCode() != 202) throw new IOException("NCP_MAIL_NOT_ACCEPTED");
                byte[] bytes = response.body();
                if (bytes.length > MAX_RESPONSE_BYTES) throw new IOException("NCP_MAIL_RESPONSE_INVALID");
                JsonNode result = json.readTree(bytes);
                String requestId = result.path("requestId").asText("");
                if (requestId.isBlank() || requestId.length() > 200 || result.path("requestCount").asInt(-1) != 1) {
                    throw new IOException("NCP_MAIL_RESPONSE_INVALID");
                }
                return new Accepted(requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("NCP_MAIL_OUTCOME_UNKNOWN");
        } catch (Exception e) {
            // Never include request, recipient, provider response or credentials in exceptions.
            throw new IOException("NCP_MAIL_SUBMISSION_UNCONFIRMED");
        }
    }

    static String literalHtml(String body) {
        // Preserve the rendered HTML while preventing NCP's server-side substitution.
        return body.replace("${", "&#36;{");
    }

    static String signature(String path, String timestamp, String accessKey, String secretKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String canonical = "POST " + path + "\n" + timestamp + "\n" + accessKey;
        return Base64.getEncoder().encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    private static boolean safeHeader(String value) {
        return value != null && !value.isBlank() && value.chars().allMatch(c -> c >= 33 && c <= 126);
    }

    private static Exchange defaultExchange() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        return request -> {
            var future = client.sendAsync(request, ignored -> new LimitedBody());
            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException e) {
                future.cancel(true);
                throw new IOException("NCP_MAIL_OUTCOME_UNKNOWN");
            } catch (InterruptedException e) {
                future.cancel(true);
                throw e;
            }
        };
    }

    @FunctionalInterface
    interface Exchange { HttpResponse<byte[]> send(HttpRequest request) throws IOException, InterruptedException; }

    static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final CompletableFuture<byte[]> body = new CompletableFuture<>();
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private Flow.Subscription subscription;
        public CompletionStage<byte[]> getBody() { return body; }
        public void onSubscribe(Flow.Subscription value) { subscription = value; value.request(1); }
        public void onNext(List<ByteBuffer> buffers) {
            for (ByteBuffer buffer : buffers) {
                if (buffer.remaining() > MAX_RESPONSE_BYTES - bytes.size()) {
                    subscription.cancel();
                    body.completeExceptionally(new IOException("NCP_MAIL_RESPONSE_LIMIT"));
                    return;
                }
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk); bytes.writeBytes(chunk);
            }
            subscription.request(1);
        }
        public void onError(Throwable failure) { body.completeExceptionally(new IOException("NCP_MAIL_RESPONSE_FAILED")); }
        public void onComplete() { body.complete(bytes.toByteArray()); }
    }

    /** Provider request ID is deliberately not logged; delivery reconciliation is a separate operation. */
    public record Accepted(String requestId) { }
}
