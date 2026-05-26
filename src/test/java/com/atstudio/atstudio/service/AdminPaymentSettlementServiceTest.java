package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementReconcileRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentSettlementService unit tests")
class AdminPaymentSettlementServiceTest {

    @Mock PaymentSettlementRepository paymentSettlementRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentOperationAuditLogService auditLogService;

    AdminPaymentSettlementService service;

    @BeforeEach
    void setUp() {
        service = new AdminPaymentSettlementService(
                paymentSettlementRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userRepository,
                auditLogService);
    }

    @Test
    @DisplayName("importSettlements stores matched settlement rows")
    void importSettlementsMatched() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findByPaymentOrder(fixture.order()))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.existsByDeduplicationKey(any())).willReturn(false);
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> {
                    PaymentSettlement settlement = invocation.getArgument(0);
                    ReflectionTestUtils.setField(settlement, "id", 1L);
                    return settlement;
                });

        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor(),
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date
                        TOSS_BILLING,payment_key,ORDER-1,9900,0,300,30,9570,2026-05-26
                        """),
                "import note").getData();

        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.failedRows()).isZero();
        assertThat(result.statusCounts()).containsEntry("MATCHED", 1);

        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).save(captor.capture());
        PaymentSettlement settlement = captor.getValue();
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.MATCHED);
        assertThat(settlement.getPaymentOrder()).isEqualTo(fixture.order());
        assertThat(settlement.getSubscriptionPayment()).isEqualTo(fixture.payment());
        assertThat(settlement.getNetSettlementAmount()).isEqualByComparingTo("9570");
        verify(auditLogService).recordPaymentSettlementEvent(
                any(),
                any(PaymentSettlement.class),
                org.mockito.ArgumentMatchers.eq(
                        com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(PaymentSettlementStatus.MATCHED),
                org.mockito.ArgumentMatchers.eq("Settlement row imported."));
    }

    @Test
    @DisplayName("importSettlements marks amount mismatch")
    void importSettlementsMismatch() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findByPaymentOrder(fixture.order()))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.existsByDeduplicationKey(any())).willReturn(false);
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor(),
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date
                        TOSS_BILLING,payment_key,ORDER-1,9900,100,300,30,9470,2026-05-26
                        """),
                null).getData();

        assertThat(result.statusCounts()).containsEntry("MISMATCHED", 1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED);
        assertThat(captor.getValue().getMismatchReason()).contains("refund_amount");
    }

    @Test
    @DisplayName("importSettlements skips duplicate rows by deduplication key")
    void importSettlementsSkipsDuplicate() {
        given(paymentSettlementRepository.existsByDeduplicationKey(any())).willReturn(true);

        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor(),
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS_BILLING,payment_key,ORDER-1,9900,9900,2026-05-26
                        """),
                null).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        verify(paymentSettlementRepository, never()).save(any());
        verify(paymentOrderRepository, never()).findByOrderId(any());
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements does not create a missing-provider row when provider evidence exists")
    void reconcileMissingProviderSettlementsSkipsExistingProviderEvidence() {
        Fixture fixture = fixture();
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE),
                any(),
                any()))
                .willReturn(List.of(fixture.payment()));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(
                "ORDER-1",
                PaymentSettlementSource.SYSTEM_RECONCILIATION))
                .willReturn(true);

        AdminPaymentSettlementImportResponse result = service.reconcileMissingProviderSettlements(
                actor(),
                new AdminPaymentSettlementReconcileRequest(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31))).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        verify(paymentSettlementRepository, never()).existsByDeduplicationKey(any());
        verify(paymentSettlementRepository, never()).save(any());
    }

    private MockMultipartFile csv(String content) {
        return new MockMultipartFile(
                "file",
                "settlements.csv",
                "text/csv",
                content.stripIndent().getBytes(StandardCharsets.UTF_8));
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
        return new Fixture(order, payment);
    }

    private CustomUserDetails actor() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }

    private record Fixture(PaymentOrder order, SubscriptionPayment payment) {
    }
}
