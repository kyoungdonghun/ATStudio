package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
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
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGenerator;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareResult;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingAgreementApplicationService unit tests")
class BillingAgreementApplicationServiceTest {

    @Mock UserRepository userRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock CompanyCertificationRepository companyCertificationRepository;
    @Mock PlaylistService playlistService;
    @Mock BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    BillingAgreementApplicationService service;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        service = new BillingAgreementApplicationService(
                userRepository,
                subscriptionRepository,
                userSubscriptionRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                billingAgreementRepository,
                companyCertificationRepository,
                playlistService,
                billingCustomerKeyGenerator,
                billingKeyCrypto,
                List.of(recurringPaymentProvider)
        );
    }

    @Test
    @DisplayName("prepare creates READY billing agreement and TOSS_BILLING payment order")
    void prepareBillingAgreement_success() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(subscriptionRepository.findById(10L)).willReturn(Optional.of(subscription));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.empty());
        given(billingCustomerKeyGenerator.generate()).willReturn("ats_billing_customer_1");
        given(billingAgreementRepository.save(any(BillingAgreement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentOrderRepository.existsByOrderId(anyString())).willReturn(false);
        given(paymentOrderRepository.save(any(PaymentOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(recurringPaymentProvider.prepareAgreement(any()))
                .willReturn(new BillingAgreementPrepareResult(
                        PaymentProviderType.TOSS_BILLING,
                        "TOSS_BILLING_AUTH",
                        "{\"phase\":\"prepare\"}",
                        Map.of(
                                "clientKey", "test_ck",
                                "customerKey", "ats_billing_customer_1",
                                "successUrl", "http://localhost/success",
                                "failUrl", "http://localhost/fail",
                                "method", "CARD"
                        )));

        BillingAgreementPrepareResponse response = service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(10L, BillingCycle.MONTHLY));

        assertThat(response.provider()).isEqualTo(PaymentProviderType.TOSS_BILLING);
        assertThat(response.agreementStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(9900));
        assertThat(response.checkout().customerKey()).isEqualTo("ats_billing_customer_1");
        assertThat(response.checkout().method()).isEqualTo("CARD");
        verify(paymentOrderRepository).save(any(PaymentOrder.class));
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
    }

    @Test
    @DisplayName("prepare creates zero-amount billing agreement order for active subscription re-registration")
    void prepareBillingAgreement_activeSubscriptionReRegistration() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        UserSubscription activeSubscription = buildUserSubscription(
                100L,
                user,
                subscription,
                SubscriptionStatus.ACTIVE);
        BillingAgreement agreement = buildReadyAgreement(user);
        agreement.expire();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(subscriptionRepository.findById(10L)).willReturn(Optional.of(subscription));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(activeSubscription));
        given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(agreement));
        given(paymentOrderRepository.existsByOrderId(anyString())).willReturn(false);
        given(paymentOrderRepository.save(any(PaymentOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(recurringPaymentProvider.prepareAgreement(any()))
                .willReturn(new BillingAgreementPrepareResult(
                        PaymentProviderType.TOSS_BILLING,
                        "TOSS_BILLING_AUTH",
                        "{\"phase\":\"prepare\"}",
                        Map.of(
                                "clientKey", "test_ck",
                                "customerKey", "ats_billing_customer_1",
                                "successUrl", "http://localhost/success",
                                "failUrl", "http://localhost/fail",
                                "method", "CARD"
                        )));

        BillingAgreementPrepareResponse response = service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(10L, BillingCycle.MONTHLY));

        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);

        ArgumentCaptor<PaymentOrder> orderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(paymentOrderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPurpose()).isEqualTo(PaymentPurpose.BILLING_AGREEMENT);
        assertThat(orderCaptor.getValue().getUserSubscription()).isEqualTo(activeSubscription);
    }

    @Test
    @DisplayName("confirm issues billing key, charges immediately, and activates subscription")
    void confirmBillingAgreement_success() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(user);
        PaymentOrder order = buildBillingOrder(user, subscription, agreement);
        UserSubscription saved = buildUserSubscription(100L, user, subscription, SubscriptionStatus.ACTIVE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(recurringPaymentProvider.confirmAgreement(any()))
                .willReturn(BillingAgreementConfirmResult.success(
                        "billing_raw_key",
                        "CARD",
                        "1234",
                        "{\"method\":\"CARD\"}"));
        given(billingKeyCrypto.encrypt("billing_raw_key"))
                .willReturn(new BillingKeyCrypto.ProtectedBillingKey("encrypted-key", "fingerprint"));
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.success(
                        "tx_1",
                        "CARD",
                        "1234",
                        "{\"paymentKey\":\"pay_1\"}"));
        given(userSubscriptionRepository.findByUser(user)).willReturn(Optional.empty());
        given(userSubscriptionRepository.save(any(UserSubscription.class))).willReturn(saved);
        given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BillingAgreementConfirmResponse response = service.confirmBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementConfirmRequest(
                        "ORDER-1",
                        "auth_key",
                        "ats_billing_customer_1",
                        BigDecimal.valueOf(9900)));

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(response.agreementStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(response.subscription().id()).isEqualTo(100L);
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(response.toString()).doesNotContain("billing_raw_key");

        ArgumentCaptor<BillingChargeCommand> chargeCaptor = ArgumentCaptor.forClass(BillingChargeCommand.class);
        verify(recurringPaymentProvider).charge(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().idempotencyKey()).isEqualTo("billing-initial-ORDER-1");
        verify(subscriptionPaymentRepository).save(any(SubscriptionPayment.class));
        verify(playlistService).createDefaultPlaylist(user);
    }

    @Test
    @DisplayName("confirm billing agreement re-registration stores key without an immediate charge")
    void confirmBillingAgreement_reRegistrationOnly() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        UserSubscription activeSubscription = buildUserSubscription(
                100L,
                user,
                subscription,
                SubscriptionStatus.ACTIVE);
        BillingAgreement agreement = buildReadyAgreement(user);
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-REAUTH")
                .user(user)
                .purpose(PaymentPurpose.BILLING_AGREEMENT)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(subscription)
                .userSubscription(activeSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.ZERO)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        order.markInProgress("{\"phase\":\"prepare\"}");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-REAUTH")).willReturn(Optional.of(order));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(activeSubscription));
        given(recurringPaymentProvider.confirmAgreement(any()))
                .willReturn(BillingAgreementConfirmResult.success(
                        "billing_raw_key",
                        "CARD",
                        "1234",
                        "{\"method\":\"CARD\"}"));
        given(billingKeyCrypto.encrypt("billing_raw_key"))
                .willReturn(new BillingKeyCrypto.ProtectedBillingKey("encrypted-key", "fingerprint"));

        BillingAgreementConfirmResponse response = service.confirmBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementConfirmRequest(
                        "ORDER-REAUTH",
                        "auth_key",
                        "ats_billing_customer_1",
                        BigDecimal.ZERO));

        assertThat(response.orderStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(response.agreementStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(response.nextBillingAt()).isEqualTo(activeSubscription.getExpiresAt());
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        verify(recurringPaymentProvider, never()).charge(any());
        verify(subscriptionPaymentRepository, never()).save(any());
        verify(playlistService, never()).createDefaultPlaylist(any());
    }

    @Test
    @DisplayName("initial charge failure leaves subscription inactive")
    void confirmBillingAgreement_chargeFailure() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(user);
        PaymentOrder order = buildBillingOrder(user, subscription, agreement);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(recurringPaymentProvider.confirmAgreement(any()))
                .willReturn(BillingAgreementConfirmResult.success(
                        "billing_raw_key",
                        "CARD",
                        "1234",
                        "{}"));
        given(billingKeyCrypto.encrypt("billing_raw_key"))
                .willReturn(new BillingKeyCrypto.ProtectedBillingKey("encrypted-key", "fingerprint"));
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.failure("DECLINED", "Initial charge failed."));
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.success("{}"));

        assertThatThrownBy(() -> service.confirmBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementConfirmRequest(
                        "ORDER-1",
                        "auth_key",
                        "ats_billing_customer_1",
                        BigDecimal.valueOf(9900))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED));

        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(agreement.getFailureCount()).isEqualTo(1);
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
        verify(subscriptionPaymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("confirm rejects owner mismatch before provider call")
    void confirmBillingAgreement_ownerMismatch() {
        User user = buildUser(1L);
        User otherUser = buildUser(2L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(otherUser);
        PaymentOrder order = buildBillingOrder(otherUser, subscription, agreement);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementConfirmRequest(
                        "ORDER-1",
                        "auth_key",
                        "ats_billing_customer_1",
                        BigDecimal.valueOf(9900))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        verify(recurringPaymentProvider, never()).confirmAgreement(any());
    }

    @Test
    @DisplayName("confirm rejects customerKey mismatch")
    void confirmBillingAgreement_customerKeyMismatch() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(user);
        PaymentOrder order = buildBillingOrder(user, subscription, agreement);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementConfirmRequest(
                        "ORDER-1",
                        "auth_key",
                        "wrong_customer_key",
                        BigDecimal.valueOf(9900))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verify(recurringPaymentProvider, never()).confirmAgreement(any());
    }

    @Test
    @DisplayName("prepare rejects duplicate active billing agreement")
    void prepareBillingAgreement_duplicateActiveAgreement() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(user);
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", LocalDate.now().plusMonths(1));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(subscriptionRepository.findById(10L)).willReturn(Optional.of(subscription));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(agreement));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(10L, BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE));

        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    @DisplayName("cancel deletes provider billing key and preserves paid access as CANCELLED")
    void cancelMyBillingAgreement_success() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        BillingAgreement agreement = buildReadyAgreement(user);
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", LocalDate.now().plusMonths(1));
        UserSubscription subscriptionAccess = buildUserSubscription(
                100L,
                user,
                subscription,
                SubscriptionStatus.ACTIVE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(agreement));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.success("{}"));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(subscriptionAccess));

        BillingAgreementResponse response = service.cancelMyBillingAgreement(buildUserDetails(1L));

        assertThat(response.status()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getBillingKeyFingerprint()).isNull();
        assertThat(agreement.getNextBillingAt()).isNull();
        assertThat(subscriptionAccess.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(response.toString()).doesNotContain("billing_raw_key");
        verify(recurringPaymentProvider).cancelAgreement(any());
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

    private BillingAgreement buildReadyAgreement(User user) {
        return BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_customer_1")
                .build();
    }

    private PaymentOrder buildBillingOrder(User user, Subscription subscription, BillingAgreement agreement) {
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(subscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        order.markInProgress("{\"phase\":\"prepare\"}");
        return order;
    }

    private UserSubscription buildUserSubscription(
            Long id,
            User user,
            Subscription subscription,
            SubscriptionStatus status) {
        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(status)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(userSubscription, "id", id);
        return userSubscription;
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
