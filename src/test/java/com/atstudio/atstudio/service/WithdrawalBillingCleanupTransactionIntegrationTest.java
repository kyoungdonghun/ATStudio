package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentProviderOutcomeUnknownException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        PaymentOperationAuditLogService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementCleanupTransactionService.class,
        BillingAgreementCleanupProviderExecutor.class,
        BillingAgreementApplicationService.class,
        WithdrawalBillingCleanupService.class,
        BillingAgreementCleanupIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Withdrawal billing cleanup transaction integration tests")
class WithdrawalBillingCleanupTransactionIntegrationTest
        extends BillingAgreementCleanupIntegrationTestSupport {

    @MockitoBean BillingAgreementPrepareTransactionService billingAgreementPrepareTransactionService;

    @Test
    @DisplayName("withdrawal claim commits before provider deletion and already-removed converges")
    void withdrawal_alreadyRemovedConvergesAfterDurableClaim() {
        Fixture fixture = persistFixture(true);
        cleanupProvider.cancelResult(BillingAgreementCancelResult.failure(
                "ALREADY_REMOVED_BILLING_KEY",
                "Billing key was already removed."));
        cleanupProvider.cancelProbe(() -> {
            BillingAgreement claimed = reloadAgreement(fixture.agreementID());
            assertThat(claimed.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
            assertThat(claimed.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        });

        WithdrawalBillingCleanupService.CleanupOutcome outcome =
                withdrawalCleanupService.cleanup(fixture.agreementID());

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.SUCCEEDED);
        assertThat(reloaded.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.NONE);
        assertThat(reloaded.getBillingKeyCiphertext()).isNull();
        assertThat(cleanupProvider.transactionActiveAtCancel()).isFalse();
        assertThat(cleanupProvider.calls()).containsExactly("cancel");
    }

    @Test
    @DisplayName("fresh competition loses explicitly without a second provider call")
    void withdrawal_freshCompetitionReturnsInProgress() {
        Fixture fixture = persistFixture(true);
        cleanupTransactionService.claimWithdrawalCleanup(
                fixture.agreementID(),
                LocalDateTime.now());

        WithdrawalBillingCleanupService.CleanupOutcome outcome =
                withdrawalCleanupService.cleanup(fixture.agreementID());

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.IN_PROGRESS);
        assertThat(reloadAgreement(fixture.agreementID()).getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PROCESSING);
        assertThat(cleanupProvider.calls()).isEmpty();
    }

    @Test
    @DisplayName("bounded stale scan is detect-only and never replays provider deletion")
    void withdrawal_staleScanMovesClaimToPendingWithoutReplay() {
        Fixture fixture = persistFixture(true);
        cleanupTransactionService.claimWithdrawalCleanup(
                fixture.agreementID(),
                LocalDateTime.now().minusMinutes(16));

        int markedPending = withdrawalCleanupService.detectStaleCleanupClaims();

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(markedPending).isEqualTo(1);
        assertThat(reloaded.getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(reloaded.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(incidentRepository.count()).isEqualTo(1);
        assertThat(auditLogRepository.count()).isEqualTo(1);
        assertThat(cleanupProvider.calls()).isEmpty();
    }

    @Test
    @DisplayName("unknown withdrawal provider outcome remains pending with safe evidence")
    void withdrawal_unknownOutcomeRemainsPending() {
        Fixture fixture = persistFixture(true);
        cleanupProvider.cancelException(new PaymentProviderOutcomeUnknownException("raw transport detail"));

        WithdrawalBillingCleanupService.CleanupOutcome outcome =
                withdrawalCleanupService.cleanup(fixture.agreementID());

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(outcome)
                .isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.PENDING_PROVIDER_CONFIRMATION);
        assertThat(reloaded.getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(reloaded.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(incidentRepository.findAll().get(0).getFailureMessage())
                .isEqualTo("PaymentProviderOutcomeUnknownException");
    }
}
