package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurringRenewalService unit tests")
class RecurringRenewalServiceTest {

    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock EmailService emailService;
    @Mock PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    RecurringRenewalService service;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        service = new RecurringRenewalService(
                billingAgreementRepository,
                userSubscriptionRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                billingKeyCrypto,
                emailService,
                paymentReceiptEvidenceService,
                List.of(recurringPaymentProvider)
        );
    }

    @Test
    @DisplayName("successful due renewal creates order, saves payment, and extends access")
    void processDueRenewals_success() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, due);
        BillingAgreement agreement = buildActiveAgreement(user, due);

        givenDueAgreement(agreement, due);
        given(userSubscriptionRepository.findActiveByUser(user, due)).willReturn(Optional.of(userSubscription));
        given(paymentOrderRepository.findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                eq(agreement), eq(PaymentPurpose.RENEWAL), anyCollection())).willReturn(Optional.empty());
        given(paymentOrderRepository.save(any(PaymentOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.success(
                        "tx_renewal",
                        "CARD",
                        "1234",
                        "{\"paymentKey\":\"pay_renewal\"}"));
        given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.attempted()).isEqualTo(1);
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(userSubscription.getStartedAt()).isEqualTo(due);
        assertThat(userSubscription.getExpiresAt()).isEqualTo(due.plusMonths(1));
        assertThat(agreement.getNextBillingAt()).isEqualTo(due.plusMonths(1));
        assertThat(agreement.getFailureCount()).isZero();

        ArgumentCaptor<BillingChargeCommand> chargeCaptor = ArgumentCaptor.forClass(BillingChargeCommand.class);
        verify(recurringPaymentProvider).charge(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().idempotencyKey()).contains("attempt-1");
        verify(subscriptionPaymentRepository).save(any(SubscriptionPayment.class));
        verify(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                eq("{\"paymentKey\":\"pay_renewal\"}"));
    }

    @Test
    @DisplayName("pending billing cycle is used at renewal after an immediate upgrade")
    void processDueRenewals_usesPendingBillingCycle() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Premium");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, due);
        userSubscription.schedulePendingChange(subscription, BillingCycle.YEARLY);
        BillingAgreement agreement = buildActiveAgreement(user, due);

        givenDueAgreement(agreement, due);
        given(userSubscriptionRepository.findActiveByUser(user, due)).willReturn(Optional.of(userSubscription));
        given(paymentOrderRepository.findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                eq(agreement), eq(PaymentPurpose.RENEWAL), anyCollection())).willReturn(Optional.empty());
        given(paymentOrderRepository.save(any(PaymentOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.success(
                        "tx_renewal",
                        "CARD",
                        "1234",
                        "{\"paymentKey\":\"pay_renewal\"}"));
        given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(userSubscription.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(userSubscription.getStartedAt()).isEqualTo(due);
        assertThat(userSubscription.getExpiresAt()).isEqualTo(due.plusYears(1));
        assertThat(userSubscription.getPendingSubscription()).isNull();
        assertThat(userSubscription.getPendingBillingCycle()).isNull();
        assertThat(agreement.getNextBillingAt()).isEqualTo(due.plusYears(1));

        ArgumentCaptor<BillingChargeCommand> chargeCaptor = ArgumentCaptor.forClass(BillingChargeCommand.class);
        verify(recurringPaymentProvider).charge(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(99000));
    }

    @Test
    @DisplayName("duplicate scheduler run skips an already done renewal order")
    void processDueRenewals_duplicateDoneOrder() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, due);
        BillingAgreement agreement = buildActiveAgreement(user, due);
        PaymentOrder doneOrder = buildRenewalOrder(user, subscription, userSubscription, agreement, due);
        doneOrder.markDone("tx_renewal", userSubscription, "{}");

        givenDueAgreement(agreement, due);
        given(userSubscriptionRepository.findActiveByUser(user, due)).willReturn(Optional.of(userSubscription));
        given(paymentOrderRepository.findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                eq(agreement), eq(PaymentPurpose.RENEWAL), anyCollection())).willReturn(Optional.of(doneOrder));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.skipped()).isEqualTo(1);
        verify(recurringPaymentProvider, never()).charge(any());
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("transient failure schedules retry inside 3-day grace")
    void processDueRenewals_transientFailure() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, due);
        BillingAgreement agreement = buildActiveAgreement(user, due);

        givenDueAgreement(agreement, due);
        given(userSubscriptionRepository.findActiveByUser(user, due)).willReturn(Optional.of(userSubscription));
        given(paymentOrderRepository.findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                eq(agreement), eq(PaymentPurpose.RENEWAL), anyCollection())).willReturn(Optional.empty());
        given(paymentOrderRepository.save(any(PaymentOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.failure("DECLINED", "Renewal charge failed."));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(agreement.getFailureCount()).isEqualTo(1);
        assertThat(agreement.getNextBillingAt()).isEqualTo(due.plusDays(1));
        assertThat(userSubscription.getExpiresAt()).isEqualTo(due.plusDays(3));
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("third failure suspends renewal but keeps grace access until grace end")
    void processDueRenewals_finalFailureBeforeGraceEnd() {
        LocalDate originalDue = LocalDate.of(2026, 5, 17);
        LocalDate retryDay = originalDue.plusDays(2);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, originalDue.plusDays(3));
        BillingAgreement agreement = buildActiveAgreement(user, originalDue);
        agreement.recordFailedCharge(originalDue.plusDays(1));
        agreement.recordFailedCharge(retryDay);
        PaymentOrder failedOrder = buildRenewalOrder(user, subscription, userSubscription, agreement, originalDue);
        failedOrder.markFailed("DECLINED", "Previous failure.");

        givenDueAgreement(agreement, retryDay);
        given(userSubscriptionRepository.findActiveByUser(user, retryDay)).willReturn(Optional.of(userSubscription));
        given(paymentOrderRepository.findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                eq(agreement), eq(PaymentPurpose.RENEWAL), anyCollection())).willReturn(Optional.of(failedOrder));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.failure("DECLINED", "Renewal charge failed."));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(retryDay);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.SUSPENDED);
        assertThat(agreement.getFailureCount()).isEqualTo(3);
        assertThat(userSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(userSubscription.getExpiresAt()).isEqualTo(originalDue.plusDays(3));
    }

    @Test
    @DisplayName("cancelled agreements are skipped even if included by mistake")
    void processDueRenewals_cancelledAgreementSkipped() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        BillingAgreement agreement = buildActiveAgreement(user, due);
        agreement.cancel();

        givenDueAgreement(agreement, due);

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.skipped()).isEqualTo(1);
        verify(recurringPaymentProvider, never()).charge(any());
    }

    @Test
    @DisplayName("a due candidate withdrawn before locked reload is never charged")
    void processDueRenewals_deletedUserNeverCharges() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        BillingAgreement agreement = buildActiveAgreement(user, due);
        user.withdraw();

        givenDueAgreement(agreement, due);

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        verify(billingKeyCrypto, never()).decrypt(any());
        verify(userSubscriptionRepository, never()).findActiveByUser(any(), any());
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
        verify(recurringPaymentProvider, never()).charge(any());
        verify(billingAgreementRepository).findByIDForRenewal(agreement.getId());
    }

    @Test
    @DisplayName("cancelled grace-period subscriptions are not renewed even if agreement is active")
    void processDueRenewals_cancelledSubscriptionSkipped() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription userSubscription = buildUserSubscription(100L, user, subscription, due);
        userSubscription.cancel();
        BillingAgreement agreement = buildActiveAgreement(user, due);

        givenDueAgreement(agreement, due);
        given(userSubscriptionRepository.findActiveByUser(user, due)).willReturn(Optional.of(userSubscription));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        verify(recurringPaymentProvider, never()).charge(any());
    }

    @Test
    @DisplayName("no due agreements means no renewal attempt")
    void processDueRenewals_dueDateBoundary() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE, today)).willReturn(List.of());

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.attempted()).isZero();
        assertThat(result.skipped()).isZero();
        verify(recurringPaymentProvider, never()).charge(any());
    }

    @Test
    @DisplayName("missing active subscription after grace suspends agreement and expires old row")
    void processDueRenewals_missingActiveAfterGrace() {
        LocalDate due = LocalDate.of(2026, 5, 17);
        LocalDate afterGrace = due.plusDays(4);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L, "Basic");
        UserSubscription expiredSubscription = buildUserSubscription(100L, user, subscription, due.plusDays(3));
        BillingAgreement agreement = buildActiveAgreement(user, due);

        givenDueAgreement(agreement, afterGrace);
        given(userSubscriptionRepository.findActiveByUser(user, afterGrace)).willReturn(Optional.empty());
        given(userSubscriptionRepository.findByUser(user)).willReturn(Optional.of(expiredSubscription));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(afterGrace);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.SUSPENDED);
        assertThat(expiredSubscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        verify(recurringPaymentProvider, never()).charge(any());
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

    private Subscription buildSubscription(Long id, String name) {
        Subscription subscription = Subscription.builder()
                .name(name)
                .description("Test plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build();
        ReflectionTestUtils.setField(subscription, "id", id);
        return subscription;
    }

    private UserSubscription buildUserSubscription(
            Long id,
            User user,
            Subscription subscription,
            LocalDate expiresAt) {
        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(expiresAt.minusMonths(1))
                .expiresAt(expiresAt)
                .build();
        ReflectionTestUtils.setField(userSubscription, "id", id);
        return userSubscription;
    }

    private BillingAgreement buildActiveAgreement(User user, LocalDate nextBillingAt) {
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_customer_1")
                .build();
        ReflectionTestUtils.setField(agreement, "id", user.getId());
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", nextBillingAt);
        return agreement;
    }

    private void givenDueAgreement(BillingAgreement agreement, LocalDate dueDate) {
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                dueDate)).willReturn(List.of(agreement.getId()));
        given(billingAgreementRepository.findByIDForRenewal(agreement.getId()))
                .willReturn(Optional.of(agreement));
    }

    private PaymentOrder buildRenewalOrder(
            User user,
            Subscription subscription,
            UserSubscription userSubscription,
            BillingAgreement agreement,
            LocalDate periodStart) {
        return PaymentOrder.builder()
                .orderId("ATS-REN-20260517-1")
                .user(user)
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(periodStart.plusDays(3).atTime(LocalTime.MAX))
                .build();
    }
}
