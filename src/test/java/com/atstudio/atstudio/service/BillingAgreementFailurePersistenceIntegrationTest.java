package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
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
        PaymentCommandKeyFactory.class,
        BillingAgreementPrepareTransactionService.class,
        PaymentCommandTransactionService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Initial billing-confirm failure persistence integration tests")
class BillingAgreementFailurePersistenceIntegrationTest
        extends BillingAgreementCommandIntegrationTestSupport {

    @Test
    @DisplayName("stale processing becomes pending confirmation and is never blindly replayed")
    void staleProcessingBecomesPendingWithoutProviderReplay() {
        Fixture fixture = persistPreparedOrder();
        PaymentOrder staleOrder = reloadOrder();
        staleOrder.claimProviderAttempt(
                "BILLING_CONFIRM:" + ORDER_ID,
                "billing-initial-" + ORDER_ID + "-attempt-1",
                LocalDateTime.now().minusMinutes(16));
        paymentOrderRepository.saveAndFlush(staleOrder);

        assertThatThrownBy(() -> service.confirmBillingAgreement(
                userDetails(fixture.userID()),
                new BillingAgreementConfirmRequest(
                        ORDER_ID,
                        "auth_key",
                        CUSTOMER_KEY,
                        AMOUNT)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        PaymentOrder reloaded = reloadOrder();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(reloaded.getFailureCode()).isEqualTo("STALE_BILLING_CONFIRM");
        assertThat(recurringPaymentProvider.calls()).isEmpty();
    }

    @Test
    @DisplayName("billing-key issue failure is committed before the API exception escapes")
    void issueFailureSurvivesBusinessException() {
        Fixture fixture = persistPreparedOrder();
        recurringPaymentProvider.confirmResult(
                BillingAgreementConfirmResult.failure("ISSUE_DECLINED", "Billing authorization was declined."));
        recurringPaymentProvider.confirmProbe(() -> {
            PaymentOrder claimed = reloadOrder();
            assertThat(claimed.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
            assertThat(claimed.getCommandKey()).isEqualTo("BILLING_CONFIRM:" + ORDER_ID);
            assertThat(claimed.getProviderIdempotencyKey())
                    .isEqualTo("billing-initial-" + ORDER_ID + "-attempt-1");
        });

        assertConfirmFailure(fixture);

        PaymentOrder reloaded = reloadOrder();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(reloaded.getFailureCode()).isEqualTo("ISSUE_DECLINED");
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm");
    }

    @Test
    @DisplayName("charge failure commits before cleanup and clears encrypted key only after cleanup succeeds")
    void chargeFailureCommitsBeforeSuccessfulCleanup() {
        Fixture fixture = persistPreparedOrder();
        recurringPaymentProvider.chargeResult(
                BillingChargeResult.failure("DECLINED", "Initial charge failed."));
        recurringPaymentProvider.chargeProbe(() -> {
            PaymentOrder beforeCharge = reloadOrder();
            BillingAgreement agreement = reloadAgreement(fixture.agreementID());
            assertThat(beforeCharge.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
            assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        });
        recurringPaymentProvider.cancelProbe(() -> {
            PaymentOrder failedBeforeCleanup = reloadOrder();
            BillingAgreement agreement = reloadAgreement(fixture.agreementID());
            assertThat(failedBeforeCleanup.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
            assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
            assertThat(agreement.getFailureCount()).isEqualTo(1);
        });

        assertConfirmFailure(fixture);

        PaymentOrder reloadedOrder = reloadOrder();
        BillingAgreement reloadedAgreement = reloadAgreement(fixture.agreementID());
        assertThat(reloadedOrder.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(reloadedAgreement.getBillingKeyCiphertext()).isNull();
        assertThat(incidentRepository.count()).isZero();
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge", "cancel");
    }

    @Test
    @DisplayName("cleanup failure retains encrypted key and creates recoverable incident evidence")
    void cleanupFailureRetainsEncryptedKeyAndCreatesIncident() {
        Fixture fixture = persistPreparedOrder();
        recurringPaymentProvider.chargeResult(
                BillingChargeResult.failure("DECLINED", "Initial charge failed."));
        recurringPaymentProvider.cancelResult(
                BillingAgreementCancelResult.failure("DELETE_FAILED", "Provider rejected billing-key cleanup."));

        assertConfirmFailure(fixture);

        PaymentOrder reloadedOrder = reloadOrder();
        BillingAgreement reloadedAgreement = reloadAgreement(fixture.agreementID());
        PaymentReconciliationIncident incident = incidentRepository.findAll().get(0);
        assertThat(reloadedOrder.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(reloadedAgreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(incidentRepository.count()).isEqualTo(1);
        assertThat(incident.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(incident.getFailureCode()).isEqualTo("DELETE_FAILED");
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge", "cancel");
    }

    private void assertConfirmFailure(Fixture fixture) {
        assertThatThrownBy(() -> service.confirmBillingAgreement(
                userDetails(fixture.userID()),
                new BillingAgreementConfirmRequest(
                        ORDER_ID,
                        "auth_key",
                        CUSTOMER_KEY,
                        AMOUNT)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED));
    }
}
