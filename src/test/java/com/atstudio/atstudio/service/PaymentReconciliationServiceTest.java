package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationService unit tests")
class PaymentReconciliationServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentStatusLookupProvider paymentStatusLookupProvider;
    @Mock PaymentReconciliationIncidentService paymentReconciliationIncidentService;

    @Test
    @DisplayName("scheduled reconciliation records detected issues into incident storage")
    void reconcilePaymentLedgersOnSchedule_recordsIncidents() {
        PaymentReconciliationService service = new PaymentReconciliationService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                List.of(),
                paymentReconciliationIncidentService);

        given(paymentOrderRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of()));
        given(billingAgreementRepository.findByStatus(BillingAgreementStatus.ACTIVE)).willReturn(List.of());

        service.reconcilePaymentLedgersOnSchedule();

        verify(paymentReconciliationIncidentService).recordIssues(any(), any());
    }

    @Test
    @DisplayName("reconcileLocalLedger reports missing finalized payment rows")
    void reconcileLocalLedger_reportsDoneOrderWithoutPayment() {
        PaymentReconciliationService service = new PaymentReconciliationService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                List.of(),
                paymentReconciliationIncidentService);
        User user = buildUser(1L);
        PaymentOrder doneOrder = buildOrder(user);
        doneOrder.markDone("tx_1", null, "{}");

        given(paymentOrderRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(doneOrder)));
        given(subscriptionPaymentRepository.existsByPaymentOrder(doneOrder)).willReturn(false);
        given(billingAgreementRepository.findByStatus(BillingAgreementStatus.ACTIVE)).willReturn(List.of());

        PaymentReconciliationService.ReconciliationResult result = service.reconcileLocalLedger();

        assertThat(result.checkedOrders()).isEqualTo(1);
        assertThat(result.doneOrdersWithoutPayment()).isEqualTo(1);
        assertThat(result.issues()).extracting(PaymentReconciliationService.LocalReconciliationIssue::issueType)
                .containsExactly(PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT);
        assertThat(result.hasMismatch()).isTrue();
    }

    @Test
    @DisplayName("reconcileLocalLedger reports active agreement without active subscription")
    void reconcileLocalLedger_reportsAgreementWithoutSubscription() {
        PaymentReconciliationService service = new PaymentReconciliationService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                List.of(),
                paymentReconciliationIncidentService);
        User user = buildUser(1L);
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-key")
                .build();
        ReflectionTestUtils.setField(agreement, "id", 10L);
        agreement.activate("encrypted", "fingerprint", "CARD", "1234", LocalDate.now().plusMonths(1));

        given(paymentOrderRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of()));
        given(billingAgreementRepository.findByStatus(BillingAgreementStatus.ACTIVE)).willReturn(List.of(agreement));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());

        PaymentReconciliationService.ReconciliationResult result = service.reconcileLocalLedger();

        assertThat(result.checkedBillingAgreements()).isEqualTo(1);
        assertThat(result.activeAgreementsWithoutSubscription()).isEqualTo(1);
        assertThat(result.issues()).extracting(PaymentReconciliationService.LocalReconciliationIssue::issueType)
                .containsExactly(PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION);
        assertThat(result.hasMismatch()).isTrue();
    }

    @Test
    @DisplayName("reconcileProviderLedger reports provider DONE orders that are not finalized locally")
    void reconcileProviderLedger_reportsProviderDoneWithoutLocalFinalization() {
        PaymentReconciliationService service = new PaymentReconciliationService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                List.of(paymentStatusLookupProvider),
                paymentReconciliationIncidentService);
        User user = buildUser(1L);
        PaymentOrder order = buildOrder(user);

        given(paymentOrderRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(order)));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId("ATS-DONE"))
                .willReturn(ProviderPaymentLookupResult.found(
                        PaymentProviderType.TOSS_BILLING,
                        "ATS-DONE",
                        "payment_key",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        "{\"paymentKey\":\"payment_key\"}"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(1);
        assertThat(result.providerDoneWithoutLocalFinalization()).isEqualTo(1);
        assertThat(result.issues()).extracting(PaymentReconciliationService.ProviderReconciliationIssue::issueType)
                .containsExactly(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        assertThat(result.hasMismatch()).isTrue();
    }

    @Test
    @DisplayName("reconcileProviderLedger skips provider lookup when provider configuration is unavailable")
    void reconcileProviderLedger_skipsUnconfiguredLookupProvider() {
        PaymentReconciliationService service = new PaymentReconciliationService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                List.of(paymentStatusLookupProvider),
                paymentReconciliationIncidentService);
        User user = buildUser(1L);
        PaymentOrder order = buildOrder(user);

        given(paymentOrderRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(order)));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(false);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(1);
        assertThat(result.skippedOrders()).isEqualTo(1);
        assertThat(result.hasMismatch()).isFalse();
    }

    private User buildUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("user" + id)
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private PaymentOrder buildOrder(User user) {
        Subscription subscription = Subscription.builder()
                .name("Basic")
                .description("Test plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build();
        return PaymentOrder.builder()
                .orderId("ATS-DONE")
                .user(user)
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }
}
