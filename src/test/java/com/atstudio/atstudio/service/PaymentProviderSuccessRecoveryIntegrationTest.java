package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Provider success and local finalization recovery integration tests")
class PaymentProviderSuccessRecoveryIntegrationTest
        extends BillingAgreementCommandIntegrationTestSupport {

    @Test
    @DisplayName("provider success commits before finalization and retry finalizes without a second charge")
    void providerSuccessSurvivesFinalizationFailureAndRetryDoesNotChargeAgain() {
        Fixture fixture = persistPreparedOrder();
        doThrow(new IllegalStateException("forced finalization failure"))
                .doNothing()
                .when(playlistService)
                .createDefaultPlaylist(any(User.class));

        BillingAgreementConfirmRequest request = new BillingAgreementConfirmRequest(
                ORDER_ID,
                "auth_key",
                CUSTOMER_KEY,
                AMOUNT);

        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forced finalization failure");

        PaymentOrder providerSucceeded = reloadOrder();
        BillingAgreement retainedAgreement = reloadAgreement(fixture.agreementID());
        assertThat(providerSucceeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(providerSucceeded.getPgTransactionId()).isEqualTo("tx_wi005");
        assertThat(retainedAgreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(userSubscriptionRepository.count()).isZero();
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge");

        BillingAgreementConfirmResponse response =
                service.confirmBillingAgreement(userDetails(fixture.userID()), request);

        PaymentOrder finalized = reloadOrder();
        BillingAgreement activeAgreement = reloadAgreement(fixture.agreementID());
        SubscriptionPayment payment = subscriptionPaymentRepository.findByPaymentOrder(finalized).orElseThrow();
        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(finalized.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(activeAgreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(payment.getPaymentOrder().getId()).isEqualTo(finalized.getId());
        assertThat(userSubscriptionRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge");
        assertThat(recurringPaymentProvider.lastChargeCommand().idempotencyKey())
                .isEqualTo("billing-initial-" + ORDER_ID + "-attempt-1");
        verify(playlistService, times(2)).createDefaultPlaylist(any(User.class));
        verify(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));
    }
}
