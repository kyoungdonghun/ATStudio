package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("BillingAgreementRepository tests")
class BillingAgreementRepositoryTest {

    @Autowired
    private BillingAgreementRepository billingAgreementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Test
    @DisplayName("find by user/provider and provider/customerKey")
    void findByUserProviderAndCustomerKey() {
        User user = userRepository.save(user("billing-user", "billing@test.com"));
        BillingAgreement saved = billingAgreementRepository.save(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build());

        assertThat(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .contains(saved);
        assertThat(billingAgreementRepository.findByProviderAndProviderCustomerKey(
                PaymentProviderType.TOSS_BILLING,
                "ats_billing_random"))
                .contains(saved);
    }

    @Test
    @DisplayName("find due agreement IDs and lock the selected agreement before renewal")
    void findDueActiveAgreements() throws Exception {
        LocalDate today = LocalDate.of(2026, 5, 17);
        RenewalFixture due = renewalFixture("due", today);
        renewalFixture("future", today.plusDays(1));
        RenewalFixture deleted = renewalFixture("deleted", today);
        deleted.user().withdraw();

        assertThat(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                today.minusDays(3)))
                .containsExactly(due.agreement().getId());
        assertThat(billingAgreementRepository.findByIDForRenewal(due.agreement().getId()))
                .contains(due.agreement());
        Method lockedLookup = BillingAgreementRepository.class.getMethod("findByIDForRenewal", Long.class);
        assertThat(lockedLookup.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("due projection selects only fresh, retry-due, or provider-succeeded exact periods")
    void findExactDueRenewalCandidates() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        RenewalFixture fresh = renewalFixture("candidate-fresh", today);
        RenewalFixture retryDue = renewalFixture("candidate-retry", today);
        RenewalFixture retryLater = renewalFixture("candidate-later", today);
        RenewalFixture pending = renewalFixture("candidate-pending", today);
        RenewalFixture providerSucceeded = renewalFixture("candidate-succeeded", today);

        PaymentOrder retryDueOrder = renewalOrder(retryDue, today, "RETRY-DUE");
        retryDueOrder.claimProviderAttempt(retryDueOrder.getCommandKey(), "attempt-retry-due", LocalDateTime.now());
        retryDueOrder.markFailed("DECLINED", "retry due");
        retryDue.agreement().recordFailedCharge(today);

        PaymentOrder retryLaterOrder = renewalOrder(retryLater, today, "RETRY-LATER");
        retryLaterOrder.claimProviderAttempt(
                retryLaterOrder.getCommandKey(),
                "attempt-retry-later",
                LocalDateTime.now());
        retryLaterOrder.markFailed("DECLINED", "retry later");
        retryLater.agreement().recordFailedCharge(today.plusDays(1));

        PaymentOrder pendingOrder = renewalOrder(pending, today, "PENDING");
        pendingOrder.claimProviderAttempt(pendingOrder.getCommandKey(), "attempt-pending", LocalDateTime.now());
        pendingOrder.markProviderOutcomeUnknown("UNKNOWN", "ambiguous");

        PaymentOrder succeededOrder = renewalOrder(providerSucceeded, today, "SUCCEEDED");
        succeededOrder.claimProviderAttempt(succeededOrder.getCommandKey(), "attempt-succeeded", LocalDateTime.now());
        succeededOrder.markProviderSucceeded("tx-succeeded", "{}");

        paymentOrderRepository.saveAllAndFlush(
                java.util.List.of(retryDueOrder, retryLaterOrder, pendingOrder, succeededOrder));
        billingAgreementRepository.flush();

        assertThat(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                today.minusDays(3)))
                .containsExactlyInAnyOrder(
                        fresh.agreement().getId(),
                        retryDue.agreement().getId(),
                        providerSucceeded.agreement().getId())
                .doesNotContain(retryLater.agreement().getId(), pending.agreement().getId());
    }

    @Test
    @DisplayName("withdrawal cleanup candidates contain only deleted users with cancelled agreements and retained keys")
    void findWithdrawalCleanupCandidates() {
        User targetUser = userRepository.save(user("cleanup-target", "cleanup-target@test.com"));
        targetUser.withdraw();
        BillingAgreement target = activeAgreement(
                targetUser,
                "ats_billing_cleanup_target",
                LocalDate.of(2026, 5, 17));
        target.cancel();

        User activeUser = userRepository.save(user("cleanup-active", "cleanup-active@test.com"));
        BillingAgreement activeUserAgreement = activeAgreement(
                activeUser,
                "ats_billing_cleanup_active",
                LocalDate.of(2026, 5, 17));
        activeUserAgreement.cancel();

        User noKeyUser = userRepository.save(user("cleanup-no-key", "cleanup-no-key@test.com"));
        noKeyUser.withdraw();
        BillingAgreement noKeyAgreement = BillingAgreement.builder()
                .user(noKeyUser)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_cleanup_no_key")
                .build();
        noKeyAgreement.cancel();

        User nonCancelledUser = userRepository.save(user("cleanup-pending", "cleanup-not-cancelled@test.com"));
        nonCancelledUser.withdraw();
        BillingAgreement nonCancelledAgreement = activeAgreement(
                nonCancelledUser,
                "ats_billing_cleanup_not_cancelled",
                LocalDate.of(2026, 5, 17));

        billingAgreementRepository.save(target);
        billingAgreementRepository.save(activeUserAgreement);
        billingAgreementRepository.save(noKeyAgreement);
        billingAgreementRepository.save(nonCancelledAgreement);

        assertThat(billingAgreementRepository.findWithdrawalCleanupCandidateIDs())
                .containsExactly(target.getId());
        assertThat(billingAgreementRepository.findWithdrawalCleanupCandidateIDs(
                0L,
                PageRequest.of(0, 100)))
                .containsExactly(target.getId());
    }

