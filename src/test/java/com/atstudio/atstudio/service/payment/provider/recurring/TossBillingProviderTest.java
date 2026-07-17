package com.atstudio.atstudio.service.payment.provider.recurring;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

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

        assertThat(result.provider()).isEqualTo(PaymentProviderType.TOSS);
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
    @DisplayName("confirmAgreement treats a Toss server error as an unknown provider outcome")
    void confirmAgreementServerErrorIsUnknown() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> send(
                exchange,
                500,
                "{\"code\":\"FAILED_INTERNAL_SYSTEM_PROCESSING\",\"message\":\"temporary failure\"}"));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        assertThatThrownBy(() -> provider.confirmAgreement(
                new BillingAgreementConfirmCommand("auth_key", "ats_billing_customer")))
                .isInstanceOf(PaymentProviderOutcomeUnknownException.class);
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
                "AT.M STANDARD Subscription",
                BigDecimal.valueOf(9900),
                "buyer@test.com",
                "buyer",
                "renewal-1"));

        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
        assertThat(captured.idempotencyKey.get()).isEqualTo("renewal-1");
        assertThat(captured.body.get()).contains("\"amount\":9900");
        assertThat(captured.body.get()).contains("\"customerKey\":\"ats_billing_customer\"");
        assertThat(captured.body.get()).contains("\"orderId\":\"ORDER-1\"");
        assertThat(captured.body.get()).contains("\"orderName\":\"AT.M STANDARD Subscription\"");
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
    @DisplayName("charge treats mismatched successful evidence as an unknown provider outcome")
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

        assertThatThrownBy(() -> provider.charge(new BillingChargeCommand(
                "billing_secret_key",
                "ats_billing_customer",
                "ORDER-1",
                "AT.M STANDARD Subscription",
                BigDecimal.valueOf(9900),
                null,
                null,
                null)))
                .isInstanceOf(PaymentProviderOutcomeUnknownException.class);
    }

    @Test
    @DisplayName("charge treats a Toss server error as an unknown provider outcome")
    void chargeServerErrorIsUnknown() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/billing_secret_key", exchange -> send(
                exchange,
                500,
                "{\"code\":\"FAILED_INTERNAL_SYSTEM_PROCESSING\",\"message\":\"temporary failure\"}"));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        assertThatThrownBy(() -> provider.charge(new BillingChargeCommand(
                "billing_secret_key",
                "ats_billing_customer",
                "ORDER-1",
                "AT.M STANDARD Subscription",
                BigDecimal.valueOf(9900),
                null,
                null,
                "renewal-1")))
                .isInstanceOf(PaymentProviderOutcomeUnknownException.class);
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
    @DisplayName("cancelAgreement treats a Toss server error as an unknown provider outcome")
    void cancelAgreementServerErrorIsUnknown() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/billing_secret_key", exchange -> send(
                exchange,
                503,
                "{\"code\":\"PROVIDER_UNAVAILABLE\",\"message\":\"temporary failure\"}"));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        assertThatThrownBy(() -> provider.cancelAgreement(
                new BillingAgreementCancelCommand("billing_secret_key")))
                .isInstanceOf(PaymentProviderOutcomeUnknownException.class);
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
                      "currency": "KRW",
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
        assertThat(result.currency()).isEqualTo("KRW");
        assertThat(result.providerPayload()).doesNotContain("paymentKey", "payment_key");
        assertThat(result.providerPayload()).contains("\"currency\":\"KRW\"");
    }

    @Test
    @DisplayName("cancelPayment calls Toss payment cancel API with idempotency key and sanitized response")
    void cancelPaymentSuccess() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/payment_key/cancel", exchange -> {
            captured.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            captured.idempotencyKey.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            captured.method.set(exchange.getRequestMethod());
            captured.body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = """
                    {
                      "paymentKey": "payment_key",
                      "orderId": "ORDER-1",
                      "lastTransactionKey": "cancel_tx_key",
                      "status": "CANCELED",
                      "totalAmount": 9900,
                      "balanceAmount": 0,
                      "card": {
                        "number": "5388111122221111"
                      },
                      "cancels": [
                        {
                          "cancelAmount": 9900,
                          "cancelReason": "CUSTOMER_REQUEST",
                          "canceledAt": "2026-05-25T10:00:00+09:00",
                          "transactionKey": "cancel_tx_key",
                          "cancelStatus": "DONE"
                        }
                      ]
                    }
                    """;
            send(exchange, 200, response);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        PaymentRefundProviderResult result = provider.cancelPayment(new PaymentRefundProviderCommand(
                "payment_key",
                "ORDER-1",
                BigDecimal.valueOf(9900),
                "CUSTOMER_REQUEST",
                "ATS-REFUND-1"));

        assertThat(captured.method.get()).isEqualTo("POST");
        assertThat(captured.authorization.get()).isEqualTo(basicAuth());
        assertThat(captured.idempotencyKey.get()).isEqualTo("ATS-REFUND-1");
        assertThat(captured.body.get()).contains("\"cancelReason\":\"CUSTOMER_REQUEST\"");
        assertThat(captured.body.get()).contains("\"cancelAmount\":9900");
        assertThat(result.success()).isTrue();
        assertThat(result.providerRefundTransactionId()).isEqualTo("cancel_tx_key");
        assertThat(result.providerPayload()).contains("\"paymentKey\":\"payment_key\"");
        assertThat(result.providerPayload()).contains("\"transactionKey\":\"cancel_tx_key\"");
        assertThat(result.providerPayload()).doesNotContain("5388111122221111");
    }

    @Test
    @DisplayName("cancelPayment uses the AT.M default reason when the requested reason is blank")
    void cancelPaymentUsesDefaultReasonWhenReasonIsBlank() throws IOException {
        CapturedRequest captured = new CapturedRequest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/payment_key/cancel", exchange -> {
            captured.body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            send(exchange, 200, """
                    {
                      "paymentKey": "payment_key",
                      "orderId": "ORDER-1",
                      "lastTransactionKey": "cancel_tx_key",
                      "status": "CANCELED",
                      "cancels": [
                        {
                          "transactionKey": "cancel_tx_key"
                        }
                      ]
                    }
                    """);
        });
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        PaymentRefundProviderResult result = provider.cancelPayment(new PaymentRefundProviderCommand(
                "payment_key",
                "ORDER-1",
                BigDecimal.valueOf(9900),
                " ",
                "ATS-REFUND-DEFAULT-1"));

        assertThat(captured.body.get()).contains("\"cancelReason\":\"AT.M admin refund\"");
        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("cancelPayment uses the latest refund transaction key for repeated partial cancellations")
    void cancelPaymentUsesLatestTransactionKey() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/payment_key/cancel", exchange -> send(exchange, 200, """
                {
                  "paymentKey": "payment_key",
                  "orderId": "ORDER-1",
                  "lastTransactionKey": "cancel_tx_new",
                  "status": "PARTIAL_CANCELED",
                  "cancels": [
                    {"transactionKey": "cancel_tx_old", "cancelAmount": 1000},
                    {"transactionKey": "cancel_tx_new", "cancelAmount": 2000}
                  ]
                }
                """));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        PaymentRefundProviderResult result = provider.cancelPayment(new PaymentRefundProviderCommand(
                "payment_key",
                "ORDER-1",
                BigDecimal.valueOf(2000),
                "CUSTOMER_REQUEST",
                "ATS-REFUND-2"));

        assertThat(result.success()).isTrue();
        assertThat(result.providerRefundTransactionId()).isEqualTo("cancel_tx_new");
    }

    @Test
    @DisplayName("cancelPayment keeps server errors pending instead of finalizing a failure")
    void cancelPaymentServerErrorIsPending() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/payment_key/cancel", exchange -> send(
                exchange,
                503,
                "{\"code\":\"PROVIDER_UNAVAILABLE\",\"message\":\"temporary failure\"}"));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        PaymentRefundProviderResult result = provider.cancelPayment(new PaymentRefundProviderCommand(
                "payment_key",
                "ORDER-1",
                BigDecimal.valueOf(9900),
                "CUSTOMER_REQUEST",
                "ATS-REFUND-3"));

        assertThat(result.success()).isFalse();
        assertThat(result.pendingConfirmation()).isTrue();
        assertThat(result.failureCode()).isEqualTo("TOSS_PAYMENT_CANCEL_UNKNOWN");
    }

    @Test
    @DisplayName("cancelPayment does not infer refund identity when lastTransactionKey is missing")
    void cancelPaymentMissingTransactionKeyIsPending() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/payment_key/cancel", exchange -> send(exchange, 200, """
                {
                  "paymentKey": "payment_key",
                  "orderId": "ORDER-1",
                  "status": "CANCELED",
                  "cancels": [{"cancelAmount": 9900, "transactionKey": "historical_cancel_tx"}]
                }
                """));
        server.start();

        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        PaymentRefundProviderResult result = provider.cancelPayment(new PaymentRefundProviderCommand(
                "payment_key",
                "ORDER-1",
                BigDecimal.valueOf(9900),
                "CUSTOMER_REQUEST",
                "ATS-REFUND-4"));

        assertThat(result.success()).isFalse();
        assertThat(result.pendingConfirmation()).isTrue();
        assertThat(result.providerRefundTransactionId()).isNull();
    }

    @Test
    @DisplayName("provider input guards fail before any Toss network request")
    void providerInputGuards() {
        PaymentProperties missing = new PaymentProperties();
        missing.getToss().setClientKey("client-only");
        TossBillingProvider missingProvider = new TossBillingProvider(missing);

        assertThat(missingProvider.getProviderType()).isEqualTo(PaymentProviderType.TOSS);
        assertThat(missingProvider.isLookupConfigured()).isFalse();
        assertThatThrownBy(() -> missingProvider.prepareAgreement(
                new BillingAgreementPrepareCommand("customer")))
                .isInstanceOf(BusinessException.class);
        assertThat(missingProvider.confirmAgreement(
                        new BillingAgreementConfirmCommand("auth", "customer"))
                .failureCode()).isEqualTo("TOSS_SECRET_KEY_MISSING");
        assertThat(missingProvider.charge(chargeCommand("billing", "customer", BigDecimal.valueOf(9900)))
                .failureCode()).isEqualTo("TOSS_SECRET_KEY_MISSING");
        assertThat(missingProvider.cancelAgreement(new BillingAgreementCancelCommand("billing"))
                .failureCode()).isEqualTo("TOSS_SECRET_KEY_MISSING");
        assertThat(missingProvider.cancelPayment(refundCommand("payment", "idempotency"))
                .failureCode()).isEqualTo("TOSS_SECRET_KEY_MISSING");
        assertThat(missingProvider.findPaymentByOrderId("ORDER-1").failureCode())
                .isEqualTo("TOSS_SECRET_KEY_MISSING");

        TossBillingProvider configured = new TossBillingProvider(properties("http://localhost:1"));
        assertThat(configured.isLookupConfigured()).isTrue();
        assertThatThrownBy(() -> configured.prepareAgreement(new BillingAgreementPrepareCommand(" ")))
                .isInstanceOf(BusinessException.class);
        assertThat(configured.confirmAgreement(new BillingAgreementConfirmCommand(" ", "customer"))
                .failureCode()).isEqualTo("TOSS_BILLING_AUTH_INVALID");
        assertThat(configured.confirmAgreement(new BillingAgreementConfirmCommand("auth", " "))
                .failureCode()).isEqualTo("TOSS_BILLING_AUTH_INVALID");
        assertThat(configured.charge(chargeCommand(" ", "customer", BigDecimal.valueOf(9900)))
                .failureCode()).isEqualTo("TOSS_BILLING_CHARGE_INVALID");
        assertThat(configured.charge(chargeCommand("billing", " ", BigDecimal.valueOf(9900)))
                .failureCode()).isEqualTo("TOSS_BILLING_CHARGE_INVALID");
        assertThat(configured.cancelAgreement(new BillingAgreementCancelCommand(" "))
                .failureCode()).isEqualTo("TOSS_BILLING_KEY_MISSING");
        assertThat(configured.cancelPayment(refundCommand(" ", "idempotency"))
                .failureCode()).isEqualTo("TOSS_PAYMENT_CANCEL_INVALID_ARGUMENT");
        assertThat(configured.cancelPayment(refundCommand("payment", " "))
                .failureCode()).isEqualTo("TOSS_PAYMENT_CANCEL_INVALID_ARGUMENT");
        assertThat(configured.findPaymentByOrderId(" ").failureCode()).isEqualTo("TOSS_ORDER_ID_MISSING");
    }

    @Test
    @DisplayName("Toss client errors remain final failures while a missing payment remains not found")
    void providerClientErrorsAreClassified() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> send(
                exchange, 400, "{\"code\":\"INVALID_AUTH\",\"message\":\"bad auth\"}"));
        server.createContext("/v1/billing/billing_secret_key", exchange -> send(
                exchange,
                400,
                "DELETE".equals(exchange.getRequestMethod())
                        ? "{\"code\":\"ALREADY_REMOVED_BILLING_KEY\",\"message\":\"removed\"}"
                        : "{\"code\":\"REJECT_CARD_PAYMENT\",\"message\":\"rejected\"}"));
        server.createContext("/v1/payments/orders/ORDER-404", exchange -> send(
                exchange, 404, "{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"missing\"}"));
        server.createContext("/v1/payments/orders/ORDER-FAIL", exchange -> send(
                exchange, 400, "{\"code\":\"INVALID_REQUEST\",\"message\":\"bad request\"}"));
        server.createContext("/v1/payments/payment_key/cancel", exchange -> send(
                exchange, 400, "{\"code\":\"NOT_CANCELABLE_AMOUNT\",\"message\":\"too much\"}"));
        server.start();
        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        assertThat(provider.confirmAgreement(new BillingAgreementConfirmCommand("auth", "customer")))
                .satisfies(result -> {
                    assertThat(result.success()).isFalse();
                    assertThat(result.failureCode()).isEqualTo("INVALID_AUTH");
                });
        assertThat(provider.charge(chargeCommand("billing_secret_key", "customer", BigDecimal.valueOf(9900))))
                .satisfies(result -> {
                    assertThat(result.success()).isFalse();
                    assertThat(result.failureCode()).isEqualTo("REJECT_CARD_PAYMENT");
                });
        assertThat(provider.cancelAgreement(new BillingAgreementCancelCommand("billing_secret_key")))
                .satisfies(result -> {
                    assertThat(result.success()).isFalse();
                    assertThat(result.failureCode()).isEqualTo("ALREADY_REMOVED_BILLING_KEY");
                });
        assertThat(provider.findPaymentByOrderId("ORDER-404"))
                .satisfies(result -> {
                    assertThat(result.found()).isFalse();
                    assertThat(result.lookupFailure()).isFalse();
                });
        assertThat(provider.findPaymentByOrderId("ORDER-FAIL"))
                .satisfies(result -> {
                    assertThat(result.found()).isFalse();
                    assertThat(result.lookupFailure()).isTrue();
                    assertThat(result.failureCode()).isEqualTo("INVALID_REQUEST");
                });
        assertThat(provider.cancelPayment(refundCommand("payment_key", "ATS-REFUND-CLIENT-1")))
                .satisfies(result -> {
                    assertThat(result.success()).isFalse();
                    assertThat(result.pendingConfirmation()).isFalse();
                    assertThat(result.failureCode()).isEqualTo("NOT_CANCELABLE_AMOUNT");
                });
    }

    @Test
    @DisplayName("lookup and billing authorization sanitize sparse transfer evidence")
    void sparseTransferEvidenceIsSanitized() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> send(exchange, 200, """
                {
                  "billingKey": "billing_secret_key",
                  "method": "TRANSFER",
                  "transfers": [{"bankName": "Test Bank", "bankAccountNumber": "123456789012"}]
                }
                """));
        server.createContext("/v1/payments/orders/ORDER-SPARSE", exchange -> send(exchange, 200, """
                {
                  "orderId": "ORDER-SPARSE",
                  "status": "WAITING_FOR_DEPOSIT",
                  "transfers": [{"bankName": "Test Bank", "bankAccountNumber": "short"}],
                  "receipt": {},
                  "cashReceipt": {}
                }
                """));
        server.start();
        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        BillingAgreementConfirmResult agreement = provider.confirmAgreement(
                new BillingAgreementConfirmCommand("auth", "customer"));
        ProviderPaymentLookupResult lookup = provider.findPaymentByOrderId("ORDER-SPARSE");

        assertThat(agreement.success()).isTrue();
        assertThat(agreement.maskedMethod()).isEqualTo("Test Bank 1234-****-****-9012");
        assertThat(agreement.providerPayload()).doesNotContain("123456789012");
        assertThat(lookup.found()).isTrue();
        assertThat(lookup.providerDone()).isFalse();
        assertThat(lookup.totalAmount()).isNull();
        assertThat(lookup.currency()).isNull();
        assertThat(lookup.providerPayload()).doesNotContain("short");
    }

    @Test
    @DisplayName("successful responses without durable identity remain unknown or pending")
    void successfulResponsesRequireDurableIdentity() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/billing/authorizations/issue", exchange -> send(
                exchange, 200, "{\"method\":\"CARD\"}"));
        server.createContext("/v1/payments/payment_key/cancel", exchange -> send(
                exchange,
                200,
                "{\"orderId\":\"OTHER-ORDER\",\"lastTransactionKey\":\"cancel-key\",\"status\":\"CANCELED\"}"));
        server.start();
        TossBillingProvider provider = new TossBillingProvider(properties(baseUrl()));

        assertThatThrownBy(() -> provider.confirmAgreement(
                new BillingAgreementConfirmCommand("auth", "customer")))
                .isInstanceOf(PaymentProviderOutcomeUnknownException.class);
        assertThat(provider.cancelPayment(refundCommand("payment_key", "ATS-REFUND-EVIDENCE-1")))
                .satisfies(result -> {
                    assertThat(result.success()).isFalse();
                    assertThat(result.pendingConfirmation()).isTrue();
                    assertThat(result.failureCode()).isEqualTo("TOSS_PAYMENT_CANCEL_EVIDENCE_MISSING");
                });
    }

    @Test
    @DisplayName("cancelPayment unknown transport failure logs exception class without URI or provider key")
    void cancelPaymentUnknownFailureLogsBoundedMetadata() {
        String rawProviderPaymentKey = "provider-payment-key-secret";
        PaymentProperties properties = properties("http://localhost:1");
        properties.getToss().setCancelUrl("http://localhost/%zz/{paymentKey}");
        TossBillingProvider provider = new TossBillingProvider(properties);
        Logger logger = (Logger) LoggerFactory.getLogger(TossBillingProvider.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        PaymentRefundProviderResult result;
        try {
            result = provider.cancelPayment(new PaymentRefundProviderCommand(
                    rawProviderPaymentKey,
                    "ORDER-SECRET-1",
                    BigDecimal.valueOf(9900),
                    "CUSTOMER_REQUEST",
                    "ATS-REFUND-SECRET-1"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        assertThat(result.pendingConfirmation()).isTrue();
        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getFormattedMessage())
                    .contains("exceptionClass=IllegalArgumentException")
                    .doesNotContain(rawProviderPaymentKey, "%zz", "http://");
            assertThat(event.getThrowableProxy()).isNull();
        });
    }

    private PaymentProperties properties(String baseUrl) {
        PaymentProperties properties = new PaymentProperties();
        properties.getToss().setClientKey("test_ck_sample");
        properties.getToss().setSecretKey("test_sk_sample");
        properties.getToss().setCancelUrl(baseUrl + "/v1/payments/{paymentKey}/cancel");
        properties.getBilling().setAuthSuccessUrl("http://localhost:5173/subscriptions/billing/success");
        properties.getBilling().setAuthFailUrl("http://localhost:5173/subscriptions/billing/fail");
        properties.getBilling().setIssueUrl(baseUrl + "/v1/billing/authorizations/issue");
        properties.getBilling().setChargeUrl(baseUrl + "/v1/billing/{billingKey}");
        properties.getBilling().setDeleteUrl(baseUrl + "/v1/billing/{billingKey}");
        properties.getBilling().setPaymentLookupByOrderIdUrl(baseUrl + "/v1/payments/orders/{orderId}");
        return properties;
    }

    private BillingChargeCommand chargeCommand(String billingKey, String customerKey, BigDecimal amount) {
        return new BillingChargeCommand(
                billingKey,
                customerKey,
                "ORDER-1",
                "AT.M STANDARD Subscription",
                amount,
                null,
                null,
                "charge-idempotency");
    }

    private PaymentRefundProviderCommand refundCommand(String paymentKey, String idempotencyKey) {
        return new PaymentRefundProviderCommand(
                paymentKey,
                "ORDER-1",
                BigDecimal.valueOf(9900),
                "CUSTOMER_REQUEST",
                idempotencyKey);
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
