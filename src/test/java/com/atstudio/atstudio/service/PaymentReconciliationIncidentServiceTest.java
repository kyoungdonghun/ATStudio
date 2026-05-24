package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationIncidentService unit tests")
class PaymentReconciliationIncidentServiceTest {

    @Mock PaymentReconciliationIncidentRepository incidentRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;

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
                emailService);
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

    private PaymentReconciliationService.ReconciliationResult emptyLocalResult() {
        return new PaymentReconciliationService.ReconciliationResult(0, 0, 0, 0, List.of());
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
                        PaymentProviderType.TOSS_BILLING,
                        PaymentPurpose.RENEWAL,
                        "IN_PROGRESS",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        BigDecimal.valueOf(9900),
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
                List.of(issue));
    }
}
