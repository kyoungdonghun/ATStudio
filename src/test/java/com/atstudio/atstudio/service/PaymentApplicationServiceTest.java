package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.PaymentCancelRequest;
import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.dto.payment.PaymentConfirmResponse;
import com.atstudio.atstudio.dto.payment.PaymentPrepareRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.MockPaymentProvider;
import com.atstudio.atstudio.service.payment.provider.TossPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApplicationService unit tests")
class PaymentApplicationServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock UserRepository userRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock CompanyCertificationRepository companyCertificationRepository;
    @Mock PlaylistService playlistService;

    PaymentApplicationService service;
    PaymentProperties paymentProperties;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        service = new PaymentApplicationService(
                paymentOrderRepository,
                userRepository,
                subscriptionRepository,
                userSubscriptionRepository,
                subscriptionPaymentRepository,
                companyCertificationRepository,
                playlistService,
                paymentProperties,
                List.of(new MockPaymentProvider(), new TossPaymentProvider(paymentProperties))
        );
    }

    @Test
    @DisplayName("one-time SUBSCRIBE prepare is blocked for subscription scope")
    void prepareSubscribe_blocked() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> service.prepareSubscriptionPayment(
                buildUserDetails(1L),
                new PaymentPrepareRequest(PaymentPurpose.SUBSCRIBE, 10L, BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    @DisplayName("one-time UPGRADE prepare is blocked for subscription scope")
    void prepareUpgrade_blocked() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> service.prepareSubscriptionPayment(
                buildUserDetails(1L),
                new PaymentPrepareRequest(PaymentPurpose.UPGRADE, 10L, BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    @DisplayName("legacy one-time SUBSCRIBE confirm is rejected before subscription mutation")
    void confirmSubscribe_rejected() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        Subscription subscription = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
        PaymentOrder order = buildOrder(user, subscription, PaymentPurpose.SUBSCRIBE, null,
                BigDecimal.valueOf(9900));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmPayment(
                buildUserDetails(1L),
                new PaymentConfirmRequest("ORDER-1", BigDecimal.valueOf(9900),
                        PaymentProviderType.MOCK, "mock-ORDER-1")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.READY);
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("legacy one-time UPGRADE confirm is rejected before subscription mutation")
    void confirmUpgrade_rejected() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        Subscription subscription = buildSubscription(10L, "Premium", UserType.INDIVIDUAL);
        UserSubscription current = buildUserSubscription(100L, user, subscription,
                BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
        PaymentOrder order = buildOrder(user, subscription, PaymentPurpose.UPGRADE, current,
                BigDecimal.valueOf(5000));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmPayment(
                buildUserDetails(1L),
                new PaymentConfirmRequest("ORDER-1", BigDecimal.valueOf(5000),
                        PaymentProviderType.MOCK, "mock-ORDER-1")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.READY);
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("DONE order remains idempotent for legacy records")
    void confirmDone_isIdempotent() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        Subscription subscription = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
        UserSubscription saved = buildUserSubscription(100L, user, subscription,
                BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
        PaymentOrder order = buildOrder(user, subscription, PaymentPurpose.SUBSCRIBE, saved,
                BigDecimal.valueOf(9900));
        order.markDone("MOCK-ORDER-1", saved, "mockConfirmed=true");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        PaymentConfirmResponse response = service.confirmPayment(
                buildUserDetails(1L),
                new PaymentConfirmRequest("ORDER-1", BigDecimal.valueOf(9900),
                        PaymentProviderType.MOCK, "mock-ORDER-1"));

        assertThat(response.status()).isEqualTo(PaymentOrderStatus.DONE);
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("cancel can close a READY legacy order as CANCELLED")
    void cancelPayment_cancelled() {
        User user = buildUser(1L, UserType.INDIVIDUAL);
        Subscription subscription = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
        PaymentOrder order = buildOrder(user, subscription, PaymentPurpose.SUBSCRIBE, null,
                BigDecimal.valueOf(9900));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        service.cancelPayment(buildUserDetails(1L),
                new PaymentCancelRequest("ORDER-1", PaymentOrderStatus.CANCELLED, "user cancelled"));

        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.CANCELLED);
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
    }

    private User buildUser(Long id, UserType userType) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("user" + id)
                .password("pw")
                .userType(userType)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Subscription buildSubscription(Long id, String name, UserType userType) {
        Subscription subscription = Subscription.builder()
                .name(name)
                .description("Test plan")
                .userType(userType)
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
            BillingCycle billingCycle,
            SubscriptionStatus status) {
        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(billingCycle)
                .status(status)
                .startedAt(java.time.LocalDate.now())
                .expiresAt(java.time.LocalDate.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(userSubscription, "id", id);
        return userSubscription;
    }

    private PaymentOrder buildOrder(
            User user,
            Subscription subscription,
            PaymentPurpose purpose,
            UserSubscription userSubscription,
            BigDecimal amount) {
        return PaymentOrder.builder()
                .orderId("ORDER-1")
                .user(user)
                .purpose(purpose)
                .provider(PaymentProviderType.MOCK)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(amount)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}
