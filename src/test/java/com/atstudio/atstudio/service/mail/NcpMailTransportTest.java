package com.atstudio.atstudio.service.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NcpMailTransportTest {
    private static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(1700000000000L), ZoneOffset.UTC);
    private NcpMailTransport transport(NcpMailTransport.Exchange exchange) {
        return new NcpMailTransport("ncp", "ncp:mail:kr:test:main", "test-access", "test-secret", CLOCK, exchange);
    }
    @SuppressWarnings("unchecked")
    private HttpResponse<byte[]> response(int status, String body) {
        HttpResponse<byte[]> result = mock(HttpResponse.class);
        when(result.statusCode()).thenReturn(status);
        if (status == 202) when(result.body()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        return result;
    }
    @Test void signingMatchesIndependentPythonVector() throws Exception {
        assertThat(NcpMailTransport.signature("/mail/v2/services/ncp:mail:kr:test:main/requests", "1700000000000", "test-access", "test-secret"))
                .isEqualTo("zxDbVMaUALjDawFVX7d4pJHB94bomQjINkm71DjD8VA=");
    }
    @Test void submitsUtf8OnceAndReturnsAcceptanceNotDelivery() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        NcpMailTransport sender = transport(request -> {
            calls.incrementAndGet();
            assertThat(request.uri().toString()).isEqualTo("https://sens.apigw.ntruss.com/mail/v2/services/ncp:mail:kr:test:main/requests");
            assertThat(request.timeout().orElseThrow().toSeconds()).isEqualTo(10);
            assertThat(request.headers().firstValue("x-ncp-apigw-signature-v2").orElseThrow()).isEqualTo("zxDbVMaUALjDawFVX7d4pJHB94bomQjINkm71DjD8VA=");
            var json = new ObjectMapper().readTree(body(request));
            assertThat(json.path("title").asText()).isEqualTo("메일 인증");
            assertThat(json.path("body").asText()).isEqualTo("<b>한글 & token</b>");
            assertThat(json.path("senderAddress").asText()).isEqualTo("from@example.test");
            assertThat(json.path("recipients").get(0).path("type").asText()).isEqualTo("TO");
            assertThat(json.path("recipients").get(0).path("address").asText()).isEqualTo("to@example.test");
            assertThat(json.path("individual").asBoolean()).isTrue();
            assertThat(json.path("advertising").asBoolean()).isFalse();
            return response(202, "{\"requestId\":\"opaque-request\",\"requestCount\":1}");
        });
        assertThat(sender.submit("from@example.test", "to@example.test", "메일 인증", "<b>한글 & token</b>").requestId()).isEqualTo("opaque-request");
        assertThat(calls).hasValue(1);
    }
    @ParameterizedTest @ValueSource(ints = {200, 301, 400, 401, 429, 500})
    void errorsAreSanitizedAndNeverRetried(int status) {
        AtomicInteger calls = new AtomicInteger();
        NcpMailTransport sender = transport(request -> { calls.incrementAndGet(); return response(status, "private-provider-message"); });
        assertThatThrownBy(() -> sender.submit("from", "to", "subject", "token")).isInstanceOf(IOException.class)
                .hasMessage("NCP_MAIL_SUBMISSION_UNCONFIRMED").hasNoCause();
        assertThat(calls).hasValue(1);
    }
    @Test void timeoutIsUnknownAndNeverRetried() {
        AtomicInteger calls = new AtomicInteger();
        NcpMailTransport sender = transport(request -> { calls.incrementAndGet(); throw new HttpTimeoutException("token-secret-recipient"); });
        assertThatThrownBy(() -> sender.submit("from", "to", "subject", "token")).hasMessage("NCP_MAIL_SUBMISSION_UNCONFIRMED").hasNoCause();
        assertThat(calls).hasValue(1);
    }
    @Test void completedHtmlInterpolationSyntaxIsRenderedLiterally() throws Exception {
        NcpMailTransport sender = transport(request -> {
            var json = new ObjectMapper().readTree(body(request));
            assertThat(json.path("body").asText())
                    .isEqualTo("<p>nickname &#36;{key} &amp; details &#36;{failure}</p>")
                    .doesNotContain("${");
            return response(202, "{\"requestId\":\"opaque-request\",\"requestCount\":1}");
        });
        sender.submit("from", "to", "Fixed subject", "<p>nickname ${key} &amp; details ${failure}</p>");
    }
    @Test void plainTextTitleInterpolationFailsBeforeSubmission() {
        AtomicInteger calls = new AtomicInteger();
        NcpMailTransport sender = transport(request -> { calls.incrementAndGet(); throw new AssertionError(); });
        assertThatThrownBy(() -> sender.submit("from", "to", "Subject ${key}", "<p>body</p>"))
                .isInstanceOf(IOException.class).hasMessage("NCP_MAIL_TITLE_INVALID").hasNoCause();
        assertThat(calls).hasValue(0);
    }
    @ParameterizedTest @ValueSource(strings = {"{}", "not-json", "{\"requestId\":\"x\",\"requestCount\":2}"})
    void malformedAcceptanceIsNotSuccess(String body) {
        assertThatThrownBy(() -> transport(request -> response(202, body)).submit("from", "to", "subject", "token"))
                .hasMessage("NCP_MAIL_SUBMISSION_UNCONFIRMED").hasNoCause();
    }
    @Test void providerConfigurationFailsClosedAndSmtpNeedsNoNcpSecret() {
        assertThat(new NcpMailTransport("smtp", "", "", "", CLOCK, request -> { throw new AssertionError(); }).isEnabled()).isFalse();
        assertThatThrownBy(() -> new NcpMailTransport("typo", "", "", "", CLOCK, null)).hasMessage("MAIL_TRANSPORT_INVALID");
        assertThatThrownBy(() -> new NcpMailTransport("ncp", "../../other", "key", "secret", CLOCK, null)).hasMessage("NCP_MAIL_CONFIG_REQUIRED");
        assertThatThrownBy(() -> new NcpMailTransport("ncp", "ncp:mail:kr:test:main", "key\r\nsecret", "secret", CLOCK, null)).hasMessage("NCP_MAIL_CONFIG_REQUIRED");
    }
    @Test void oversizedResponseCancelsBeforeBuffering() {
        NcpMailTransport.LimitedBody body = new NcpMailTransport.LimitedBody();
        Flow.Subscription subscription = mock(Flow.Subscription.class);
        body.onSubscribe(subscription);
        body.onNext(List.of(ByteBuffer.allocate(65537)));
        verify(subscription).cancel();
        assertThat(body.getBody().toCompletableFuture()).isCompletedExceptionally();
    }
    private static byte[] body(HttpRequest request) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<>() {
            public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer buffer) { byte[] part = new byte[buffer.remaining()]; buffer.get(part); bytes.writeBytes(part); }
            public void onError(Throwable error) { throw new AssertionError(error); }
            public void onComplete() { }
        });
        return bytes.toByteArray();
    }
}
