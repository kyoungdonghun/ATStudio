package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.BillingAgreement;
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
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommandTransactionService renewal cancellation fences")
class PaymentCommandTransactionFenceTest {

    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock CompanyCertificationRepository companyCertificationRepository;
    @Mock PlaylistService playlistService;
    @Mock PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @Mock PaymentReconciliationIncidentService incidentService;
    @Mock PaymentCommandKeyFactory keyFactory;

    PaymentCommandTransactionService service;

    @BeforeEach
    void setUp() {
        service = new PaymentCommandTransactionService(
                billingAgreementRepository,
                paymentOrderRepository,
                userSubscriptionRepository,
                subscriptionRepository,
                subscriptionPaymentRepository,
                companyCertificationRepository,
                playlistService,
                paymentReceiptEvidenceService,
                incidentService,
                keyFactory);
    }

    @Test
    @DisplayName("provider authorization rejects a cancelled subscription before key decryption or charge")
    void authorizeRenewalProviderCall_rejectsCancelledSubscription() {
        Fixture fixture = fixture();
        fixture.subscription().cancel();
        stubLockedCommand(fixture);
        given(keyFactory.renewal(11L, 13L, fixture.agreement().getNextBillingAt()))
                .willReturn("renewal-command-11");

        assertThatThrownBy(() -> service.authorizeRenewalProviderCall(11L, "ORDER-11"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));
    }

    @Test
    @DisplayName("provider success evidence is retained but deleted users cannot proceed to renewal finalization")
    void recordProviderSuccess_deletedUserRetainsEvidenceAndRejectsFinalization() {
        Fixture fixture = fixture();
        fixture.user().withdraw();
        stubLockedCommand(fixture);

        assertThatThrownBy(() -> service.recordProviderSuccess(
                11L,
                "ORDER-11",
                "provider-payment-key-11",
                "{}",
                null,
                null))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(fixture.order().getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(fixture.order().getPgTransactionId()).isEqualTo("provider-payment-key-11");
    }

    @Test
    @DisplayName("renewal finalization cannot reactivate a cancelled subscription")
    void finalizeRenewal_rejectsCancelledSubscription() {
        Fixture fixture = fixture();
        fixture.subscription().cancel();
        fixture.order().markProviderSucceeded("provider-payment-key-11", "{}");
        PaymentOrderRepository.CommandLockProjection projection =
                org.mockito.Mockito.mock(PaymentOrderRepository.CommandLockProjection.class);
        given(projection.getBillingAgreementID()).willReturn(11L);
        given(projection.getUserSubscriptionID()).willReturn(13L);
        given(projection.getUserID()).willReturn(7L);
        given(projection.getPurpose()).willReturn(PaymentPurpose.RENEWAL);
        given(paymentOrderRepository.findCommandLockProjectionByOrderId("ORDER-11"))
                .willReturn(Optional.of(projection));
        given(billingAgreementRepository.findByIDForUpdate(11L))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(13L))
                .willReturn(Optional.of(fixture.subscription()));
        given(paymentOrderRepository.findByOrderIdForUpdate("ORDER-11"))
                .willReturn(Optional.of(fixture.order()));

        assertThatThrownBy(() -> service.finalizeRenewal(11L, "ORDER-11"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(fixture.subscription().getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        verify(subscriptionPaymentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void stubLockedCommand(Fixture fixture) {
        given(billingAgreementRepository.findByIDForUpdate(11L))
                .willReturn(Optional.of(fixture.agreement()));
        given(paymentOrderRepository.findByOrderIdForUpdate("ORDER-11"))
                .willReturn(Optional.of(fixture.order()));
    }

    private Fixture fixture() {
        LocalDate periodStart = LocalDate.of(2026, 7, 16);
        User user = User.builder()
                .email("renewal@test.com")
                .nickname("renewal-user")
                .password("encoded")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);
        Subscription plan = Subscription.builder()
                .name("Basic")
                .description("Basic plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build();
        ReflectionTestUtils.setField(plan, "id", 17L);
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscription(plan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(periodStart.minusMonths(1))
                .expiresAt(periodStart)
                .build();
        ReflectionTestUtils.setField(subscription, "id", 13L);
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("customer-11")
                .build();
        ReflectionTestUtils.setField(agreement, "id", 11L);
        agreement.activate("ciphertext", "fingerprint", "CARD", "****1234", periodStart);
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-11")
                .commandKey("renewal-command-11")
                .user(user)
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS)
                .subscription(plan)
                .userSubscription(subscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .billingPeriodStart(periodStart)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(LocalDateTime.of(2026, 7, 16, 0, 10))
                .build();
        ReflectionTestUtils.setField(order, "id", 19L);
        order.claimProviderAttempt(
                "renewal-command-11",
                "renewal-attempt-11",
                LocalDateTime.of(2026, 7, 16, 0, 0));
        return new Fixture(user, agreement, subscription, order);
    }

    private record Fixture(
            User user,
            BillingAgreement agreement,
            UserSubscription subscription,
            PaymentOrder order) {
    }
}
