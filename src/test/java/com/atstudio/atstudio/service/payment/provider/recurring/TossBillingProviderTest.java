package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TossBillingProvider unit tests")
class TossBillingProviderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("prepareAgreement returns client-safe billing auth metadata")
    void prepareAgreement() {
        TossBillingProvider provider = new TossBillingProvider(properties("http://localhost:1"));

        BillingAgreementPrepareResult result = provider.prepareAgreement(
                new BillingAgreementPrepareCommand("ats_billing_customer"));

        assertThat(result.provider()).isEqualTo(PaymentProviderType.TOSS_BILLING);
        assertThat(result.checkoutType()).isEqualTo("TOSS_BILLING_AUTH");
        assertThat(result.checkoutMetadata())
                .containsEntry("clientKey", "test_ck_sample")
                .containsEntry("customerKey", "ats_billing_customer")
                .containsEntry("method", "CARD");
        assertThat(result.checkoutMetadata().toString()).doesNotContain("test_sk_sample");
    }

    @Test
    @DisplayName("prepareAgreement fails when Toss keys are missing")
    void prepareAgreementMissingConfig() {
        TossBillingProvider provider = new TossBillingProvider(new PaymentProperties());

        assertThatThrownBy(() -> provider.prepareAgreement(
                new BillingAgreementPrepareCommand("ats_billing_customer")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("confirmAgreement issues billing key and returns sanitized payload")
    void confirmAgreementSuccess() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> {
            captured.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            captured.body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = """
                    {
                      "billingKey": "billing_secret_key",
                      "method": "카드",
                      "authenticatedAt": "2026-05-17T10:00:00+09:00",
                      "card": {
                        "issuerCode": "61",
                        "acquirerCode": "61",
                        "number": "1234-****-****-5678",
                        "cardType": "신용",
                        "ownerType": "개인"
                      }
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingAgreementConfirmResult result = provider.confirmAgreement(
                new BillingAgreementConfirmCommand("auth_key", "ats_billing_customer"));

        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
        assertThat(captured.body.get()).contains("\"authKey\":\"auth_key\"");
        assertThat(captured.body.get()).contains("\"customerKey\":\"ats_billing_customer\"");
        assertThat(result.success()).isTrue();
        assertThat(result.billingKey()).isEqualTo("billing_secret_key");
        assertThat(result.maskedMethod()).isEqualTo("1234-****-****-5678");
        assertThat(result.providerPayload()).contains("1234-****-****-5678");
        assertThat(result.providerPayload()).doesNotContain("billing_secret_key");
    }

    @Test
    @DisplayName("confirmAgreement masks raw card number before returning or storing provider payload")
    void confirmAgreementMasksRawCardNumber() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> {
            String response = """
                    {
                      "billingKey": "billing_secret_key",
                      "method": "카드",
                      "authenticatedAt": "2026-05-17T10:00:00+09:00",
                      "card": {
                        "number": "5388111122221111"
                      }
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingAgreementConfirmResult result = provider.confirmAgreement(
                new BillingAgreementConfirmCommand("auth_key", "ats_billing_customer"));

        assertThat(result.success()).isTrue();
        assertThat(result.maskedMethod()).isEqualTo("5388-****-****-1111");
        assertThat(result.providerPayload()).contains("5388-****-****-1111");
        assertThat(result.providerPayload()).doesNotContain("5388111122221111");
    }

    @Test
    @DisplayName("charge calls Toss billing API with idempotency key and sanitized response")
    void chargeSuccess() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/billing_secret_key", exchange -> {
            captured.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            captured.idempotencyKey.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            captured.body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = """
                    {
                      "paymentKey": "payment_key",
                      "orderId": "ORDER-1",
                      "status": "DONE",
                      "method": "카드",
                      "approvedAt": "2026-05-17T10:00:00+09:00",
                      "totalAmount": 9900,
                      "receipt": {
                        "url": "https://dashboard.tosspayments.com/receipt/payment_key"
                      },
                      "cashReceipt": {
                        "receiptKey": "cash_receipt_key",
                        "receiptUrl": "https://dashboard.tosspayments.com/cash-receipts/cash_receipt_key",
                        "type": "소득공제",
                        "issueStatus": "IN_PROGRESS",
                        "requestedAt": "2026-05-17T10:00:01+09:00"
                      },
                      "card": {
                        "number": "5388111122221111"
                      }
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingChargeResult result = provider.charge(new BillingChargeCommand(
                "billing_secret_key",
                "ats_billing_customer",
                "ORDER-1",
                "ATStudio STANDARD Subscription",
                BigDecimal.valueOf(9900),
                "buyer@test.com",
                "buyer",
                "renewal-1"));

        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
        assertThat(captured.idempotencyKey.get()).isEqualTo("renewal-1");
        assertThat(captured.body.get()).contains("\"amount\":9900");
        assertThat(captured.body.get()).contains("\"customerKey\":\"ats_billing_customer\"");
        assertThat(captured.body.get()).contains("\"orderId\":\"ORDER-1\"");
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("payment_key");
        assertThat(result.maskedMethod()).isEqualTo("5388-****-****-1111");
        assertThat(result.providerPayload()).contains("\"paymentKey\":\"payment_key\"");
        assertThat(result.providerPayload())
                .contains("\"receipt\":{\"url\":\"https://dashboard.tosspayments.com/receipt/payment_key\"")
                .contains("\"cashReceipt\":{\"receiptKey\":\"cash_receipt_key\"");
        assertThat(result.providerPayload()).contains("5388-****-****-1111");
        assertThat(result.providerPayload()).doesNotContain("5388111122221111");
        assertThat(result.providerPayload()).doesNotContain("billing_secret_key");
    }

    @Test
    @DisplayName("charge returns mismatch failure when Toss amount differs")
    void chargeMismatch() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/billing_secret_key", exchange -> {
            String response = """
                    {
                      "paymentKey": "payment_key",
                      "orderId": "ORDER-1",
                      "totalAmount": 1000
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingChargeResult result = provider.charge(new BillingChargeCommand(
                "billing_secret_key",
                "ats_billing_customer",
                "ORDER-1",
                "ATStudio STANDARD Subscription",
                BigDecimal.valueOf(9900),
                null,
                null,
                null));

        assertThat(result.success()).isFalse();
        assertThat(result.failureCode()).isEqualTo("TOSS_BILLING_CHARGE_MISMATCH");
    }

    @Test
    @DisplayName("cancelAgreement deletes billing key")
    void cancelAgreementSuccess() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/billing_secret_key", exchange -> {
            captured.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            captured.method.set(exchange.getRequestMethod());
            send(exchange, 200, "");
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingAgreementCancelResult result = provider.cancelAgreement(
                new BillingAgreementCancelCommand("billing_secret_key"));

        assertThat(result.success()).isTrue();
        assertThat(captured.method.get()).isEqualTo("DELETE");
        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
    }

    @Test
    @DisplayName("findPaymentByOrderId retrieves Toss payment state with sanitized payload")
    void findPaymentByOrderIdSuccess() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/orders/ORDER-1", exchange -> {
            captured.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            captured.method.set(exchange.getRequestMethod());
            String response = """
                    {
                      "paymentKey": "payment_key",
                      "orderId": "ORDER-1",
                      "status": "DONE",
                      "method": "카드",
                      "approvedAt": "2026-05-24T10:00:00+09:00",
                      "totalAmount": 9900,
                      "card": {
                        "number": "1234-****-****-5678"
                      }
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        ProviderPaymentLookupResult result = provider.findPaymentByOrderId("ORDER-1");

        assertThat(captured.method.get()).isEqualTo("GET");
        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
        assertThat(result.found()).isTrue();
        assertThat(result.providerDone()).isTrue();
        assertThat(result.transactionId()).isEqualTo("payment_key");
        assertThat(result.totalAmount()).isEqualByComparingTo("9900");
        assertThat(result.providerPayload()).contains("\"paymentKey\":\"payment_key\"");
    }

    private PaymentProperties properties(String baseUrl) {
        PaymentProperties properties = new PaymentProperties();
        properties.getToss().setClientKey("test_ck_sample");
        properties.getToss().setSecretKey("test_sk_sample");
        properties.getBilling().setAuthSuccessUrl("http://localhost:5173/subscriptions/billing/success");
        properties.getBilling().setAuthFailUrl("http://localhost:5173/subscriptions/billing/fail");
        properties.getBilling().setIssueUrl(baseUrl + "/v1/billing/authorizations/issue");
        properties.getBilling().setChargeUrl(baseUrl + "/v1/billing/{billingKey}");
        properties.getBilling().setDeleteUrl(baseUrl + "/v1/billing/{billingKey}");
        properties.getBilling().setPaymentLookupByOrderIdUrl(baseUrl + "/v1/payments/orders/{orderId}");
        return properties;
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder()
                .encodeToString("test_sk_sample:".getBytes(StandardCharsets.UTF_8));
    }

    private void send(com.sun.net.httpserver.HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static class CapturedRequest {
        private final AtomicReference<String> authorization = new AtomicReference<>();
        private final AtomicReference<String> idempotencyKey = new AtomicReference<>();
        private final AtomicReference<String> method = new AtomicReference<>();
        private final AtomicReference<String> body = new AtomicReference<>();
    }
}
