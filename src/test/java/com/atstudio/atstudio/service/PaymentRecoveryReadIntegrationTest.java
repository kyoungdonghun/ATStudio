package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.PaymentCommandOutcomeResponse;
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
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentCommandKeyFactory.class,
        PaymentRecoveryReadService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
class PaymentRecoveryReadIntegrationTest {

    @Autowired PaymentRecoveryReadService service;
    @Autowired PaymentCommandKeyFactory keyFactory;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired BillingAgreementCommandIntegrationTestSupport.TestRecurringPaymentProvider provider;

    private User owner;
    private User foreignUser;
    private Subscription currentPlan;
    private Subscription targetPlan;
    private UserSubscription current;

    @BeforeEach
    void setUp() {
        provider.reset();
        owner = userRepository.save(user("recovery-owner", "recovery-owner@test.com"));
        foreignUser = userRepository.save(user("recovery-foreign", "recovery-foreign@test.com"));
        currentPlan = subscriptionRepository.save(plan("Current", 9900));
        targetPlan = subscriptionRepository.save(plan("Target", 19900));
        current = userSubscriptionRepository.save(UserSubscription.builder()
                .user(owner)
                .subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(10))
                .expiresAt(LocalDate.now().plusDays(20))
                .build());
    }

    @Test
    void callbackReadIsOwnerScopedAndDoesNotInvokeProvider() {
        PaymentOrder order = persistOrder(
                "CALLBACK-ORDER",
                "BILLING_CONFIRM:CALLBACK-ORDER",
                owner,
                currentPlan,
                PaymentPurpose.SUBSCRIBE,
                BillingCycle.MONTHLY,
                null);
        order.markDone("provider-tx", current, "{}");
        paymentOrderRepository.saveAndFlush(order);

        PaymentCommandOutcomeResponse response =
                service.getCallbackOutcome(principal(owner), "CALLBACK-ORDER");

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(response.userSubscriptionId()).isEqualTo(current.getId());
        assertThatThrownBy(() -> service.getCallbackOutcome(principal(foreignUser), "CALLBACK-ORDER"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
        assertThat(provider.calls()).isEmpty();
    }

    @Test
    void upgradeReadUsesExactCurrentPeriodTargetKeyInsteadOfLatestOrder() {
        String exactKey = keyFactory.upgrade(
                current.getId(),
                current.getStartedAt(),
                current.getExpiresAt(),
                targetPlan.getId(),
                BillingCycle.YEARLY);
        PaymentOrder exact = persistOrder(
                "UPGRADE-EXACT",
                exactKey,
                owner,
                targetPlan,
                PaymentPurpose.UPGRADE,
                BillingCycle.MONTHLY,
                BillingCycle.YEARLY);
        exact.markDone("provider-upgrade-tx", current, "{}");
        paymentOrderRepository.saveAndFlush(exact);

        Subscription decoyPlan = subscriptionRepository.save(plan("Decoy", 29900));
        paymentOrderRepository.saveAndFlush(persistOrder(
                "UPGRADE-LATEST-DECOY",
                keyFactory.upgrade(
                        current.getId(),
                        current.getStartedAt(),
                        current.getExpiresAt(),
                        decoyPlan.getId(),
                        BillingCycle.MONTHLY),
                owner,
                decoyPlan,
                PaymentPurpose.UPGRADE,
                BillingCycle.MONTHLY,
                BillingCycle.MONTHLY));

        PaymentCommandOutcomeResponse response = service.getUpgradeOutcome(
                principal(owner),
                targetPlan.getId(),
                BillingCycle.YEARLY);

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(response.userSubscriptionId()).isEqualTo(current.getId());
        assertThat(response.targetSubscriptionId()).isEqualTo(targetPlan.getId());
        assertThat(response.targetBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(provider.calls()).isEmpty();
    }

    private PaymentOrder persistOrder(
            String orderID,
            String commandKey,
            User user,
            Subscription subscription,
            PaymentPurpose purpose,
            BillingCycle billingCycle,
            BillingCycle targetBillingCycle) {
        return paymentOrderRepository.save(PaymentOrder.builder()
                .orderId(orderID)
                .commandKey(commandKey)
                .user(user)
                .purpose(purpose)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentOrderStatus.PROCESSING)
                .subscription(subscription)
                .userSubscription(current)
                .billingCycle(billingCycle)
                .upgradeTargetBillingCycle(targetBillingCycle)
                .amount(BigDecimal.valueOf(5000))
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build());
    }

    private User user(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
    }

    private Subscription plan(String name, long monthlyPrice) {
        return Subscription.builder()
                .name(name)
                .description(name)
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(monthlyPrice))
                .priceYearly(BigDecimal.valueOf(monthlyPrice * 10))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build();
    }

    private CustomUserDetails principal(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}
