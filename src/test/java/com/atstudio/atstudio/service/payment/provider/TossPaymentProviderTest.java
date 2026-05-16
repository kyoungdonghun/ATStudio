package com.atstudio.atstudio.service.payment.provider;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TossPaymentProvider unit tests")
class TossPaymentProviderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("confirm calls Toss confirm API and returns sanitized success payload")
    void confirm_success() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            String response = """
                    {
                      "paymentKey": "toss_payment_key",
                      "orderId": "ORDER-1",
                      "status": "DONE",
                      "method": "CARD",
                      "approvedAt": "2026-05-17T10:00:00+09:00",
                      "totalAmount": 9900
                    }
                    """;
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        server.start();

        PaymentProperties properties = properties("http://localhost:" + server.getAddress().getPort()
                + "/v1/payments/confirm");
        TossPaymentProvider provider = new TossPaymentProvider(properties);

        PaymentProviderConfirmResult result = provider.confirm(
                buildOrder(),
                new PaymentConfirmRequest("ORDER-1", BigDecimal.valueOf(9900),
                        PaymentProviderType.TOSS, null, "toss_payment_key"));

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("toss_payment_key");
        assertThat(result.providerPayload()).contains("\"method\":\"CARD\"");
        assertThat(result.providerPayload()).doesNotContain("secret");
    }

    @Test
    @DisplayName("confirm returns failure when Toss response mismatches order amount")
    void confirm_amountMismatch() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            String response = """
                    {
                      "paymentKey": "toss_payment_key",
                      "orderId": "ORDER-1",
                      "status": "DONE",
                      "totalAmount": 1000
                    }
                    """;
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        server.start();

        PaymentProperties properties = properties("http://localhost:" + server.getAddress().getPort()
                + "/v1/payments/confirm");
        TossPaymentProvider provider = new TossPaymentProvider(properties);

        PaymentProviderConfirmResult result = provider.confirm(
                buildOrder(),
                new PaymentConfirmRequest("ORDER-1", BigDecimal.valueOf(9900),
                        PaymentProviderType.TOSS, null, "toss_payment_key"));

        assertThat(result.success()).isFalse();
        assertThat(result.failureCode()).isEqualTo("TOSS_CONFIRM_MISMATCH");
    }

    private PaymentProperties properties(String confirmUrl) {
        PaymentProperties properties = new PaymentProperties();
        properties.getToss().setSecretKey("test_sk_sample");
        properties.getToss().setConfirmUrl(confirmUrl);
        return properties;
    }

    private PaymentOrder buildOrder() {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Subscription subscription = Subscription.builder()
                .name("Basic")
                .description("Test plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build();
        ReflectionTestUtils.setField(subscription, "id", 10L);

        return PaymentOrder.builder()
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }
}
