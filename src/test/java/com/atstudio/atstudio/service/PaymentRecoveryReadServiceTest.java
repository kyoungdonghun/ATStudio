package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.PaymentCommandOutcomeResponse;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentRecoveryReadServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock PaymentCommandKeyFactory paymentCommandKeyFactory;

    private PaymentRecoveryReadService service;

    @BeforeEach
    void setUp() {
        service = new PaymentRecoveryReadService(
                userRepository,
                userSubscriptionRepository,
                paymentOrderRepository,
                paymentCommandKeyFactory);
    }

    @Test
    void callbackOutcomeUsesExactOwnerScopedOrderRead() {
        CustomUserDetails principal = principal(7L);
        PaymentOrder order = order(
                PaymentPurpose.SUBSCRIBE,
                PaymentOrderStatus.DONE,
                20L,
                BillingCycle.MONTHLY);
        given(paymentOrderRepository.findRecoveryByOrderIdAndUserID("ORDER-1", 7L))
                .willReturn(Optional.of(order));

        PaymentCommandOutcomeResponse response = service.getCallbackOutcome(principal, "ORDER-1");

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(response.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE);
        assertThat(response.userSubscriptionId()).isEqualTo(91L);
        assertThat(response.targetSubscriptionId()).isEqualTo(20L);
        assertThat(response.targetBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        verify(paymentOrderRepository).findRecoveryByOrderIdAndUserID("ORDER-1", 7L);
        verifyNoMoreInteractions(paymentOrderRepository);
    }

    @Test
    void absentAndForeignCallbackOrdersShareTheSameNotFoundResult() {
        CustomUserDetails principal = principal(7L);
        given(paymentOrderRepository.findRecoveryByOrderIdAndUserID("FOREIGN", 7L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCallbackOutcome(principal, "FOREIGN"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
    }

    @Test
    void upgradeOutcomeUsesCurrentPeriodAndRequestedTargetForTheExactCommandKey() {
        CustomUserDetails principal = principal(7L);
        User user = mock(User.class);
        UserSubscription current = mock(UserSubscription.class);
        PaymentOrder order = order(
                PaymentPurpose.UPGRADE,
                PaymentOrderStatus.PROCESSING,
                30L,
                BillingCycle.YEARLY);
        LocalDate startedAt = LocalDate.of(2026, 8, 1);
        LocalDate expiresAt = LocalDate.of(2026, 9, 1);

        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(user, LocalDate.now()))
                .willReturn(Optional.of(current));
        given(current.getId()).willReturn(11L);
        given(current.getStartedAt()).willReturn(startedAt);
        given(current.getExpiresAt()).willReturn(expiresAt);
        given(paymentCommandKeyFactory.upgrade(11L, startedAt, expiresAt, 30L, BillingCycle.YEARLY))
                .willReturn("UPGRADE:EXACT");
        given(paymentOrderRepository.findRecoveryByCommandKeyAndUserID("UPGRADE:EXACT", 7L))
                .willReturn(Optional.of(order));

        PaymentCommandOutcomeResponse response = service.getUpgradeOutcome(
                principal,
                30L,
                BillingCycle.YEARLY);

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
        assertThat(response.userSubscriptionId()).isNull();
        verify(paymentOrderRepository).findRecoveryByCommandKeyAndUserID("UPGRADE:EXACT", 7L);
        verifyNoMoreInteractions(paymentOrderRepository);
    }

    private CustomUserDetails principal(Long id) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getId()).willReturn(id);
        return principal;
    }

    private PaymentOrder order(
            PaymentPurpose purpose,
            PaymentOrderStatus status,
            Long targetSubscriptionId,
            BillingCycle targetBillingCycle) {
        PaymentOrder order = mock(PaymentOrder.class);
        Subscription subscription = mock(Subscription.class);
        given(subscription.getId()).willReturn(targetSubscriptionId);
        given(order.getProvider()).willReturn(PaymentProviderType.TOSS);
        given(order.getPurpose()).willReturn(purpose);
        given(order.getStatus()).willReturn(status);
        given(order.getSubscription()).willReturn(subscription);
        if (status == PaymentOrderStatus.DONE) {
            UserSubscription userSubscription = mock(UserSubscription.class);
            given(userSubscription.getId()).willReturn(91L);
            given(order.getUserSubscription()).willReturn(userSubscription);
        }
        if (purpose == PaymentPurpose.UPGRADE) {
            given(order.getUpgradeTargetBillingCycle()).willReturn(targetBillingCycle);
        } else {
            given(order.getBillingCycle()).willReturn(targetBillingCycle);
        }
        return order;
    }
}
