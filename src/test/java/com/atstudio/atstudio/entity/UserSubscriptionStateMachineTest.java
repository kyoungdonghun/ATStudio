package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserSubscription entitlement state machine")
class UserSubscriptionStateMachineTest {

    @Test
    @DisplayName("upgrade keeping the period only schedules a real billing-cycle change")
    void upgradeKeepingPeriod_schedulesOnlyDifferentCycle() {
        Subscription standard = plan("STANDARD");
        Subscription premium = plan("PREMIUM");
        UserSubscription subscription = active(standard, BillingCycle.MONTHLY);

        subscription.upgradeKeepingPeriod(premium, null);
        assertThat(subscription.getSubscription()).isSameAs(premium);
        assertThat(subscription.hasPending()).isFalse();

        subscription.upgradeKeepingPeriod(standard, BillingCycle.MONTHLY);
        assertThat(subscription.hasPending()).isFalse();

        subscription.upgradeKeepingPeriod(premium, BillingCycle.YEARLY);
        assertThat(subscription.getPendingSubscription()).isSameAs(premium);
        assertThat(subscription.getPendingBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(subscription.hasPending()).isTrue();
    }

    @Test
    @DisplayName("pending application is inert without a plan and renews monthly or yearly as requested")
    void applyPending_appliesCycleSpecificPeriod() {
        UserSubscription noPending = active(plan("STANDARD"), BillingCycle.MONTHLY);
        LocalDate originalExpiry = noPending.getExpiresAt();
        noPending.applyPending();
        assertThat(noPending.getExpiresAt()).isEqualTo(originalExpiry);

        UserSubscription yearly = active(plan("STANDARD"), BillingCycle.MONTHLY);
        yearly.schedulePendingChange(plan("PREMIUM"), BillingCycle.YEARLY);
        yearly.cancel();
        yearly.applyPending();
        assertThat(yearly.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(yearly.getExpiresAt()).isEqualTo(yearly.getStartedAt().plusYears(1));
        assertThat(yearly.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(yearly.hasPending()).isFalse();

        UserSubscription monthly = active(plan("STANDARD"), BillingCycle.YEARLY);
        monthly.schedulePendingChange(plan("DELUXE"), BillingCycle.MONTHLY);
        monthly.applyPending();
        assertThat(monthly.getExpiresAt()).isEqualTo(monthly.getStartedAt().plusMonths(1));
    }

    @Test
    @DisplayName("cancelled subscriptions reactivate while expired subscriptions remain terminal")
    void reactivate_respectsExpiredTerminalState() {
        UserSubscription cancelled = active(plan("STANDARD"), BillingCycle.MONTHLY);
        cancelled.cancel();
        cancelled.reactivate();
        assertThat(cancelled.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        UserSubscription expired = active(plan("STANDARD"), BillingCycle.MONTHLY);
        expired.schedulePendingChange(plan("PREMIUM"), BillingCycle.YEARLY);
        expired.expire();
        expired.reactivate();
        assertThat(expired.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(expired.hasPending()).isFalse();
    }

    @Test
    @DisplayName("admin patch updates only supplied fields")
    void adminUpdate_isPatchSemantic() {
        UserSubscription subscription = active(plan("STANDARD"), BillingCycle.MONTHLY);
        LocalDate originalExpiry = subscription.getExpiresAt();
        subscription.adminUpdate(null, null, null);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(subscription.getExpiresAt()).isEqualTo(originalExpiry);

        LocalDate changedExpiry = originalExpiry.plusMonths(2);
        subscription.adminUpdate(SubscriptionStatus.CANCELLED, BillingCycle.YEARLY, changedExpiry);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(subscription.getExpiresAt()).isEqualTo(changedExpiry);
    }

    @Test
    @DisplayName("entitlement correction preserves or clears a pending change exactly as approved")
    void applyEntitlementCorrection_honorsClearPendingFlag() {
        UserSubscription subscription = active(plan("STANDARD"), BillingCycle.MONTHLY);
        Subscription pending = plan("PREMIUM");
        subscription.schedulePendingChange(pending, BillingCycle.YEARLY);
        subscription.applyEntitlementCorrection(
                plan("DELUXE"),
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE,
                LocalDate.of(2026, 10, 1),
                false);
        assertThat(subscription.getPendingSubscription()).isSameAs(pending);

        subscription.applyEntitlementCorrection(
                plan("STANDARD"),
                BillingCycle.YEARLY,
                SubscriptionStatus.CANCELLED,
                LocalDate.of(2027, 1, 1),
                true);
        assertThat(subscription.getPendingSubscription()).isNull();
        assertThat(subscription.getPendingBillingCycle()).isNull();
    }

    @Test
    @DisplayName("immediate upgrade and new subscription reset stale pending state")
    void immediateTransitions_clearPendingState() {
        UserSubscription subscription = active(plan("STANDARD"), BillingCycle.MONTHLY);
        subscription.schedulePendingChange(plan("PREMIUM"), BillingCycle.YEARLY);
        subscription.upgrade(plan("DELUXE"), BillingCycle.YEARLY, LocalDate.of(2027, 7, 17));
        assertThat(subscription.hasPending()).isFalse();

        subscription.schedulePendingChange(plan("PREMIUM"), BillingCycle.MONTHLY);
        subscription.startNewSubscription(
                plan("STANDARD"),
                BillingCycle.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1));
        assertThat(subscription.hasPending()).isFalse();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    private UserSubscription active(Subscription plan, BillingCycle cycle) {
        return UserSubscription.builder()
                .user(User.builder().email("subscriber@example.com").nickname("subscriber").build())
                .subscription(plan)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.of(2026, 7, 1))
                .expiresAt(LocalDate.of(2026, 8, 1))
                .build();
    }

    private Subscription plan(String name) {
        return Subscription.builder().name(name).build();
    }
}
