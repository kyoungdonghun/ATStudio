package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.validation.ProviderReceiptUrlPolicy;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.enums.PaymentReceiptStatus;
import com.atstudio.atstudio.entity.enums.PaymentReceiptType;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReceiptRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReceiptEvidenceService {

    private final ApplicationEventPublisher eventPublisher;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentReceiptRepository paymentReceiptRepository;
    private final PaymentOperationAuditLogService auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishSuccessfulChargeEvidence(
            PaymentOrder order,
            SubscriptionPayment subscriptionPayment,
            String providerPayload) {
        if (order == null || subscriptionPayment == null || isBlank(providerPayload)) {
            return;
        }
        eventPublisher.publishEvent(new PaymentReceiptEvidenceRequestedEvent(
                order.getId(),
                subscriptionPayment.getId(),
                providerPayload));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAfterCommit(PaymentReceiptEvidenceRequestedEvent event) {
        try {
            recordCommittedPayment(event.paymentOrderId(), event.subscriptionPaymentId(), event.providerPayload());
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to store payment receipt evidence after payment commit. paymentOrderId={}, subscriptionPaymentId={}",
                    event.paymentOrderId(),
                    event.subscriptionPaymentId(),
                    e);
        }
    }

    void recordCommittedPayment(
            Long paymentOrderId,
            Long subscriptionPaymentId,
            String providerPayload) {
        if (paymentOrderId == null || subscriptionPaymentId == null || isBlank(providerPayload)) {
            return;
        }
        Optional<PaymentOrder> order = paymentOrderRepository.findById(paymentOrderId);
        Optional<SubscriptionPayment> subscriptionPayment =
                subscriptionPaymentRepository.findById(subscriptionPaymentId);
        if (order.isEmpty() || subscriptionPayment.isEmpty()) {
            log.warn(
                    "Payment receipt evidence skipped because committed payment references were not found. paymentOrderId={}, subscriptionPaymentId={}",
                    paymentOrderId,
                    subscriptionPaymentId);
            return;
        }

        JsonNode root = readJsonOrNull(providerPayload, paymentOrderId);
        if (root == null || !root.isObject()) {
            return;
        }

        savePaymentReceiptIfPresent(order.get(), subscriptionPayment.get(), root);
        saveCashReceiptIfPresent(order.get(), subscriptionPayment.get(), root);
    }

    private void savePaymentReceiptIfPresent(
            PaymentOrder order,
            SubscriptionPayment subscriptionPayment,
            JsonNode root) {
        String receiptUrl = ProviderReceiptUrlPolicy.normalizeOrNull(text(root.path("receipt"), "url"));
        if (isBlank(receiptUrl)
                || paymentReceiptRepository.existsByPaymentOrderAndType(order, PaymentReceiptType.PAYMENT_RECEIPT)) {
            return;
        }
        PaymentReceipt receipt = paymentReceiptRepository.save(PaymentReceipt.builder()
                .paymentOrder(order)
                .subscriptionPayment(subscriptionPayment)
                .user(order.getUser())
                .provider(order.getProvider())
                .type(PaymentReceiptType.PAYMENT_RECEIPT)
                .status(PaymentReceiptStatus.ISSUED)
                .providerPaymentKey(paymentKey(root, order))
                .receiptUrl(receiptUrl)
                .issuedAt(parseDateTime(text(root, "approvedAt")))
                .evidencePayload(minimalEvidencePayload(root, PaymentReceiptType.PAYMENT_RECEIPT, receiptUrl, null))
                .build());
        recordReceiptAudit(receipt);
    }

    private void saveCashReceiptIfPresent(
            PaymentOrder order,
            SubscriptionPayment subscriptionPayment,
            JsonNode root) {
        JsonNode cashReceipt = root.path("cashReceipt");
        if (!cashReceipt.isObject()
                || paymentReceiptRepository.existsByPaymentOrderAndType(order, PaymentReceiptType.CASH_RECEIPT)) {
            return;
        }
        String receiptKey = text(cashReceipt, "receiptKey");
        String receiptUrl = ProviderReceiptUrlPolicy.normalizeOrNull(text(cashReceipt, "receiptUrl"));
        if (isBlank(receiptKey) && isBlank(receiptUrl)) {
            return;
        }

        PaymentReceipt receipt = paymentReceiptRepository.save(PaymentReceipt.builder()
                .paymentOrder(order)
                .subscriptionPayment(subscriptionPayment)
                .user(order.getUser())
                .provider(order.getProvider())
                .type(PaymentReceiptType.CASH_RECEIPT)
                .status(PaymentReceiptStatus.ISSUED)
                .providerPaymentKey(paymentKey(root, order))
                .receiptKey(receiptKey)
                .receiptUrl(receiptUrl)
                .issuedAt(parseDateTime(firstPresent(text(cashReceipt, "requestedAt"), text(root, "approvedAt"))))
                .evidencePayload(minimalEvidencePayload(root, PaymentReceiptType.CASH_RECEIPT, receiptUrl, receiptKey))
                .build());
        recordReceiptAudit(receipt);
    }

    private void recordReceiptAudit(PaymentReceipt receipt) {
        try {
            auditLogService.recordReceiptEvidenceCreated(receipt);
        } catch (RuntimeException e) {
            log.warn("Failed to write receipt evidence audit log. receiptId={}", receipt.getId(), e);
        }
    }

    private JsonNode readJsonOrNull(String providerPayload, Long paymentOrderId) {
        try {
            return objectMapper.readTree(providerPayload);
        } catch (JsonProcessingException e) {
            log.warn("Payment receipt evidence skipped because provider payload is not JSON. paymentOrderId={}",
                    paymentOrderId);
            return null;
        }
    }

    private String minimalEvidencePayload(
            JsonNode root,
            PaymentReceiptType type,
            String receiptUrl,
            String receiptKey) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfPresent(payload, "paymentKey", text(root, "paymentKey"));
        putIfPresent(payload, "orderId", text(root, "orderId"));
        putIfPresent(payload, "status", text(root, "status"));
        putIfPresent(payload, "method", text(root, "method"));
        putIfPresent(payload, "approvedAt", text(root, "approvedAt"));
        if (root.hasNonNull("totalAmount")) {
            payload.put("totalAmount", root.get("totalAmount").asLong());
        }
        payload.put("receiptType", type.name());
        putIfPresent(payload, "receiptUrl", receiptUrl);
        putIfPresent(payload, "receiptKey", receiptKey);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String paymentKey(JsonNode root, PaymentOrder order) {
        return firstPresent(text(root, "paymentKey"), order.getPgTransactionId());
    }

    private LocalDateTime parseDateTime(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private void putIfPresent(Map<String, Object> payload, String field, String value) {
        if (!isBlank(value)) {
            payload.put(field, value);
        }
    }

    private String text(JsonNode root, String field) {
        return root != null && root.hasNonNull(field) ? root.get(field).asText() : null;
    }

    private String firstPresent(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
