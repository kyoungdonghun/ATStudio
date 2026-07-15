package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationIncidentService {

    private static final int MAX_FAILURE_MESSAGE_LENGTH = 500;
    private static final int MAX_FAILURE_CODE_LENGTH = 100;
    private static final int MAX_NOTE_LENGTH = 500;
    private static final String BILLING_KEY_DELETE_FAILED = "BILLING_KEY_DELETE_FAILED";
    private static final String BILLING_KEY_DELETE_FAILURE_MESSAGE = "Provider billing key deletion failed.";
    private static final String BILLING_CLEANUP_RESOLVED_NOTE = "Provider billing key cleanup completed.";
    private static final String PROVIDER_RECOVERY_RESOLVED_NOTE =
            "Provider payment evidence was finalized locally without another charge.";
    private static final List<PaymentReconciliationIssueType> PROVIDER_RECOVERY_ISSUE_TYPES = List.of(
            PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
            PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
            PaymentReconciliationIssueType.AMOUNT_MISMATCH,
            PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_FOUND,
            PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
            PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT
    );

    private final PaymentReconciliationIncidentRepository incidentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final UserRepository userRepository;
    private final PaymentProperties paymentProperties;
    private final EmailService emailService;
    private final ObjectProvider<PaymentOperationAuditLogService> auditLogServiceProvider;

    @Transactional
    public void recordIssues(
            PaymentReconciliationService.ReconciliationResult localResult,
            PaymentReconciliationService.ProviderReconciliationResult providerResult) {
        LocalDateTime detectedAt = LocalDateTime.now();
        localResult.issues().forEach(issue -> recordLocalIssue(issue, detectedAt));
        providerResult.issues().forEach(issue -> recordProviderIssue(issue, detectedAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLocalIssues(PaymentReconciliationService.ReconciliationResult localResult) {
        LocalDateTime detectedAt = LocalDateTime.now();
        localResult.issues().forEach(issue -> recordLocalIssue(issue, detectedAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProviderRecoveryIssue(PaymentReconciliationService.ProviderReconciliationIssue issue) {
        PaymentReconciliationIncident incident = recordProviderIssue(issue, LocalDateTime.now());
        recordIncidentAudit(
                incident,
                null,
                incident.getStatus(),
                recoveryEvidenceNote(issue));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProviderFinalizationFailure(
            PaymentReconciliationService.ProviderReconciliationIssue issue) {
        PaymentReconciliationIncident incident = recordProviderIssue(issue, LocalDateTime.now());
        recordIncidentAudit(
                incident,
                incident.getStatus(),
                incident.getStatus(),
                "Reconciliation finalization failed; exceptionClass=" + nullText(issue.failureMessage()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolveProviderRecoveryIncidents(String orderID) {
        LocalDateTime resolvedAt = LocalDateTime.now();
        for (PaymentReconciliationIssueType issueType : PROVIDER_RECOVERY_ISSUE_TYPES) {
            String dedupeKey = dedupeKey(issueType, orderID, null, null);
            incidentRepository.findByDedupeKey(dedupeKey)
                    .filter(incident -> incident.getStatus() != PaymentReconciliationIncidentStatus.RESOLVED)
                    .ifPresent(incident -> {
                        PaymentReconciliationIncidentStatus beforeStatus = incident.getStatus();
                        incident.changeStatus(
                                PaymentReconciliationIncidentStatus.RESOLVED,
                                PROVIDER_RECOVERY_RESOLVED_NOTE,
                                resolvedAt);
                        recordIncidentAudit(
                                incident,
                                beforeStatus,
                                PaymentReconciliationIncidentStatus.RESOLVED,
                                PROVIDER_RECOVERY_RESOLVED_NOTE);
                    });
        }
    }

    @Transactional
    public PaymentReconciliationIncident changeStatus(
            Long incidentId,
            PaymentReconciliationIncidentStatus status,
            String note) {
        PaymentReconciliationIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new com.atstudio.atstudio.common.exception.BusinessException(
                        com.atstudio.atstudio.common.exception.BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        incident.changeStatus(status, truncate(note, MAX_NOTE_LENGTH), LocalDateTime.now());
        return incident;
    }

    @Transactional
    public void recordBillingCleanupFailure(
            BillingAgreement billingAgreement,
            String failureCode,
            String failureMessage) {
        LocalDateTime detectedAt = LocalDateTime.now();
        upsertIncident(
                dedupeKey(
                        PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                        null,
                        null,
                        billingAgreement.getId()),
                PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                PaymentReconciliationIncidentSeverity.WARNING,
                null,
                billingAgreement,
                billingAgreement.getUser(),
                null,
                billingAgreement.getProvider(),
                null,
                BillingAgreementStatus.CANCELLED.name(),
                BILLING_KEY_DELETE_FAILED,
                null,
                null,
                null,
                normalizeFailureCode(failureCode),
                normalizeFailureMessage(failureMessage),
                detectedAt);
    }

    @Transactional
    public void resolveBillingCleanupIncident(BillingAgreement billingAgreement) {
        String dedupeKey = dedupeKey(
                PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                null,
                null,
                billingAgreement.getId());
        incidentRepository.findByDedupeKey(dedupeKey)
                .filter(incident -> incident.getStatus() != PaymentReconciliationIncidentStatus.RESOLVED)
                .ifPresent(incident -> incident.changeStatus(
                        PaymentReconciliationIncidentStatus.RESOLVED,
                        BILLING_CLEANUP_RESOLVED_NOTE,
                        LocalDateTime.now()));
    }

    private void recordLocalIssue(
            PaymentReconciliationService.LocalReconciliationIssue issue,
            LocalDateTime detectedAt) {
        PaymentOrder paymentOrder = findPaymentOrder(issue.paymentOrderId());
        BillingAgreement billingAgreement = findBillingAgreement(issue.billingAgreementId());
        User user = resolveUser(paymentOrder, billingAgreement, issue.userId());
        upsertIncident(
                dedupeKey(issue.issueType(), issue.orderId(), issue.paymentOrderId(), issue.billingAgreementId()),
                issue.issueType(),
                severity(issue.issueType()),
                paymentOrder,
                billingAgreement,
                user,
                issue.orderId(),
                issue.provider(),
                issue.purpose(),
                issue.localStatus(),
                null,
                issue.localAmount(),
                null,
                null,
                null,
                null,
                detectedAt);
    }

    private PaymentReconciliationIncident recordProviderIssue(
            PaymentReconciliationService.ProviderReconciliationIssue issue,
            LocalDateTime detectedAt) {
        PaymentOrder paymentOrder = findPaymentOrder(issue.paymentOrderId());
        BillingAgreement billingAgreement = findBillingAgreement(issue.billingAgreementId());
        User user = resolveUser(paymentOrder, billingAgreement, issue.userId());
        return upsertIncident(
                dedupeKey(issue.issueType(), issue.orderId(), issue.paymentOrderId(), issue.billingAgreementId()),
                issue.issueType(),
                severity(issue.issueType()),
                paymentOrder,
                billingAgreement,
                user,
                issue.orderId(),
                issue.provider(),
                issue.purpose(),
                issue.localStatus(),
                issue.providerStatus(),
                issue.localAmount(),
                issue.providerAmount(),
                issue.providerTransactionId(),
                issue.failureCode(),
                issue.failureMessage(),
                detectedAt);
    }

    private PaymentReconciliationIncident upsertIncident(
            String dedupeKey,
            PaymentReconciliationIssueType issueType,
            PaymentReconciliationIncidentSeverity severity,
            PaymentOrder paymentOrder,
            BillingAgreement billingAgreement,
            User user,
            String orderId,
            PaymentProviderType provider,
            PaymentPurpose purpose,
            String localStatus,
            String providerStatus,
            BigDecimal localAmount,
            BigDecimal providerAmount,
            String providerTransactionId,
            String failureCode,
            String failureMessage,
            LocalDateTime detectedAt) {
        PaymentReconciliationIncident incident = incidentRepository.findByDedupeKey(dedupeKey)
                .map(existing -> {
                    existing.recordDetection(
                            paymentOrder,
                            billingAgreement,
                            user,
                            orderId,
                            provider,
                            purpose,
                            localStatus,
                            providerStatus,
                            localAmount,
                            providerAmount,
                            providerTransactionId,
                            truncate(failureCode, MAX_FAILURE_CODE_LENGTH),
                            truncate(failureMessage, MAX_FAILURE_MESSAGE_LENGTH),
                            severity,
                            detectedAt);
                    return existing;
                })
                .orElseGet(() -> incidentRepository.save(PaymentReconciliationIncident.builder()
                        .dedupeKey(dedupeKey)
                        .issueType(issueType)
                        .status(PaymentReconciliationIncidentStatus.OPEN)
                        .severity(severity)
                        .paymentOrder(paymentOrder)
                        .billingAgreement(billingAgreement)
                        .user(user)
                        .orderId(orderId)
                        .provider(provider)
                        .purpose(purpose)
                        .localStatus(localStatus)
                        .providerStatus(providerStatus)
                        .localAmount(localAmount)
                        .providerAmount(providerAmount)
                        .providerTransactionId(providerTransactionId)
                        .failureCode(truncate(failureCode, MAX_FAILURE_CODE_LENGTH))
                        .failureMessage(truncate(failureMessage, MAX_FAILURE_MESSAGE_LENGTH))
                        .occurrenceCount(1)
                        .firstDetectedAt(detectedAt)
                        .lastDetectedAt(detectedAt)
                        .build()));

        notifyOperatorIfNeeded(incident);
        return incident;
    }

    private void recordIncidentAudit(
            PaymentReconciliationIncident incident,
            PaymentReconciliationIncidentStatus beforeStatus,
            PaymentReconciliationIncidentStatus afterStatus,
            String note) {
        auditLogServiceProvider.ifAvailable(auditLogService ->
                auditLogService.recordReconciliationIncidentStatusUpdate(
                        null,
                        incident,
                        beforeStatus,
                        afterStatus,
                        truncate(note, MAX_NOTE_LENGTH)));
    }

    private String recoveryEvidenceNote(PaymentReconciliationService.ProviderReconciliationIssue issue) {
        String nextLocalStatus = issue.failureCode() == null
                ? PaymentOrderStatus.PROVIDER_SUCCEEDED.name()
                : issue.localStatus();
        return "Reconciliation evidence detected: oldLocalStatus=%s, newLocalStatus=%s, "
                .formatted(
                        nullText(issue.localStatus()),
                        nullText(nextLocalStatus))
                + "providerStatus=%s, localAmount=%s, "
                .formatted(
                        nullText(issue.providerStatus()),
                        nullText(issue.localAmount()))
                + "providerAmount=%s, localCurrency=%s, providerCurrency=%s, transactionId=%s."
                .formatted(
                        nullText(issue.providerAmount()),
                        nullText(issue.localCurrency()),
                        nullText(issue.providerCurrency()),
                        nullText(issue.providerTransactionId()));
    }

    private PaymentOrder findPaymentOrder(Long paymentOrderId) {
        if (paymentOrderId == null) {
            return null;
        }
        return paymentOrderRepository.findById(paymentOrderId).orElse(null);
    }

    private BillingAgreement findBillingAgreement(Long billingAgreementId) {
        if (billingAgreementId == null) {
            return null;
        }
        return billingAgreementRepository.findById(billingAgreementId).orElse(null);
    }

    private User resolveUser(PaymentOrder paymentOrder, BillingAgreement billingAgreement, Long userId) {
        if (paymentOrder != null) {
            return paymentOrder.getUser();
        }
        if (billingAgreement != null) {
            return billingAgreement.getUser();
        }
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }

    private void notifyOperatorIfNeeded(PaymentReconciliationIncident incident) {
        PaymentProperties.Operations operations = paymentProperties.getOperations();
        if (!operations.isReconciliationNotificationEnabled()) {
            return;
        }
        String operatorEmail = operations.getOperatorEmail();
        if (operatorEmail == null || operatorEmail.isBlank() || !incident.shouldNotify()) {
            return;
        }

        String summary = "%s %s incident detected for orderId=%s"
                .formatted(incident.getSeverity(), incident.getIssueType(), nullText(incident.getOrderId()));
        String details = """
                incidentId=%s
                dedupeKey=%s
                status=%s
                provider=%s
                purpose=%s
                localStatus=%s
                providerStatus=%s
                localAmount=%s
                providerAmount=%s
                occurrenceCount=%s
                detectedAt=%s
                failureCode=%s
                failureMessage=%s
                """.formatted(
                nullText(incident.getId()),
                incident.getDedupeKey(),
                incident.getStatus(),
                nullText(incident.getProvider()),
                nullText(incident.getPurpose()),
                nullText(incident.getLocalStatus()),
                nullText(incident.getProviderStatus()),
                nullText(incident.getLocalAmount()),
                nullText(incident.getProviderAmount()),
                incident.getOccurrenceCount(),
                incident.getLastDetectedAt(),
                nullText(incident.getFailureCode()),
                nullText(incident.getFailureMessage()));

        emailService.sendPaymentReconciliationIncidentAlert(operatorEmail, summary, details);
        incident.markNotified(LocalDateTime.now());
        log.warn("Payment reconciliation incident notification queued. incidentId={}, dedupeKey={}",
                incident.getId(),
                incident.getDedupeKey());
    }

    private String dedupeKey(
            PaymentReconciliationIssueType issueType,
            String orderId,
            Long paymentOrderId,
            Long billingAgreementId) {
        if (orderId != null && !orderId.isBlank()) {
            return issueType + ":order:" + orderId;
        }
        if (paymentOrderId != null) {
            return issueType + ":paymentOrder:" + paymentOrderId;
        }
        if (billingAgreementId != null) {
            return issueType + ":billingAgreement:" + billingAgreementId;
        }
        return issueType + ":unknown";
    }

    private PaymentReconciliationIncidentSeverity severity(PaymentReconciliationIssueType issueType) {
        return switch (issueType) {
            case PROVIDER_DONE_LOCAL_NOT_FINALIZED, DONE_ORDER_WITHOUT_PAYMENT, AMOUNT_MISMATCH ->
                    PaymentReconciliationIncidentSeverity.CRITICAL;
            case ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION,
                    LOCAL_DONE_PROVIDER_NOT_FOUND,
                    LOCAL_DONE_PROVIDER_NOT_DONE,
                    PROVIDER_LOOKUP_FAILED -> PaymentReconciliationIncidentSeverity.WARNING;
        };
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizeFailureCode(String failureCode) {
        if (failureCode == null || failureCode.isBlank()) {
            return BILLING_KEY_DELETE_FAILED;
        }
        return failureCode;
    }

    private String normalizeFailureMessage(String failureMessage) {
        if (failureMessage == null || failureMessage.isBlank()) {
            return BILLING_KEY_DELETE_FAILURE_MESSAGE;
        }
        return failureMessage;
    }

    private String nullText(Object value) {
        return value == null ? "-" : value.toString();
    }
}
