package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundExecuteRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentRefundService unit tests")
class AdminPaymentRefundServiceTest {

    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentOperationAuditLogService auditLogService;
    @Mock PaymentRefundProvider refundProvider;

    AdminPaymentRefundService service;

    @BeforeEach
    void setUp() {
        service = new AdminPaymentRefundService(
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userRepository,
                auditLogService,
                List.of(refundProvider));
    }

    @Test
    @DisplayName("createRefund stores local ledger before provider execution")
    void createRefund() {
        Fixture fixture = fixture();
        User admin = admin();
        given(subscriptionPaymentRepository.findWithGraphById(30L))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(paymentRefundRepository.save(any(PaymentRefund.class)))
                .willAnswer(invocation -> {
                    PaymentRefund refund = invocation.getArgument(0);
                    ReflectionTestUtils.setField(refund, "id", 77L);
                    return refund;
                });

        service.createRefund(
                actor(),
                new AdminPaymentRefundCreateRequest(
                        30L,
                        BigDecimal.valueOf(5000),
                        PaymentRefundReasonCode.CUSTOMER_REQUEST,
                        "customer support approved"));

        ArgumentCaptor<PaymentRefund> captor = ArgumentCaptor.forClass(PaymentRefund.class);
        verify(paymentRefundRepository).save(captor.capture());
        PaymentRefund refund = captor.getValue();
        assertThat(refund.getStatus()).isEqualTo(PaymentRefundStatus.REQUESTED);
        assertThat(refund.getAmount()).isEqualByComparingTo("5000");
        assertThat(refund.getProviderPaymentKey()).isEqualTo("payment_key");
        assertThat(refund.getIdempotencyKey()).startsWith("ATS-REFUND-");
        verify(auditLogService).recordPaymentRefundEvent(
                any(),
                any(PaymentRefund.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_REFUND_REQUESTED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(PaymentRefundStatus.REQUESTED),
                org.mockito.ArgumentMatchers.eq("customer support approved"));
        verify(refundProvider, never()).cancelPayment(any());
    }

    @Test
    @DisplayName("createRefund blocks cumulative refund amount above original payment amount")
    void createRefundBlocksOverRefund() {
        Fixture fixture = fixture();
        given(subscriptionPaymentRepository.findWithGraphById(30L))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.valueOf(9500));

        assertThatThrownBy(() -> service.createRefund(
                actor(),
                new AdminPaymentRefundCreateRequest(
                        30L,
                        BigDecimal.valueOf(500),
                        PaymentRefundReasonCode.CUSTOMER_REQUEST,
                        "too much")))
                .isInstanceOf(BusinessException.class);

        verify(paymentRefundRepository, never()).save(any());
        verify(refundProvider, never()).cancelPayment(any());
    }

    @Test
    @DisplayName("executeRefund calls provider only after approval and keeps idempotency key")
    void executeRefund() {
        Fixture fixture = fixture();
        User admin = admin();
        PaymentRefund refund = refund(fixture, PaymentRefundStatus.APPROVED);
        given(paymentRefundRepository.findByIdForUpdate(77L)).willReturn(Optional.of(refund));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(refundProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(refundProvider.cancelPayment(any(PaymentRefundProviderCommand.class)))
                .willReturn(PaymentRefundProviderResult.success(
                        "cancel_tx_key",
                        "{\"paymentKey\":\"payment_key\"}"));

        service.executeRefund(77L, actor(), new AdminPaymentRefundExecuteRequest("execute"));

        ArgumentCaptor<PaymentRefundProviderCommand> captor =
                ArgumentCaptor.forClass(PaymentRefundProviderCommand.class);
        verify(refundProvider).cancelPayment(captor.capture());
        PaymentRefundProviderCommand command = captor.getValue();
        assertThat(command.providerPaymentKey()).isEqualTo("payment_key");
        assertThat(command.amount()).isEqualByComparingTo("5000");
        assertThat(command.idempotencyKey()).isEqualTo("ATS-REFUND-77");
        assertThat(refund.getStatus()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(refund.getProviderRefundTransactionId()).isEqualTo("cancel_tx_key");
        verify(auditLogService).recordPaymentRefundEvent(
                any(),
                any(PaymentRefund.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_REFUND_PROCESSING),
                org.mockito.ArgumentMatchers.eq(PaymentRefundStatus.APPROVED),
                org.mockito.ArgumentMatchers.eq(PaymentRefundStatus.PROCESSING),
                org.mockito.ArgumentMatchers.eq("execute"));
        verify(auditLogService).recordPaymentRefundEvent(
                any(),
                any(PaymentRefund.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_REFUND_SUCCEEDED),
                org.mockito.ArgumentMatchers.eq(PaymentRefundStatus.PROCESSING),
                org.mockito.ArgumentMatchers.eq(PaymentRefundStatus.SUCCEEDED),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("executeRefund rejects unapproved refund requests")
    void executeRefundRequiresApproval() {
        PaymentRefund refund = refund(fixture(), PaymentRefundStatus.REQUESTED);
        given(paymentRefundRepository.findByIdForUpdate(77L)).willReturn(Optional.of(refund));

        assertThatThrownBy(() -> service.executeRefund(
                77L,
                actor(),
                new AdminPaymentRefundExecuteRequest("execute")))
                .isInstanceOf(BusinessException.class);

        verify(refundProvider, never()).cancelPayment(any());
    }

    private PaymentRefund refund(Fixture fixture, PaymentRefundStatus status) {
        PaymentRefund refund = PaymentRefund.builder()
                .subscriptionPayment(fixture.payment())
                .paymentOrder(fixture.order())
                .user(fixture.user())
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(status)
                .amount(BigDecimal.valueOf(5000))
                .reasonCode(PaymentRefundReasonCode.CUSTOMER_REQUEST)
                .reasonNote("refund")
                .idempotencyKey("ATS-REFUND-77")
                .providerPaymentKey("payment_key")
                .build();
        ReflectionTestUtils.setField(refund, "id", 77L);
        return refund;
    }

    private Fixture fixture() {
        User user = User.builder()
                .id(16L)
                .nickname("buyer")
                .email("buyer@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        Subscription subscription = Subscription.builder()
                .id(3L)
                .name("STANDARD")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(3)
                .build();
        UserSubscription userSubscription = UserSubscription.builder()
                .id(20L)
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(java.time.LocalDate.of(2026, 5, 1))
                .expiresAt(java.time.LocalDate.of(2026, 6, 1))
                .build();
        PaymentOrder order = PaymentOrder.builder()
                .id(10L)
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentOrderStatus.DONE)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .pgTransactionId("payment_key")
                .expiresAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        SubscriptionPayment payment = SubscriptionPayment.builder()
                .id(30L)
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .paymentOrder(order)
                .billingCycle(BillingCycle.MONTHLY)
                .provider(PaymentProviderType.TOSS_BILLING)
                .amount(BigDecimal.valueOf(9900))
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key")
                .build();
        return new Fixture(user, order, payment);
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

    private record Fixture(User user, PaymentOrder order, SubscriptionPayment payment) {
    }
}
