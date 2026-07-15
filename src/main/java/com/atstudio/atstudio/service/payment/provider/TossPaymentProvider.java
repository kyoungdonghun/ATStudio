package com.atstudio.atstudio.service.payment.provider;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentProvider implements PaymentProvider {

    private static final String CHECKOUT_TYPE = "TOSS_WIDGET";

    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.TOSS;
    }

    @Override
    public PaymentProviderPrepareResult prepare(PaymentOrder order) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getClientKey()) || isBlank(toss.getSecretKey())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("clientKey", toss.getClientKey());
        metadata.put("customerKey", customerKey(order));
        metadata.put("orderName", orderName(order));
        metadata.put("successUrl", toss.getSuccessUrl());
        metadata.put("failUrl", toss.getFailUrl());

        return new PaymentProviderPrepareResult(
                CHECKOUT_TYPE,
                null,
                "tossClientKeyConfigured=true",
                metadata
        );
    }

    @Override
    public PaymentProviderConfirmResult confirm(PaymentOrder order, PaymentConfirmRequest request) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return PaymentProviderConfirmResult.failure(
                    "TOSS_SECRET_KEY_MISSING",
                    "Toss secret key is not configured."
            );
        }
        if (isBlank(request.paymentKey())) {
            return PaymentProviderConfirmResult.failure(
                    "TOSS_PAYMENT_KEY_MISSING",
                    "Toss paymentKey is required."
            );
        }

        try {
            HttpResponse<String> response = httpClient(toss).send(
                    confirmRequest(toss, order, request.paymentKey()),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toConfirmResult(order, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentProviderConfirmResult.failure("TOSS_CONFIRM_INTERRUPTED", e.getMessage());
        } catch (IOException | IllegalArgumentException e) {
            return PaymentProviderConfirmResult.failure("TOSS_CONFIRM_ERROR", e.getMessage());
        }
    }

    protected HttpClient httpClient(PaymentProperties.Toss toss) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(toss.getConnectTimeoutMillis()))
                .build();
    }

    private HttpRequest confirmRequest(PaymentProperties.Toss toss, PaymentOrder order, String paymentKey)
            throws IOException {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", order.getOrderId(),
                "amount", toTossAmount(order.getAmount())
        );
        String authorization = Base64.getEncoder()
                .encodeToString((toss.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder()
                .uri(URI.create(toss.getConfirmUrl()))
                .timeout(Duration.ofMillis(toss.getReadTimeoutMillis()))
                .header("Authorization", "Basic " + authorization)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();
    }

    private PaymentProviderConfirmResult toConfirmResult(
            PaymentOrder order,
            HttpResponse<String> response) throws IOException {
        JsonNode root = objectMapper.readTree(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return PaymentProviderConfirmResult.failure(
                    text(root, "code", "TOSS_CONFIRM_FAILED"),
                    text(root, "message", "Toss payment confirm failed.")
            );
        }

        String returnedOrderId = text(root, "orderId", "");
        long totalAmount = root.path("totalAmount").asLong(-1);
        if (!order.getOrderId().equals(returnedOrderId) || toTossAmount(order.getAmount()) != totalAmount) {
            return PaymentProviderConfirmResult.failure(
                    "TOSS_CONFIRM_MISMATCH",
                    "Toss confirm response does not match the payment order."
            );
        }

        String paymentKey = text(root, "paymentKey", "");
        return PaymentProviderConfirmResult.success(paymentKey, sanitizedPayload(root));
    }

    private String sanitizedPayload(JsonNode root) throws IOException {
        Map<String, String> payload = new LinkedHashMap<>();
        putIfPresent(payload, "paymentKey", root);
        putIfPresent(payload, "orderId", root);
        putIfPresent(payload, "status", root);
        putIfPresent(payload, "method", root);
        putIfPresent(payload, "approvedAt", root);
        return objectMapper.writeValueAsString(payload);
    }

    private void putIfPresent(Map<String, String> payload, String field, JsonNode root) {
        if (root.hasNonNull(field)) {
            payload.put(field, root.get(field).asText());
        }
    }

    private long toTossAmount(BigDecimal amount) {
        return amount.longValueExact();
    }

    private String customerKey(PaymentOrder order) {
        return "ats_user_" + order.getUser().getId();
    }

    private String orderName(PaymentOrder order) {
        return "AT.M " + order.getSubscription().getName() + " Subscription";
    }

    private String text(JsonNode root, String field, String fallback) {
        return root.hasNonNull(field) ? root.get(field).asText() : fallback;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
