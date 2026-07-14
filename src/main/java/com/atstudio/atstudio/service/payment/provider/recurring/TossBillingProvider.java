package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossBillingProvider implements RecurringPaymentProvider, PaymentStatusLookupProvider, PaymentRefundProvider {

    private static final String CHECKOUT_TYPE = "TOSS_BILLING_AUTH";
    private static final String AUTH_METHOD = "CARD";

    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.TOSS_BILLING;
    }

    @Override
    public boolean isLookupConfigured() {
        return !isBlank(paymentProperties.getToss().getSecretKey());
    }

    @Override
    public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        PaymentProperties.Billing billing = paymentProperties.getBilling();
        if (isBlank(toss.getClientKey()) || isBlank(toss.getSecretKey())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }
        if (isBlank(command.providerCustomerKey())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("clientKey", toss.getClientKey());
        metadata.put("customerKey", command.providerCustomerKey());
        metadata.put("successUrl", billing.getAuthSuccessUrl());
        metadata.put("failUrl", billing.getAuthFailUrl());
        metadata.put("method", AUTH_METHOD);

        return new BillingAgreementPrepareResult(
                PaymentProviderType.TOSS_BILLING,
                CHECKOUT_TYPE,
                "tossBillingClientKeyConfigured=true",
                metadata
        );
    }

    @Override
    public BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return BillingAgreementConfirmResult.failure(
                    "TOSS_SECRET_KEY_MISSING",
                    "Toss secret key is not configured.");
        }
        if (isBlank(command.authKey()) || isBlank(command.providerCustomerKey())) {
            return BillingAgreementConfirmResult.failure(
                    "TOSS_BILLING_AUTH_INVALID",
                    "Toss billing authKey and customerKey are required.");
        }

        try {
            HttpResponse<String> response = httpClient().send(
                    issueRequest(command),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toAgreementConfirmResult(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw outcomeUnknown("Toss billing key issue was interrupted.", e);
        } catch (IOException e) {
            throw outcomeUnknown("Toss billing key issue result is unknown.", e);
        } catch (IllegalArgumentException e) {
            return BillingAgreementConfirmResult.failure("TOSS_BILLING_ISSUE_ERROR", e.getMessage());
        }
    }

    @Override
    public BillingChargeResult charge(BillingChargeCommand command) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return BillingChargeResult.failure("TOSS_SECRET_KEY_MISSING", "Toss secret key is not configured.");
        }
        if (isBlank(command.billingKey()) || isBlank(command.providerCustomerKey())) {
            return BillingChargeResult.failure(
                    "TOSS_BILLING_CHARGE_INVALID",
                    "Toss billingKey and customerKey are required.");
        }

        try {
            HttpResponse<String> response = httpClient().send(
                    chargeRequest(command),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toChargeResult(command, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw outcomeUnknown("Toss recurring charge was interrupted.", e);
        } catch (IOException e) {
            throw outcomeUnknown("Toss recurring charge result is unknown.", e);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return BillingChargeResult.failure("TOSS_BILLING_CHARGE_ERROR", e.getMessage());
        }
    }

    @Override
    public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return BillingAgreementCancelResult.failure(
                    "TOSS_SECRET_KEY_MISSING",
                    "Toss secret key is not configured.");
        }
        if (isBlank(command.billingKey())) {
            return BillingAgreementCancelResult.failure(
                    "TOSS_BILLING_KEY_MISSING",
                    "Toss billingKey is required.");
        }

        try {
            HttpResponse<String> response = httpClient().send(
                    deleteRequest(command.billingKey()),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toCancelResult(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw outcomeUnknown("Toss billing key deletion was interrupted.", e);
        } catch (IOException e) {
            throw outcomeUnknown("Toss billing key deletion result is unknown.", e);
        } catch (IllegalArgumentException e) {
            return BillingAgreementCancelResult.failure("TOSS_BILLING_DELETE_ERROR", e.getMessage());
        }
    }

    @Override
    public PaymentRefundProviderResult cancelPayment(PaymentRefundProviderCommand command) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return PaymentRefundProviderResult.failure(
                    "TOSS_SECRET_KEY_MISSING",
                    "Toss secret key is not configured.",
                    null);
        }
        if (isBlank(command.providerPaymentKey()) || isBlank(command.idempotencyKey())) {
            return PaymentRefundProviderResult.failure(
                    "TOSS_PAYMENT_CANCEL_INVALID_ARGUMENT",
                    "providerPaymentKey and idempotencyKey are required.",
                    null);
        }

        try {
            HttpResponse<String> response = httpClient().send(
                    paymentCancelRequest(command),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toRefundResult(command, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentRefundProviderResult.pending(
                    "TOSS_PAYMENT_CANCEL_INTERRUPTED",
                    e.getMessage(),
                    null);
        } catch (IOException | IllegalArgumentException | ArithmeticException e) {
            log.warn("Toss payment cancel result is unknown. paymentKey={}", command.providerPaymentKey(), e);
            return PaymentRefundProviderResult.pending(
                    "TOSS_PAYMENT_CANCEL_UNKNOWN",
                    "Toss payment cancel request result is unknown.",
                    null);
        }
    }

    @Override
    public ProviderPaymentLookupResult findPaymentByOrderId(String orderId) {
        PaymentProperties.Toss toss = paymentProperties.getToss();
        if (isBlank(toss.getSecretKey())) {
            return ProviderPaymentLookupResult.failure(
                    getProviderType(),
                    orderId,
                    "TOSS_SECRET_KEY_MISSING",
                    "Toss secret key is not configured.");
        }
        if (isBlank(orderId)) {
            return ProviderPaymentLookupResult.failure(
                    getProviderType(),
                    orderId,
                    "TOSS_ORDER_ID_MISSING",
                    "orderId is required.");
        }

        try {
            HttpResponse<String> response = httpClient().send(
                    lookupByOrderIdRequest(orderId),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toPaymentLookupResult(orderId, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProviderPaymentLookupResult.failure(
                    getProviderType(),
                    orderId,
                    "TOSS_PAYMENT_LOOKUP_INTERRUPTED",
                    e.getMessage());
        } catch (IOException | IllegalArgumentException e) {
            return ProviderPaymentLookupResult.failure(
                    getProviderType(),
                    orderId,
                    "TOSS_PAYMENT_LOOKUP_ERROR",
                    e.getMessage());
        }
    }

    protected HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(paymentProperties.getBilling().getConnectTimeoutMillis()))
                .build();
    }

    private HttpRequest issueRequest(BillingAgreementConfirmCommand command) throws IOException {
        Map<String, Object> body = Map.of(
                "authKey", command.authKey(),
                "customerKey", command.providerCustomerKey()
        );

        return requestBuilder(paymentProperties.getBilling().getIssueUrl())
                .timeout(readTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body),
                        StandardCharsets.UTF_8))
                .build();
    }

    private HttpRequest chargeRequest(BillingChargeCommand command) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", toTossAmount(command.amount()));
        body.put("customerKey", command.providerCustomerKey());
        body.put("orderId", command.orderId());
        body.put("orderName", command.orderName());
        putIfPresent(body, "customerEmail", command.customerEmail());
        putIfPresent(body, "customerName", command.customerName());

        HttpRequest.Builder builder = requestBuilder(chargeUrl(command.billingKey()))
                .timeout(readTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body),
                        StandardCharsets.UTF_8));
        if (!isBlank(command.idempotencyKey())) {
            builder.header("Idempotency-Key", command.idempotencyKey());
        }
        return builder.build();
    }

    private HttpRequest deleteRequest(String billingKey) {
        return requestBuilder(deleteUrl(billingKey))
                .timeout(readTimeout())
                .DELETE()
                .build();
    }

    private HttpRequest lookupByOrderIdRequest(String orderId) {
        return requestBuilder(lookupByOrderIdUrl(orderId))
                .timeout(readTimeout())
                .GET()
                .build();
    }

    private HttpRequest paymentCancelRequest(PaymentRefundProviderCommand command) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cancelReason", isBlank(command.reason()) ? "ATStudio admin refund" : command.reason());
        if (command.amount() != null) {
            body.put("cancelAmount", toTossAmount(command.amount()));
        }

        return requestBuilder(paymentCancelUrl(command.providerPaymentKey()))
                .timeout(readTimeout())
                .header("Idempotency-Key", command.idempotencyKey())
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body),
                        StandardCharsets.UTF_8))
                .build();
    }

    private HttpRequest.Builder requestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + authorization())
                .header("Content-Type", "application/json");
    }

    private BillingAgreementConfirmResult toAgreementConfirmResult(HttpResponse<String> response) throws IOException {
        JsonNode root = readJson(response.body());
        if (isServerError(response)) {
            throw outcomeUnknown("Toss billing key issue returned a server error.");
        }
        if (isFailure(response)) {
            String code = text(root, "code", "TOSS_BILLING_ISSUE_FAILED");
            String message = text(root, "message", "Toss billing key issue failed.");
            log.warn(
                    "Toss billing key issue failed. status={}, code={}, message={}",
                    response.statusCode(),
                    code,
                    message);
            return BillingAgreementConfirmResult.failure(
                    code,
                    message);
        }

        String billingKey = text(root, "billingKey", "");
        if (isBlank(billingKey)) {
            throw outcomeUnknown("Toss billing key issue succeeded without a billingKey.");
        }

        return BillingAgreementConfirmResult.success(
                billingKey,
                text(root, "method", null),
                maskedMethod(root),
                sanitizedAgreementPayload(root));
    }

    private BillingChargeResult toChargeResult(
            BillingChargeCommand command,
            HttpResponse<String> response) throws IOException {
        JsonNode root = readJson(response.body());
        if (isServerError(response)) {
            throw outcomeUnknown("Toss recurring charge returned a server error.");
        }
        if (isFailure(response)) {
            String code = text(root, "code", "TOSS_BILLING_CHARGE_FAILED");
            String message = text(root, "message", "Toss recurring charge failed.");
            log.warn(
                    "Toss recurring charge failed. status={}, orderId={}, code={}, message={}",
                    response.statusCode(),
                    command.orderId(),
                    code,
                    message);
            return BillingChargeResult.failure(
                    code,
                    message);
        }

        String returnedOrderId = text(root, "orderId", "");
        long totalAmount = root.path("totalAmount").asLong(-1);
        if (!command.orderId().equals(returnedOrderId) || toTossAmount(command.amount()) != totalAmount) {
            throw outcomeUnknown("Toss recurring charge succeeded with mismatched payment evidence.");
        }

        return BillingChargeResult.success(
                text(root, "paymentKey", ""),
                text(root, "method", null),
                maskedMethod(root),
                sanitizedChargePayload(root));
    }

    private BillingAgreementCancelResult toCancelResult(HttpResponse<String> response) throws IOException {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return BillingAgreementCancelResult.success("tossBillingKeyDeleted=true");
        }

        if (isServerError(response)) {
            throw outcomeUnknown("Toss billing key deletion returned a server error.");
        }

        JsonNode root = readJson(response.body());
        String code = text(root, "code", "TOSS_BILLING_DELETE_FAILED");
        String message = text(root, "message", "Toss billing key delete failed.");
        log.warn(
                "Toss billing key delete failed. status={}, code={}, message={}",
                response.statusCode(),
                code,
                message);
        return BillingAgreementCancelResult.failure(
                code,
                message);
    }

    private ProviderPaymentLookupResult toPaymentLookupResult(
            String orderId,
            HttpResponse<String> response) throws IOException {
        JsonNode root = readJson(response.body());
        if (isFailure(response)) {
            String code = text(root, "code", "TOSS_PAYMENT_LOOKUP_FAILED");
            String message = text(root, "message", "Toss payment lookup failed.");
            if (response.statusCode() == 404 || "NOT_FOUND_PAYMENT".equals(code)) {
                return ProviderPaymentLookupResult.notFound(getProviderType(), orderId, "NOT_FOUND_PAYMENT", message);
            }
            log.warn(
                    "Toss payment lookup failed. status={}, orderId={}, code={}, message={}",
                    response.statusCode(),
                    orderId,
                    code,
                    message);
            return ProviderPaymentLookupResult.failure(getProviderType(), orderId, code, message);
        }

        String returnedOrderId = text(root, "orderId", orderId);
        BigDecimal totalAmount = root.hasNonNull("totalAmount")
                ? BigDecimal.valueOf(root.get("totalAmount").asLong())
                : null;
        return ProviderPaymentLookupResult.found(
                getProviderType(),
                returnedOrderId,
                text(root, "paymentKey", ""),
                text(root, "status", ""),
                totalAmount,
                sanitizedChargePayload(root));
    }

    private PaymentRefundProviderResult toRefundResult(
            PaymentRefundProviderCommand command,
            HttpResponse<String> response) throws IOException {
        JsonNode root = readJson(response.body());
        String providerPayload = sanitizedRefundPayload(root);
        if (isServerError(response)) {
            return PaymentRefundProviderResult.pending(
                    "TOSS_PAYMENT_CANCEL_UNKNOWN",
                    "Toss payment cancel request result is unknown.",
                    providerPayload);
        }
        if (isFailure(response)) {
            String code = text(root, "code", "TOSS_PAYMENT_CANCEL_FAILED");
            String message = text(root, "message", "Toss payment cancel failed.");
            log.warn(
                    "Toss payment cancel failed. status={}, orderId={}, code={}, message={}",
                    response.statusCode(),
                    command.orderId(),
                    code,
                    message);
            return PaymentRefundProviderResult.failure(code, message, providerPayload);
        }

        String returnedOrderId = text(root, "orderId", "");
        String refundTransactionKey = latestCancelTransactionKey(root);
        if (!command.orderId().equals(returnedOrderId) || isBlank(refundTransactionKey)) {
            return PaymentRefundProviderResult.pending(
                    "TOSS_PAYMENT_CANCEL_EVIDENCE_MISSING",
                    "Toss payment cancel response did not include matching refund evidence.",
                    providerPayload);
        }

        return PaymentRefundProviderResult.success(
                refundTransactionKey,
                providerPayload);
    }

    private String sanitizedAgreementPayload(JsonNode root) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        putTextIfPresent(payload, "method", root);
        putTextIfPresent(payload, "authenticatedAt", root);
        putMaskedPaymentMethod(payload, root);
        return objectMapper.writeValueAsString(payload);
    }

    private String sanitizedChargePayload(JsonNode root) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        putTextIfPresent(payload, "paymentKey", root);
        putTextIfPresent(payload, "orderId", root);
        putTextIfPresent(payload, "status", root);
        putTextIfPresent(payload, "method", root);
        putTextIfPresent(payload, "approvedAt", root);
        if (root.hasNonNull("totalAmount")) {
            payload.put("totalAmount", root.get("totalAmount").asLong());
        }
        putReceiptEvidence(payload, root);
        putMaskedPaymentMethod(payload, root);
        return objectMapper.writeValueAsString(payload);
    }

    private String sanitizedRefundPayload(JsonNode root) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        putTextIfPresent(payload, "paymentKey", root);
        putTextIfPresent(payload, "orderId", root);
        putTextIfPresent(payload, "lastTransactionKey", root);
        putTextIfPresent(payload, "status", root);
        if (root.hasNonNull("totalAmount")) {
            payload.put("totalAmount", root.get("totalAmount").asLong());
        }
        if (root.hasNonNull("balanceAmount")) {
            payload.put("balanceAmount", root.get("balanceAmount").asLong());
        }

        JsonNode cancels = root.path("cancels");
        if (cancels.isArray()) {
            List<Map<String, Object>> sanitizedCancels = new ArrayList<>();
            for (JsonNode cancel : cancels) {
                Map<String, Object> sanitizedCancel = new LinkedHashMap<>();
                if (cancel.hasNonNull("cancelAmount")) {
                    sanitizedCancel.put("cancelAmount", cancel.get("cancelAmount").asLong());
                }
                putTextIfPresent(sanitizedCancel, "cancelReason", cancel);
                putTextIfPresent(sanitizedCancel, "canceledAt", cancel);
                putTextIfPresent(sanitizedCancel, "transactionKey", cancel);
                putTextIfPresent(sanitizedCancel, "cancelStatus", cancel);
                if (!sanitizedCancel.isEmpty()) {
                    sanitizedCancels.add(sanitizedCancel);
                }
            }
            if (!sanitizedCancels.isEmpty()) {
                payload.put("cancels", sanitizedCancels);
            }
        }
        return objectMapper.writeValueAsString(payload);
    }

    private String latestCancelTransactionKey(JsonNode root) {
        String lastTransactionKey = text(root, "lastTransactionKey", "");
        return isBlank(lastTransactionKey) ? null : lastTransactionKey;
    }

    private boolean isServerError(HttpResponse<String> response) {
        return response.statusCode() >= 500;
    }

    private PaymentProviderOutcomeUnknownException outcomeUnknown(String message) {
        return new PaymentProviderOutcomeUnknownException(message);
    }

    private PaymentProviderOutcomeUnknownException outcomeUnknown(String message, Throwable cause) {
        return new PaymentProviderOutcomeUnknownException(message, cause);
    }

    private void putReceiptEvidence(Map<String, Object> payload, JsonNode root) {
        JsonNode receipt = root.path("receipt");
        if (receipt.isObject()) {
            Map<String, String> sanitizedReceipt = new LinkedHashMap<>();
            putTextIfPresentString(sanitizedReceipt, "url", receipt);
            if (!sanitizedReceipt.isEmpty()) {
                payload.put("receipt", sanitizedReceipt);
            }
        }

        JsonNode cashReceipt = root.path("cashReceipt");
        if (cashReceipt.isObject()) {
            Map<String, String> sanitizedCashReceipt = new LinkedHashMap<>();
            putTextIfPresentString(sanitizedCashReceipt, "receiptKey", cashReceipt);
            putTextIfPresentString(sanitizedCashReceipt, "receiptUrl", cashReceipt);
            putTextIfPresentString(sanitizedCashReceipt, "type", cashReceipt);
            putTextIfPresentString(sanitizedCashReceipt, "issueStatus", cashReceipt);
            putTextIfPresentString(sanitizedCashReceipt, "requestedAt", cashReceipt);
            if (!sanitizedCashReceipt.isEmpty()) {
                payload.put("cashReceipt", sanitizedCashReceipt);
            }
        }
    }

    private void putMaskedPaymentMethod(Map<String, Object> payload, JsonNode root) {
        JsonNode card = root.path("card");
        if (card.isObject()) {
            Map<String, String> sanitizedCard = new LinkedHashMap<>();
            putTextIfPresentString(sanitizedCard, "issuerCode", card);
            putTextIfPresentString(sanitizedCard, "acquirerCode", card);
            putStringIfPresent(sanitizedCard, "number", maskedSensitiveNumber(text(card, "number", "")));
            putTextIfPresentString(sanitizedCard, "cardType", card);
            putTextIfPresentString(sanitizedCard, "ownerType", card);
            if (!sanitizedCard.isEmpty()) {
                payload.put("card", sanitizedCard);
            }
        }

        JsonNode transfers = root.path("transfers");
        if (transfers.isArray() && !transfers.isEmpty()) {
            JsonNode first = transfers.get(0);
            Map<String, String> sanitizedTransfer = new LinkedHashMap<>();
            putTextIfPresentString(sanitizedTransfer, "bankName", first);
            putStringIfPresent(
                    sanitizedTransfer,
                    "bankAccountNumber",
                    maskedSensitiveNumber(text(first, "bankAccountNumber", "")));
            if (!sanitizedTransfer.isEmpty()) {
                payload.put("transfer", sanitizedTransfer);
            }
        }
    }

    private String maskedMethod(JsonNode root) {
        JsonNode card = root.path("card");
        if (card.hasNonNull("number")) {
            return maskedSensitiveNumber(card.get("number").asText());
        }

        JsonNode transfers = root.path("transfers");
        if (transfers.isArray() && !transfers.isEmpty()) {
            JsonNode first = transfers.get(0);
            String bankName = text(first, "bankName", "");
            String bankAccountNumber = maskedSensitiveNumber(text(first, "bankAccountNumber", ""));
            return (bankName + " " + (bankAccountNumber == null ? "" : bankAccountNumber)).trim();
        }
        return null;
    }

    private String maskedSensitiveNumber(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.contains("*")) {
            return trimmed;
        }
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() < 8) {
            return null;
        }
        return digits.substring(0, 4) + "-****-****-" + digits.substring(digits.length() - 4);
    }

    private JsonNode readJson(String body) throws IOException {
        if (isBlank(body)) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    private void putIfPresent(Map<String, Object> body, String field, String value) {
        if (!isBlank(value)) {
            body.put(field, value);
        }
    }

    private void putTextIfPresentString(Map<String, String> payload, String field, JsonNode root) {
        if (root.hasNonNull(field)) {
            payload.put(field, root.get(field).asText());
        }
    }

    private void putStringIfPresent(Map<String, String> payload, String field, String value) {
        if (!isBlank(value)) {
            payload.put(field, value);
        }
    }

    private void putTextIfPresent(Map<String, Object> payload, String field, JsonNode root) {
        if (root.hasNonNull(field)) {
            payload.put(field, root.get(field).asText());
        }
    }

    private boolean isFailure(HttpResponse<String> response) {
        return response.statusCode() < 200 || response.statusCode() >= 300;
    }

    private String chargeUrl(String billingKey) {
        return paymentProperties.getBilling().getChargeUrl()
                .replace("{billingKey}", encode(billingKey));
    }

    private String deleteUrl(String billingKey) {
        return paymentProperties.getBilling().getDeleteUrl()
                .replace("{billingKey}", encode(billingKey));
    }

    private String lookupByOrderIdUrl(String orderId) {
        return paymentProperties.getBilling().getPaymentLookupByOrderIdUrl()
                .replace("{orderId}", encode(orderId));
    }

    private String paymentCancelUrl(String paymentKey) {
        return paymentProperties.getToss().getCancelUrl()
                .replace("{paymentKey}", encode(paymentKey));
    }

    private String authorization() {
        String secretKey = paymentProperties.getToss().getSecretKey();
        return Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

    private Duration readTimeout() {
        return Duration.ofMillis(paymentProperties.getBilling().getReadTimeoutMillis());
    }

    private long toTossAmount(BigDecimal amount) {
        return amount.longValueExact();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode root, String field, String fallback) {
        return root.hasNonNull(field) ? root.get(field).asText() : fallback;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
