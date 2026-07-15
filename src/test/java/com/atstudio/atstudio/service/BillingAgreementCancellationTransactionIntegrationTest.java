package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentProviderOutcomeUnknownException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentOperationAuditLogService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementCleanupTransactionService.class,
        BillingAgreementCleanupProviderExecutor.class,
        BillingAgreementApplicationService.class,
        WithdrawalBillingCleanupService.class,
        BillingAgreementCleanupIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Billing agreement cancellation transaction integration tests")
class BillingAgreementCancellationTransactionIntegrationTest
        extends BillingAgreementCleanupIntegrationTestSupport {

    @Test
    @DisplayName("cancellation claim commits before provider deletion and success clears key once")
    void cancellation_successCommitsClaimBeforeProvider() {
        Fixture fixture = persistFixture(false);
        cleanupProvider.cancelProbe(() -> {
            BillingAgreement claimed = reloadAgreement(fixture.agreementID());
            assertThat(claimed.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
            assertThat(claimed.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
            assertThat(claimed.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
            assertThat(reloadSubscription(fixture.subscriptionID()).getStatus())
                    .isEqualTo(SubscriptionStatus.CANCELLED);
        });

        var response = applicationService.cancelMyBillingAgreement(userDetails(fixture.userID()));

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(response.status()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(reloaded.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.NONE);
        assertThat(reloaded.getBillingKeyCiphertext()).isNull();
        assertThat(reloaded.getBillingKeyFingerprint()).isNull();
        assertThat(cleanupProvider.transactionActiveAtCancel()).isFalse();
        assertThat(cleanupProvider.calls()).containsExactly("cancel");
        assertThat(incidentRepository.count()).isZero();
    }

    @Test
    @DisplayName("deterministic provider failure preserves cancellation and recoverable evidence")
    void cancellation_deterministicFailurePersistsFailedCleanup() {
        Fixture fixture = persistFixture(false);
        cleanupProvider.cancelResult(BillingAgreementCancelResult.failure(
                "DELETE_DECLINED",
                "Provider rejected deletion."));

        assertCancellationFailure(fixture);

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        PaymentReconciliationIncident incident = incidentRepository.findAll().get(0);
        assertThat(reloaded.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(reloaded.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.FAILED);
        assertThat(reloaded.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(reloadSubscription(fixture.subscriptionID()).getStatus())
                .isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(incident.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(incident.getFailureCode()).isEqualTo("DELETE_DECLINED");
        assertThat(auditLogRepository.findAll()).singleElement().satisfies(audit -> {
            assertThat(audit.getBeforeStatus()).isNull();
            assertThat(audit.getAfterStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN.name());
        });
    }

    @Test
    @DisplayName("unknown provider outcome remains pending and is not replayed")
    void cancellation_unknownOutcomeRemainsPending() {
        Fixture fixture = persistFixture(false);
        cleanupProvider.cancelException(new PaymentProviderOutcomeUnknownException("transport detail"));

        assertCancellationFailure(fixture);

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(reloaded.getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(reloaded.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(incidentRepository.findAll().get(0).getFailureMessage())
                .isEqualTo("PaymentProviderOutcomeUnknownException");

        assertCancellationFailure(fixture);
        assertThat(cleanupProvider.calls()).containsExactly("cancel");
    }

    @Test
    @DisplayName("stale cleanup becomes pending and fences a delayed old success")
    void cancellation_staleClaimIsDetectOnlyAndFencesOldResult() {
        Fixture fixture = persistFixture(false);
        LocalDateTime oldClaimTime = LocalDateTime.now().minusMinutes(16);
        BillingAgreementCleanupTransactionService.UserCancellationClaim oldClaim =
                cleanupTransactionService.claimUserCancellation(fixture.userID(), oldClaimTime);

        assertCancellationFailure(fixture);

        BillingAgreement reloaded = reloadAgreement(fixture.agreementID());
        assertThat(reloaded.getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(cleanupProvider.calls()).isEmpty();
        assertThatThrownBy(() -> cleanupTransactionService.recordUserCancellationResult(
                fixture.userID(),
                oldClaim,
                BillingAgreementCleanupProviderExecutor.CleanupProviderResult.succeeded()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE));
        assertThat(reloadAgreement(fixture.agreementID()).getBillingKeyCiphertext())
                .isEqualTo("encrypted-key");
    }

    private void assertCancellationFailure(Fixture fixture) {
        assertThatThrownBy(() -> applicationService.cancelMyBillingAgreement(userDetails(fixture.userID())))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_CANCEL_FAILED));
    }
}
