package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalBillingCleanupService unit tests")
class WithdrawalBillingCleanupServiceTest {

    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock PaymentReconciliationIncidentService incidentService;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    WithdrawalBillingCleanupService service;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        service = new WithdrawalBillingCleanupService(
                billingAgreementRepository,
                billingKeyCrypto,
                incidentService,
                List.of(recurringPaymentProvider));
    }

    @Test
    @DisplayName("cleanup runs in a new transaction and clears key material after provider success")
    void cleanup_successClearsKeyAndResolvesIncident() throws Exception {
        Method cleanupMethod = WithdrawalBillingCleanupService.class.getMethod("cleanup", Long.class);
        Transactional transactional = cleanupMethod.getAnnotation(Transactional.class);
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);

        BillingAgreement agreement = eligibleAgreement(11L);
        given(billingAgreementRepository.findById(11L)).willReturn(Optional.of(agreement));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.success("{}"));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(11L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.SUCCEEDED);
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getNextBillingAt()).isNull();
        ArgumentCaptor<BillingAgreementCancelCommand> commandCaptor =
                ArgumentCaptor.forClass(BillingAgreementCancelCommand.class);
        verify(recurringPaymentProvider).cancelAgreement(commandCaptor.capture());
        assertThat(commandCaptor.getValue().billingKey()).isEqualTo("raw-billing-key");
        verify(incidentService).resolveBillingCleanupIncident(agreement);
        verify(incidentService, never()).recordBillingCleanupFailure(any(), any(), any());
    }

    @Test
    @DisplayName("provider failure retains key material and records a durable incident")
    void cleanup_providerFailureRetainsKeyAndRecordsIncident() {
        BillingAgreement agreement = eligibleAgreement(12L);
        given(billingAgreementRepository.findById(12L)).willReturn(Optional.of(agreement));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.failure("DELETE_FAILED", "provider rejected deletion"));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(12L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.FAILED);
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        verify(incidentService).recordBillingCleanupFailure(
                agreement,
                "DELETE_FAILED",
                "provider rejected deletion");
        verify(incidentService, never()).resolveBillingCleanupIncident(any());
    }

    @Test
    @DisplayName("already-removed provider response clears local key material and resolves the incident")
    void cleanup_alreadyRemovedBillingKeyConvergesToSuccess() {
        BillingAgreement agreement = eligibleAgreement(15L);
        given(billingAgreementRepository.findById(15L)).willReturn(Optional.of(agreement));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("already-removed-billing-key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.failure(
                        "ALREADY_REMOVED_BILLING_KEY",
                        "Billing key has already been removed."));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(15L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.SUCCEEDED);
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getBillingKeyFingerprint()).isNull();
        assertThat(agreement.getNextBillingAt()).isNull();
        verify(incidentService).resolveBillingCleanupIncident(agreement);
        verify(incidentService, never()).recordBillingCleanupFailure(any(), any(), any());
    }

    @Test
    @DisplayName("crypto exceptions retain the key and do not persist the raw exception message")
    void cleanup_cryptoExceptionUsesSafeFailureDetails() {
        BillingAgreement agreement = eligibleAgreement(13L);
        given(billingAgreementRepository.findById(13L)).willReturn(Optional.of(agreement));
        given(billingKeyCrypto.decrypt("encrypted-key"))
                .willThrow(new IllegalStateException("raw-billing-secret"));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(13L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.FAILED);
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        verify(incidentService).recordBillingCleanupFailure(
                agreement,
                "BILLING_KEY_DELETE_EXCEPTION",
                "IllegalStateException");
        verify(recurringPaymentProvider, never()).cancelAgreement(any());
    }

    @Test
    @DisplayName("non-deleted users are skipped without decrypting or calling the provider")
    void cleanup_nonDeletedUserIsSkipped() {
        BillingAgreement agreement = eligibleAgreement(14L);
        ReflectionTestUtils.setField(agreement.getUser(), "isDeleted", false);
        given(billingAgreementRepository.findById(14L)).willReturn(Optional.of(agreement));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(14L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.SKIPPED);
        verify(billingKeyCrypto, never()).decrypt(any());
        verify(recurringPaymentProvider, never()).cancelAgreement(any());
        verify(incidentService, never()).recordBillingCleanupFailure(any(), any(), any());
    }

    private BillingAgreement eligibleAgreement(Long id) {
        User user = User.builder()
                .nickname("withdrawn-" + id)
                .email("withdrawn-" + id + "@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        user.withdraw();
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-key-" + id)
                .build();
        ReflectionTestUtils.setField(agreement, "id", id);
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", LocalDate.now());
        agreement.cancel();
        return agreement;
    }
}