    @Test
    @DisplayName("cleanup projections separate unresolved work from stale processing leases")
    void findCleanupAndStaleLeaseCandidates() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        BillingAgreement stale = cancelledWithdrawalAgreement("cleanup-stale");
        BillingAgreement fresh = cancelledWithdrawalAgreement("cleanup-fresh");
        BillingAgreement failed = cancelledWithdrawalAgreement("cleanup-failed");

        stale.claimBillingKeyCleanup(now.minusMinutes(16));
        fresh.claimBillingKeyCleanup(now.minusMinutes(14));
        failed.claimBillingKeyCleanup(now.minusMinutes(20));
        failed.markBillingKeyCleanupFailed(failed.getBillingKeyCleanupStartedAt());
        billingAgreementRepository.flush();

        assertThat(billingAgreementRepository.findWithdrawalCleanupCandidateIDs())
                .doesNotContain(stale.getId(), fresh.getId(), failed.getId());
        assertThat(billingAgreementRepository.findStaleBillingKeyCleanupCandidateIDs(
                now.minusMinutes(15),
                0L,
                PageRequest.of(0, 100)))
                .containsExactly(stale.getId());
    }

    @Test
    @DisplayName("command finalizer repositories expose canonical pessimistic write locks")
    void commandFinalizerRepositoriesExposeWriteLocks() throws NoSuchMethodException {
        Method orderLock = PaymentOrderRepository.class.getMethod("findByOrderIdForUpdate", String.class);
        Method subscriptionLock = UserSubscriptionRepository.class.getMethod("findByIdForUpdate", Long.class);
        Method paymentOrderLock = SubscriptionPaymentRepository.class.getMethod(
                "findByPaymentOrderForUpdate",
                PaymentOrder.class);
        Method paymentTransactionLock = SubscriptionPaymentRepository.class.getMethod(
                "findByProviderAndPgTransactionIdForUpdate",
                PaymentProviderType.class,
                String.class);

        assertThat(orderLock.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(subscriptionLock.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(paymentOrderLock.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(paymentTransactionLock.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    private RenewalFixture renewalFixture(String label, LocalDate nextBillingAt) {
        String nickname = label.length() > 15 ? label.substring(0, 15) : label;
        User user = userRepository.save(user(nickname, label + "@test.com"));
        Subscription subscription = subscriptionRepository.save(Subscription.builder()
                .name("Plan " + label)
                .description("Renewal repository fixture")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        UserSubscription userSubscription = userSubscriptionRepository.save(UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(nextBillingAt.minusMonths(1))
                .expiresAt(nextBillingAt.plusDays(3))
                .build());
        BillingAgreement agreement = billingAgreementRepository.save(activeAgreement(
                user,
                "ats_billing_" + label,
                nextBillingAt));
        return new RenewalFixture(user, subscription, userSubscription, agreement);
    }

    private PaymentOrder renewalOrder(RenewalFixture fixture, LocalDate periodStart, String suffix) {
        return PaymentOrder.builder()
                .orderId("ORDER-" + suffix)
                .commandKey("RENEWAL:%d:%d:%s".formatted(
                        fixture.agreement().getId(),
                        fixture.userSubscription().getId(),
                        periodStart))
                .user(fixture.user())
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(fixture.subscription())
                .userSubscription(fixture.userSubscription())
                .billingAgreement(fixture.agreement())
                .billingCycle(BillingCycle.MONTHLY)
                .billingPeriodStart(periodStart)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(periodStart.plusDays(3).atTime(23, 59))
                .build();
    }

    private BillingAgreement cancelledWithdrawalAgreement(String label) {
        User user = userRepository.save(user(label, label + "@test.com"));
        user.withdraw();
        BillingAgreement agreement = activeAgreement(
                user,
                "ats_billing_" + label,
                LocalDate.of(2026, 5, 17));
        agreement.cancel();
        return billingAgreementRepository.save(agreement);
    }

    private BillingAgreement activeAgreement(User user, String customerKey, LocalDate nextBillingAt) {
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey(customerKey)
                .build();
        agreement.activate("v1:nonce:ciphertext", "fingerprint-" + customerKey, "카드", "masked", nextBillingAt);
        return agreement;
    }

    private User user(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .userType(UserType.INDIVIDUAL)
                .build();
    }

    private record RenewalFixture(
            User user,
            Subscription subscription,
            UserSubscription userSubscription,
            BillingAgreement agreement) {
    }
}
