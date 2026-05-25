package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentEntitlementCorrection;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentEntitlementCorrectionRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentEntitlementCorrectionService unit tests")
class AdminPaymentEntitlementCorrectionServiceTest {

    @Mock PaymentEntitlementCorrectionRepository correctionRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentOperationAuditLogService auditLogService;

    AdminPaymentEntitlementCorrectionService service;

    @BeforeEach
    void setUp() {
        service = new AdminPaymentEntitlementCorrectionService(
                correctionRepository,
                paymentRefundRepository,
                subscriptionRepository,
                userSubscriptionRepository,
                billingAgreementRepository,
                userRepository,
                auditLogService);
    }

    @Test
    @DisplayName("previewCorrection is read-only and shows explicit target state")
    void previewCorrection() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        AdminPaymentEntitlementCorrectionRequest request = request(fixture.standard().getId());
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(fixture.standard().getId())).willReturn(Optional.of(fixture.standard()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(fixture.agreement()));

        var response = service.previewCorrection(request).getData();

        assertThat(response.executable()).isTrue();
        assertThat(response.currentPlanName()).isEqualTo("PREMIUM");
        assertThat(response.targetPlanName()).isEqualTo("STANDARD");
        assertThat(response.targetStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(response.targetBillingAgreementStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        verify(correctionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCorrection stores before and target states before execution")
    void createCorrection() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        AdminPaymentEntitlementCorrectionRequest request = request(fixture.standard().getId());
        User admin = admin();
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(fixture.standard().getId())).willReturn(Optional.of(fixture.standard()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(fixture.agreement()));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(correctionRepository.save(any(PaymentEntitlementCorrection.class)))
                .willAnswer(invocation -> {
                    PaymentEntitlementCorrection correction = invocation.getArgument(0);
                    ReflectionTestUtils.setField(correction, "id", 88L);
                    return correction;
                });

        service.createCorrection(actor(), request);

        ArgumentCaptor<PaymentEntitlementCorrection> captor =
                ArgumentCaptor.forClass(PaymentEntitlementCorrection.class);
        verify(correctionRepository).save(captor.capture());
        PaymentEntitlementCorrection correction = captor.getValue();
        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.REQUESTED);
        assertThat(correction.getBeforeSubscription().getName()).isEqualTo("PREMIUM");
        assertThat(correction.getTargetSubscription().getName()).isEqualTo("STANDARD");
        assertThat(correction.isClearPendingChange()).isTrue();
        assertThat(correction.isCancelBillingAgreement()).isTrue();
        verify(auditLogService).recordPaymentEntitlementCorrectionEvent(
                any(),
                any(PaymentEntitlementCorrection.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.REQUESTED),
                org.mockito.ArgumentMatchers.eq("full refund correction"));
    }

    @Test
    @DisplayName("createCorrection rejects non-succeeded refund records")
    void createCorrectionRejectsPendingRefund() {
        Fixture fixture = fixture(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(fixture.standard().getId())).willReturn(Optional.of(fixture.standard()));

        assertThatThrownBy(() -> service.createCorrection(actor(), request(fixture.standard().getId())))
                .isInstanceOf(BusinessException.class);

        verify(correctionRepository, never()).save(any());
    }

    @Test
    @DisplayName("executeCorrection requires approval")
    void executeCorrectionRequiresApproval() {
        PaymentEntitlementCorrection correction =
                correction(fixture(PaymentRefundStatus.SUCCEEDED), PaymentEntitlementCorrectionStatus.REQUESTED);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("execute")))
                .isInstanceOf(BusinessException.class);

        verify(userSubscriptionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("executeCorrection applies explicit target state and local billing cancel")
    void executeCorrection() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        User admin = admin();
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS_BILLING))
                .willReturn(Optional.of(fixture.agreement()));

        service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("execute"));

        assertThat(fixture.userSubscription().getSubscription().getName()).isEqualTo("STANDARD");
        assertThat(fixture.userSubscription().getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(fixture.userSubscription().getExpiresAt()).isEqualTo(LocalDate.now());
        assertThat(fixture.userSubscription().getPendingSubscription()).isNull();
        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.SUCCEEDED);
        assertThat(correction.getAfterBillingAgreementStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        verify(auditLogService).recordPaymentEntitlementCorrectionEvent(
                any(),
                any(PaymentEntitlementCorrection.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.APPROVED),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.PROCESSING),
                org.mockito.ArgumentMatchers.eq("execute"));
        verify(auditLogService).recordPaymentEntitlementCorrectionEvent(
                any(),
                any(PaymentEntitlementCorrection.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.PROCESSING),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.SUCCEEDED),
                org.mockito.ArgumentMatchers.isNull());
    }

    private AdminPaymentEntitlementCorrectionRequest request(Long targetSubscriptionId) {
        return new AdminPaymentEntitlementCorrectionRequest(
                77L,
                targetSubscriptionId,
                BillingCycle.MONTHLY,
                SubscriptionStatus.EXPIRED,
                LocalDate.now(),
                true,
                true,
                "full refund correction");
    }

    private PaymentEntitlementCorrection correction(
            Fixture fixture,
            PaymentEntitlementCorrectionStatus status) {
        PaymentEntitlementCorrection correction = PaymentEntitlementCorrection.builder()
                .paymentRefund(fixture.refund())
                .subscriptionPayment(fixture.payment())
                .paymentOrder(fixture.order())
                .userSubscription(fixture.userSubscription())
                .user(fixture.user())
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(status)
                .beforeSubscription(fixture.premium())
                .beforeBillingCycle(BillingCycle.YEARLY)
                .beforeStatus(SubscriptionStatus.ACTIVE)
                .beforeExpiresAt(LocalDate.of(2027, 5, 25))
                .beforePendingSubscription(fixture.standard())
                .beforePendingBillingCycle(BillingCycle.MONTHLY)
                .targetSubscription(fixture.standard())
                .targetBillingCycle(BillingCycle.MONTHLY)
                .targetStatus(SubscriptionStatus.EXPIRED)
                .targetExpiresAt(LocalDate.now())
                .clearPendingChange(true)
                .cancelBillingAgreement(true)
                .beforeBillingAgreementStatus(BillingAgreementStatus.ACTIVE)
                .afterBillingAgreementStatus(BillingAgreementStatus.ACTIVE)
                .reasonNote("full refund correction")
                .build();
        ReflectionTestUtils.setField(correction, "id", 88L);
        return correction;
    }

    private Fixture fixture(PaymentRefundStatus refundStatus) {
        User user = User.builder()
                .id(16L)
                .nickname("buyer")
                .email("buyer@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        Subscription standard = Subscription.builder()
                .id(1L)
                .name("STANDARD")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(3)
                .isActive(true)
                .build();
        Subscription premium = Subscription.builder()
                .id(3L)
                .name("PREMIUM")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(29900))
                .priceYearly(BigDecimal.valueOf(299000))
                .downloadPerDay(100)
                .maxWhitelistChannels(10)
                .isActive(true)
                .build();
        UserSubscription userSubscription = UserSubscription.builder()
                .id(20L)
                .user(user)
                .subscription(premium)
                .billingCycle(BillingCycle.YEARLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.of(2026, 5, 25))
                .expiresAt(LocalDate.of(2027, 5, 25))
                .pendingSubscription(standard)
                .pendingBillingCycle(BillingCycle.MONTHLY)
                .build();
        PaymentOrder order = PaymentOrder.builder()
                .id(10L)
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentOrderStatus.DONE)
                .subscription(premium)
                .userSubscription(userSubscription)
                .billingCycle(BillingCycle.YEARLY)
                .amount(BigDecimal.valueOf(299000))
                .pgTransactionId("payment_key")
                .expiresAt(LocalDateTime.of(2026, 5, 25, 10, 0))
                .build();
        SubscriptionPayment payment = SubscriptionPayment.builder()
                .id(30L)
                .user(user)
                .userSubscription(userSubscription)
                .subscription(premium)
                .paymentOrder(order)
                .billingCycle(BillingCycle.YEARLY)
                .provider(PaymentProviderType.TOSS_BILLING)
                .amount(BigDecimal.valueOf(299000))
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key")
                .build();
        PaymentRefund refund = PaymentRefund.builder()
                .subscriptionPayment(payment)
                .paymentOrder(order)
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(refundStatus)
                .amount(BigDecimal.valueOf(299000))
                .reasonCode(PaymentRefundReasonCode.CUSTOMER_REQUEST)
                .idempotencyKey("ATS-REFUND-77")
                .providerPaymentKey("payment_key")
                .providerRefundTransactionId(refundStatus == PaymentRefundStatus.SUCCEEDED ? "cancel_tx" : null)
                .build();
        ReflectionTestUtils.setField(refund, "id", 77L);
        BillingAgreement agreement = BillingAgreement.builder()
                .id(7L)
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(BillingAgreementStatus.ACTIVE)
                .providerCustomerKey("customer_key")
                .billingKeyCiphertext("encrypted")
                .billingKeyFingerprint("fingerprint")
                .nextBillingAt(LocalDate.of(2027, 5, 25))
                .build();
        return new Fixture(user, standard, premium, userSubscription, order, payment, refund, agreement);
    }

    private User admin() {
        return User.builder()
                .id(99L)
                .nickname("admin")
                .email("admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
    }

    private CustomUserDetails actor() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }

    private record Fixture(
            User user,
            Subscription standard,
            Subscription premium,
            UserSubscription userSubscription,
            PaymentOrder order,
            SubscriptionPayment payment,
            PaymentRefund refund,
            BillingAgreement agreement) {
    }
}
