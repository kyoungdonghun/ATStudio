package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdminProviderIdentifierContractTest {

    private static final String RAW_PROVIDER_TRANSACTION_SENTINEL =
            "RAW-PROVIDER-TRANSACTION-SENTINEL-7E1A9C";

    private static final Set<String> RAW_PROVIDER_FIELDS = Set.of(
            "providerPaymentKey",
            "providerTransactionId",
            "providerSettlementId",
            "providerRefundTransactionId");

    @Test
    void adminPaymentResponsesExposeSupportReferencesInsteadOfRawProviderKeys() {
        List<Class<?>> responseTypes = List.of(
                AdminPaymentReceiptResponse.class,
                AdminPaymentSettlementResponse.class,
                AdminPaymentRefundPreviewResponse.class,
                AdminPaymentRefundResponse.class,
                AdminPaymentReconciliationIncidentResponse.class,
                AdminSubscriptionPaymentResponse.class,
                AdminPaymentOperationAuditLogResponse.class);

        for (Class<?> responseType : responseTypes) {
            assertThat(responseType.isRecord()).isTrue();
            List<String> fields = List.of(responseType.getRecordComponents()).stream()
                    .map(RecordComponent::getName)
                    .toList();
            assertThat(fields).contains("providerReference");
            assertThat(fields).doesNotContainAnyElementsOf(RAW_PROVIDER_FIELDS);
        }
    }

    @Test
    void adminReconciliationProviderLedgerUsesSafeNestedIssueRecord() {
        Class<?> providerIssueType = AdminPaymentReconciliationResponse.ProviderIssue.class;
        List<String> fields = List.of(providerIssueType.getRecordComponents()).stream()
                .map(RecordComponent::getName)
                .toList();
        RecordComponent issuesComponent = List.of(
                        AdminPaymentReconciliationResponse.ProviderLedger.class.getRecordComponents()).stream()
                .filter(component -> component.getName().equals("issues"))
                .findFirst()
                .orElseThrow();

        assertThat(providerIssueType.isRecord()).isTrue();
        assertThat(fields).contains("providerReference");
        assertThat(fields).doesNotContainAnyElementsOf(RAW_PROVIDER_FIELDS);
        assertThat(issuesComponent.getGenericType()).isInstanceOf(ParameterizedType.class);
        assertThat(((ParameterizedType) issuesComponent.getGenericType()).getActualTypeArguments())
                .containsExactly(providerIssueType);
    }

    @Test
    void adminReconciliationJsonContainsOnlyDeterministicProviderReference() throws Exception {
        PaymentReconciliationService.ReconciliationResult local =
                new PaymentReconciliationService.ReconciliationResult(
                        240, 112, 10, 11, 12, true, List.of());
        PaymentReconciliationService.ProviderReconciliationIssue rawIssue =
                new PaymentReconciliationService.ProviderReconciliationIssue(
                        PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                        3001L,
                        12L,
                        10L,
                        "ATS-REN-20260716-ABC123",
                        PaymentProviderType.TOSS,
                        PaymentPurpose.RENEWAL,
                        "IN_PROGRESS",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        BigDecimal.valueOf(9900),
                        "KRW",
                        "KRW",
                        RAW_PROVIDER_TRANSACTION_SENTINEL,
                        null,
                        null);
        PaymentReconciliationService.ProviderReconciliationResult provider =
                new PaymentReconciliationService.ProviderReconciliationResult(
                        180, 2, 3, 4, 5, 6, 7, 8, 9, true, List.of(rawIssue));

        AdminPaymentReconciliationResponse response =
                AdminPaymentReconciliationResponse.from(local, provider);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(json);
        JsonNode providerIssue = root.at("/providerLedger/issues/0");

        assertThat(root.at("/localLedger/checkedOrders").asInt()).isEqualTo(240);
        assertThat(root.at("/localLedger/checkedBillingAgreements").asInt()).isEqualTo(112);
        assertThat(root.at("/localLedger/doneOrdersWithoutPayment").asInt()).isEqualTo(10);
        assertThat(root.at("/localLedger/activeAgreementsWithoutSubscription").asInt()).isEqualTo(11);
        assertThat(root.at("/localLedger/hasMismatch").asBoolean()).isTrue();
        assertThat(root.at("/localLedger/totalIssues").asInt()).isEqualTo(12);
        assertThat(root.at("/localLedger/issueDetailsTruncated").asBoolean()).isTrue();
        assertThat(root.at("/providerLedger/checkedOrders").asInt()).isEqualTo(180);
        assertThat(root.at("/providerLedger/skippedOrders").asInt()).isEqualTo(2);
        assertThat(root.at("/providerLedger/providerNotFound").asInt()).isEqualTo(3);
        assertThat(root.at("/providerLedger/lookupFailures").asInt()).isEqualTo(4);
        assertThat(root.at("/providerLedger/providerDoneWithoutLocalFinalization").asInt()).isEqualTo(5);
        assertThat(root.at("/providerLedger/localDoneButProviderNotDone").asInt()).isEqualTo(6);
        assertThat(root.at("/providerLedger/amountMismatches").asInt()).isEqualTo(7);
        assertThat(root.at("/providerLedger/hasMismatch").asBoolean()).isTrue();
        assertThat(root.at("/providerLedger/totalIssues").asInt()).isEqualTo(9);
        assertThat(root.at("/providerLedger/issueDetailsTruncated").asBoolean()).isTrue();
        assertThat(providerIssue.path("providerReference").asText())
                .isEqualTo(ProviderSupportReference.from(RAW_PROVIDER_TRANSACTION_SENTINEL))
                .startsWith("REF-")
                .doesNotContain(RAW_PROVIDER_TRANSACTION_SENTINEL);
        assertThat(providerIssue.has("providerTransactionId")).isFalse();
        assertThat(json)
                .doesNotContain("providerTransactionId")
                .doesNotContain(RAW_PROVIDER_TRANSACTION_SENTINEL);
    }

    @Test
    void adminReceiptResponseSuppressesUnsafeRetainedUrl() {
        PaymentReceipt receipt = receiptWithUrl("javascript:alert('provider-payment-key')");

        AdminPaymentReceiptResponse response = AdminPaymentReceiptResponse.from(receipt);

        assertThat(response.receiptUrl()).isNull();
        assertThat(response.providerReference()).startsWith("REF-");
        assertThat(response.providerReference()).doesNotContain("provider-payment-key");
    }

    @Test
    void adminReceiptResponseKeepsNormalizedHttpsUrl() {
        PaymentReceipt receipt = receiptWithUrl("https://receipts.example.com/a/../receipt/1");

        AdminPaymentReceiptResponse response = AdminPaymentReceiptResponse.from(receipt);

        assertThat(response.receiptUrl()).isEqualTo("https://receipts.example.com/receipt/1");
    }

    @Test
    void retainedLegacyNotesCannotSerializeProviderIdentifierFragments() {
        String raw = "pay_0123456789_abcdef";
        String rawPaymentKey = "payment-key-RAW-PREFIX-SUFFIX";
        String rawOrderId = "ORDER-RAW-PREFIX-SUFFIX";
        PaymentOperationAuditLog auditLog = PaymentOperationAuditLog.builder()
                .action(PaymentOperationAuditAction.RECONCILIATION_INCIDENT_STATUS_UPDATE)
                .targetType(PaymentOperationAuditTargetType.RECONCILIATION_INCIDENT)
                .note("providerStatus=DONE, transactionId : pay_...cdef; orderId=" + rawOrderId)
                .build();
        PaymentReconciliationIncident incident = PaymentReconciliationIncident.builder()
                .dedupeKey("PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ORDER-1")
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.OPEN)
                .severity(PaymentReconciliationIncidentSeverity.CRITICAL)
                .providerTransactionId(ProviderSupportReference.from(raw))
                .failureMessage("paymentKey:  " + rawPaymentKey)
                .resolutionNote("orderId : ORDER-...SUFFIX")
                .firstDetectedAt(LocalDateTime.of(2026, 7, 16, 0, 0))
                .lastDetectedAt(LocalDateTime.of(2026, 7, 16, 0, 0))
                .build();

        AdminPaymentOperationAuditLogResponse auditResponse =
                AdminPaymentOperationAuditLogResponse.from(auditLog);
        AdminPaymentReconciliationIncidentResponse incidentResponse =
                AdminPaymentReconciliationIncidentResponse.from(incident);

        assertThat(auditResponse.note())
                .doesNotContain("pay_")
                .doesNotContain("cdef")
                .doesNotContain(rawOrderId)
                .doesNotContain("RAW-PREFIX")
                .doesNotContain("SUFFIX");
        assertThat(incidentResponse.failureMessage())
                .doesNotContain(rawPaymentKey)
                .doesNotContain("payment-key")
                .doesNotContain("RAW-PREFIX")
                .doesNotContain("SUFFIX");
        assertThat(incidentResponse.resolutionNote())
                .doesNotContain("ORDER-")
                .doesNotContain("SUFFIX");
        assertThat(incidentResponse.providerReference())
                .isEqualTo(ProviderSupportReference.from(raw));
    }

    private PaymentReceipt receiptWithUrl(String receiptUrl) {
        PaymentReceipt receipt = mock(PaymentReceipt.class);
        User user = mock(User.class);
        PaymentOrder order = mock(PaymentOrder.class);
        SubscriptionPayment payment = mock(SubscriptionPayment.class);
        given(receipt.getUser()).willReturn(user);
        given(receipt.getPaymentOrder()).willReturn(order);
        given(receipt.getSubscriptionPayment()).willReturn(payment);
        given(receipt.getProviderPaymentKey()).willReturn("provider-payment-key");
        given(receipt.getReceiptUrl()).willReturn(receiptUrl);
        given(user.getNickname()).willReturn("buyer");
        given(order.getOrderId()).willReturn("ORDER-1");
        return receipt;
    }
}
