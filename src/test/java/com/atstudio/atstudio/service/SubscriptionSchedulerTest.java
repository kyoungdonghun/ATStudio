package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionScheduler unit tests")
class SubscriptionSchedulerTest {

    private static final Clock PAYMENT_CLOCK = Clock.fixed(
            Instant.parse("2026-07-16T00:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock RecurringRenewalService recurringRenewalService;
    @Mock PaymentOrderRepository paymentOrderRepository;

    @Test
    @DisplayName("all subscription payment crons use the configurable Asia/Seoul zone")
    void paymentCronsUseConfiguredZone() throws Exception {
        List<String> methodNames = List.of(
                "processRecurringRenewals",
                "processExpiredPaymentOrders",
                "processExpiredSubscriptions");

        for (String methodName : methodNames) {
            Method method = SubscriptionScheduler.class.getMethod(methodName);
            Scheduled scheduled = method.getAnnotation(Scheduled.class);
            assertThat(scheduled.zone()).isEqualTo("${app.payment.scheduler-zone:Asia/Seoul}");
        }
    }

    @Test
    @DisplayName("processRecurringRenewals delegates to recurring renewal service")
    void processRecurringRenewals_delegates() {
        SubscriptionScheduler scheduler = new SubscriptionScheduler(
                userSubscriptionRepository,
                recurringRenewalService,
                paymentOrderRepository,
                PAYMENT_CLOCK);
        given(recurringRenewalService.processDueRenewals())
                .willReturn(new RecurringRenewalService.RenewalRunResult(0, 0, 0, 0));

        scheduler.processRecurringRenewals();

        verify(recurringRenewalService).processDueRenewals();
    }

    @Test
    @DisplayName("processExpiredPaymentOrders expires stale READY and IN_PROGRESS orders")
    void processExpiredPaymentOrders_expiresStaleOrders() {
        SubscriptionScheduler scheduler = new SubscriptionScheduler(
                userSubscriptionRepository,
                recurringRenewalService,
                paymentOrderRepository,
                PAYMENT_CLOCK);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        PaymentOrder ready = buildOrder("ATS-READY", user, subscription);
        PaymentOrder inProgress = buildOrder("ATS-IN-PROGRESS", user, subscription);
        inProgress.markInProgress("{}");
        given(paymentOrderRepository.findByStatusInAndExpiresAtBefore(
                any(),
                any(LocalDateTime.class)))
                .willReturn(List.of(ready, inProgress));

        scheduler.processExpiredPaymentOrders();

        assertThat(ready.getStatus()).isEqualTo(PaymentOrderStatus.EXPIRED);
        assertThat(inProgress.getStatus()).isEqualTo(PaymentOrderStatus.EXPIRED);
    }

    @Test
    @DisplayName("processExpiredSubscriptions still expires subscriptions after renewal window")
    void processExpiredSubscriptions_expires() {
        SubscriptionScheduler scheduler = new SubscriptionScheduler(
                userSubscriptionRepository,
                recurringRenewalService,
                paymentOrderRepository,
                PAYMENT_CLOCK);
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        UserSubscription expired = UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusMonths(1))
                .expiresAt(LocalDate.now().minusDays(1))
                .build();
        given(userSubscriptionRepository.findExpired(any(LocalDate.class))).willReturn(List.of(expired));

        scheduler.processExpiredSubscriptions();

        assertThat(expired.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    @DisplayName("processExpiredSubscriptions expires pending changes without applying them for free")
    void processExpiredSubscriptions_expiresPendingWithoutApplying() {
        SubscriptionScheduler scheduler = new SubscriptionScheduler(
                userSubscriptionRepository,
                recurringRenewalService,
                paymentOrderRepository,
                PAYMENT_CLOCK);
        User user = buildUser(1L);
        Subscription currentPlan = buildSubscription(10L);
        Subscription pendingPlan = buildSubscription(20L);
        UserSubscription expired = UserSubscription.builder()
                .user(user)
                .subscription(currentPlan)
                .billingCycle(BillingCycle.YEARLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusYears(1))
                .expiresAt(LocalDate.now().minusDays(1))
                .build();
        expired.schedulePendingChange(pendingPlan, BillingCycle.MONTHLY);
        given(userSubscriptionRepository.findExpired(any(LocalDate.class))).willReturn(List.of(expired));

        scheduler.processExpiredSubscriptions();

        assertThat(expired.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(expired.getSubscription()).isEqualTo(currentPlan);
        assertThat(expired.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(expired.getPendingSubscription()).isNull();
        assertThat(expired.getPendingBillingCycle()).isNull();
    }

    @Test
    @DisplayName("expiration jobs use the injected non-default business-zone date")
    void processExpiredSubscriptions_usesInjectedBusinessZone() {
        Clock losAngelesClock = Clock.fixed(
                Instant.parse("2026-07-16T06:59:59Z"),
                ZoneId.of("America/Los_Angeles"));
        SubscriptionScheduler scheduler = new SubscriptionScheduler(
                userSubscriptionRepository,
                recurringRenewalService,
                paymentOrderRepository,
                losAngelesClock);
        given(userSubscriptionRepository.findExpired(LocalDate.of(2026, 7, 15)))
                .willReturn(List.of());

        scheduler.processExpiredSubscriptions();

        verify(userSubscriptionRepository).findExpired(LocalDate.of(2026, 7, 15));
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

    private Subscription buildSubscription(Long id) {
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
        ReflectionTestUtils.setField(subscription, "id", id);
        return subscription;
    }

    private PaymentOrder buildOrder(String orderId, User user, Subscription subscription) {
        return PaymentOrder.builder()
                .orderId(orderId)
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }
}
