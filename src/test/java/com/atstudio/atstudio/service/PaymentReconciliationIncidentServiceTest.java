package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.payment.ProviderSupportReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationIncidentService unit tests")
class PaymentReconciliationIncidentServiceTest {

    @Mock PaymentReconciliationIncidentRepository incidentRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @Mock PaymentOperationAuditLogService auditLogService;
    @Mock ObjectProvider<PaymentOperationAuditLogService> auditLogServiceProvider;

    PaymentProperties paymentProperties;
    PaymentReconciliationIncidentService service;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        paymentProperties.getOperations().setReconciliationNotificationEnabled(true);
        paymentProperties.getOperations().setOperatorEmail("ops@test.com");
        service = new PaymentReconciliationIncidentService(
                incidentRepository,
                paymentOrderRepository,
                billingAgreementRepository,
                userRepository,
                paymentProperties,
                emailService,
                auditLogServiceProvider);
    }

    @Test
    @DisplayName("recordIssues creates a new incident and sends an operator email when enabled")
    void recordIssues_createsIncidentAndSendsNotification() {
        given(incidentRepository.findByDedupeKey(
                "PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-1"))
                .willReturn(Optional.empty());
        given(incidentRepository.save(any(PaymentReconciliationIncident.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        PaymentReconciliationService.ProviderReconciliationResult providerResult =
                providerResult(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);

        service.recordIssues(emptyLocalResult(), providerResult);

        ArgumentCaptor<PaymentReconciliationIncident> captor =
                ArgumentCaptor.forClass(PaymentReconciliationIncident.class);
        verify(incidentRepository).save(captor.capture());
        PaymentReconciliationIncident incident = captor.getValue();
        assertThat(incident.getIssueType())
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        assertThat(incident.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(incident.getOccurrenceCount()).isEqualTo(1);
        assertThat(incident.getNotifiedAt()).isNotNull();
        verify(emailService).sendPaymentReconciliationIncidentAlert(
                eq("ops@test.com"),
                contains("CRITICAL"),
                contains("dedupeKey=PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-1"));
    }

    @Test
    @DisplayName("provider recovery stores and audits only a masked transaction identifier")
    void recordProviderRecoveryIssueMasksTransactionIdentifier() {
        String rawPaymentKey = "pay_0123456789_abcdef";
        String dedupeKey = "PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-MASK";
        paymentProperties.getOperations().setReconciliationNotificationEnabled(false);
        enableAuditService();
        given(incidentRepository.findByDedupeKey(dedupeKey)).willReturn(Optional.empty());
        given(incidentRepository.save(any(PaymentReconciliationIncident.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        PaymentReconciliationService.ProviderReconciliationIssue issue =
                new PaymentReconciliationService.ProviderReconciliationIssue(
                        PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                        null,
                        null,
                        null,
                        "ATS-REN-MASK",
                        PaymentProviderType.TOSS,
                        PaymentPurpose.RENEWAL,
                        "PENDING_PROVIDER_CONFIRMATION",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        BigDecimal.valueOf(9900),
                        "KRW",
                        "KRW",
                        rawPaymentKey,
                        null,
                        null);

        service.recordProviderRecoveryIssue(issue);

        ArgumentCaptor<PaymentReconciliationIncident> incidentCaptor =
                ArgumentCaptor.forClass(PaymentReconciliationIncident.class);
        verify(incidentRepository).save(incidentCaptor.capture());
        assertThat(incidentCaptor.getValue().getProviderTransactionId())
                .isEqualTo(ProviderSupportReference.from(rawPaymentKey))
                .doesNotContain(rawPaymentKey.substring(0, 4))
                .doesNotContain(rawPaymentKey.substring(rawPaymentKey.length() - 4));
        ArgumentCaptor<String> noteCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).recordReconciliationIncidentStatusUpdate(
                eq(null),
                eq(incidentCaptor.getValue()),
                eq(null),
                eq(PaymentReconciliationIncidentStatus.OPEN),
                noteCaptor.capture());
        assertThat(noteCaptor.getValue())
                .doesNotContain(rawPaymentKey)
                .doesNotContain(rawPaymentKey.substring(0, 4))
                .doesNotContain(rawPaymentKey.substring(rawPaymentKey.length() - 4))
                .doesNotContain("transactionId=");
    }

    @Test
    @DisplayName("recordIssues reopens a resolved incident when the same mismatch appears again")
    void recordIssues_reopensResolvedIncident() {
        PaymentReconciliationIncident existing = PaymentReconciliationIncident.builder()
                .dedupeKey("PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-1")
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.RESOLVED)
                .severity(com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity.CRITICAL)
                .occurrenceCount(1)
                .firstDetectedAt(LocalDateTime.now().minusDays(1))
                .lastDetectedAt(LocalDateTime.now().minusDays(1))
                .resolvedAt(LocalDateTime.now().minusHours(1))
                .resolutionNote("manually resolved")
                .build();
        given(incidentRepository.findByDedupeKey(existing.getDedupeKey()))
                .willReturn(Optional.of(existing));

        service.recordIssues(
                emptyLocalResult(),
                providerResult(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED));

        assertThat(existing.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(existing.getOccurrenceCount()).isEqualTo(2);
        assertThat(existing.getResolvedAt()).isNull();
        assertThat(existing.getResolutionNote()).isNull();
        assertThat(existing.getNotifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("recordIssues keeps ignored incidents ignored and does not send a new email")
    void recordIssues_keepsIgnoredIncidentIgnored() {
        PaymentReconciliationIncident existing = PaymentReconciliationIncident.builder()
                .dedupeKey("PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-1")
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.IGNORED)
                .severity(com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity.CRITICAL)
                .occurrenceCount(1)
                .firstDetectedAt(LocalDateTime.now().minusDays(1))
                .lastDetectedAt(LocalDateTime.now().minusDays(1))
                .resolvedAt(LocalDateTime.now().minusHours(1))
                .resolutionNote("expected sandbox mismatch")
                .build();
        given(incidentRepository.findByDedupeKey(existing.getDedupeKey()))
                .willReturn(Optional.of(existing));

        service.recordIssues(
                emptyLocalResult(),
                providerResult(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED));

        assertThat(existing.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.IGNORED);
        assertThat(existing.getOccurrenceCount()).isEqualTo(2);
        verify(emailService, never()).sendPaymentReconciliationIncidentAlert(any(), any(), any());
    }

    @Test
    @DisplayName("billing cleanup failure creates a warning incident with agreement and user references")
    void recordBillingCleanupFailure_createsIncident() {
        paymentProperties.getOperations().setReconciliationNotificationEnabled(false);
        BillingAgreement agreement = billingAgreement(90L);
        String dedupeKey = "LOCAL_DONE_PROVIDER_NOT_DONE:billingAgreement:90";
        given(incidentRepository.findByDedupeKey(dedupeKey)).willReturn(Optional.empty());
        given(incidentRepository.save(any(PaymentReconciliationIncident.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.recordBillingCleanupFailure(agreement, null, null);

        ArgumentCaptor<PaymentReconciliationIncident> captor =
                ArgumentCaptor.forClass(PaymentReconciliationIncident.class);
        verify(incidentRepository).save(captor.capture());
        PaymentReconciliationIncident incident = captor.getValue();
        assertThat(incident.getDedupeKey()).isEqualTo(dedupeKey);
        assertThat(incident.getIssueType())
                .isEqualTo(PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE);
        assertThat(incident.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(incident.getSeverity()).isEqualTo(PaymentReconciliationIncidentSeverity.WARNING);
        assertThat(incident.getBillingAgreement()).isSameAs(agreement);
        assertThat(incident.getUser()).isSameAs(agreement.getUser());
        assertThat(incident.getLocalStatus()).isEqualTo("CANCELLED");
        assertThat(incident.getProviderStatus()).isEqualTo("BILLING_KEY_DELETE_FAILED");
        assertThat(incident.getFailureCode()).isEqualTo("BILLING_KEY_DELETE_FAILED");
        assertThat(incident.getFailureMessage()).isEqualTo("Provider billing key deletion failed.");
    }

    @Test
    @DisplayName("billing cleanup failures use one agreement-scoped incident and increment occurrence count")
    void recordBillingCleanupFailure_deduplicatesByAgreement() {
        paymentProperties.getOperations().setReconciliationNotificationEnabled(false);
        BillingAgreement agreement = billingAgreement(91L);
        String dedupeKey = "LOCAL_DONE_PROVIDER_NOT_DONE:billingAgreement:91";
        PaymentReconciliationIncident existing = PaymentReconciliationIncident.builder()
                .dedupeKey(dedupeKey)
                .issueType(PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE)
                .status(PaymentReconciliationIncidentStatus.RESOLVED)
                .severity(PaymentReconciliationIncidentSeverity.WARNING)
                .billingAgreement(agreement)
                .user(agreement.getUser())
                .localStatus("CANCELLED")
                .providerStatus("BILLING_KEY_DELETE_FAILED")
                .occurrenceCount(1)
                .firstDetectedAt(LocalDateTime.now().minusDays(1))
                .lastDetectedAt(LocalDateTime.now().minusDays(1))
                .resolvedAt(LocalDateTime.now().minusHours(1))
                .build();
        given(incidentRepository.findByDedupeKey(dedupeKey)).willReturn(Optional.of(existing));

        service.recordBillingCleanupFailure(agreement, "DELETE_FAILED", "provider rejected deletion");

        assertThat(existing.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(existing.getOccurrenceCount()).isEqualTo(2);
        assertThat(existing.getBillingAgreement()).isSameAs(agreement);
        assertThat(existing.getUser()).isSameAs(agreement.getUser());
        assertThat(existing.getLocalStatus()).isEqualTo("CANCELLED");
        assertThat(existing.getProviderStatus()).isEqualTo("BILLING_KEY_DELETE_FAILED");
        assertThat(existing.getFailureCode()).isEqualTo("DELETE_FAILED");
        assertThat(existing.getFailureMessage()).isEqualTo("provider rejected deletion");
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("successful billing cleanup resolves its matching incident")
    void resolveBillingCleanupIncident_resolvesMatchingIncident() {
        BillingAgreement agreement = billingAgreement(92L);
        String dedupeKey = "LOCAL_DONE_PROVIDER_NOT_DONE:billingAgreement:92";
        PaymentReconciliationIncident existing = PaymentReconciliationIncident.builder()
                .dedupeKey(dedupeKey)
                .issueType(PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE)
                .status(PaymentReconciliationIncidentStatus.OPEN)
                .severity(PaymentReconciliationIncidentSeverity.WARNING)
                .occurrenceCount(1)
                .firstDetectedAt(LocalDateTime.now().minusDays(1))
                .lastDetectedAt(LocalDateTime.now().minusDays(1))
                .build();
        given(incidentRepository.findByDedupeKey(dedupeKey)).willReturn(Optional.of(existing));

        service.resolveBillingCleanupIncident(agreement);

        assertThat(existing.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(existing.getResolvedAt()).isNotNull();
        assertThat(existing.getResolutionNote()).isEqualTo("Provider billing key cleanup completed.");
    }

    @Test
    @DisplayName("successful finalize-only recovery resolves the matching provider Incident and audits it")
    void resolveProviderRecoveryIncidents_resolvesAndAuditsMatchingIncident() {
        enableAuditService();
        String dedupeKey = "PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-1";
        PaymentReconciliationIncident existing = PaymentReconciliationIncident.builder()
                .dedupeKey(dedupeKey)
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.OPEN)
                .severity(PaymentReconciliationIncidentSeverity.CRITICAL)
                .occurrenceCount(1)
                .firstDetectedAt(LocalDateTime.now().minusMinutes(5))
                .lastDetectedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        given(incidentRepository.findByDedupeKey(dedupeKey)).willReturn(Optional.of(existing));

        service.resolveProviderRecoveryIncidents("ATS-REN-1");

        assertThat(existing.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(existing.getResolutionNote())
                .isEqualTo("Provider payment evidence was finalized locally without another charge.");
        verify(auditLogService).recordReconciliationIncidentStatusUpdate(
                null,
                existing,
                PaymentReconciliationIncidentStatus.OPEN,
                PaymentReconciliationIncidentStatus.RESOLVED,
                "Provider payment evidence was finalized locally without another charge.");
    }

    private void enableAuditService() {
        doAnswer(invocation -> {
            Consumer<PaymentOperationAuditLogService> consumer = invocation.getArgument(0);
            consumer.accept(auditLogService);
            return null;
        }).when(auditLogServiceProvider).ifAvailable(any());
    }

    private PaymentReconciliationService.ReconciliationResult emptyLocalResult() {
        return new PaymentReconciliationService.ReconciliationResult(
                0,
                0,
                0,
                0,
                0,
                false,
                List.of());
    }

    private PaymentReconciliationService.ProviderReconciliationResult providerResult(
            PaymentReconciliationIssueType issueType) {
        PaymentReconciliationService.ProviderReconciliationIssue issue =
                new PaymentReconciliationService.ProviderReconciliationIssue(
                        issueType,
                        null,
                        1L,
                        null,
                        "ATS-REN-1",
                        PaymentProviderType.TOSS,
                        PaymentPurpose.RENEWAL,
                        "IN_PROGRESS",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        BigDecimal.valueOf(9900),
                        "KRW",
                        "KRW",
                        "payment_key",
                        null,
                        null);
        return new PaymentReconciliationService.ProviderReconciliationResult(
                1,
                0,
                0,
                0,
                1,
                0,
                0,
                0,
                1,
                false,
                List.of(issue));
    }

    private BillingAgreement billingAgreement(Long id) {
        User user = User.builder()
                .nickname("withdrawn-user")
                .email("withdrawn@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.withdraw();
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("customer-key-" + id)
                .build();
        ReflectionTestUtils.setField(agreement, "id", id);
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", java.time.LocalDate.now());
        agreement.cancel();
        return agreement;
    }
}
