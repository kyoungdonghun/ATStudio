package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
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
                        TOSS,payment_key,ORDER-1,9900,0,300,30,9570,2026-05-26
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
                        TOSS,payment_key,ORDER-1,9900,100,300,30,9470,2026-05-26
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
                        TOSS,payment_key,ORDER-1,9900,9900,2026-05-26
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

    @Test
    @DisplayName("importSettlements rejects missing, empty, unreadable, and malformed CSV files")
    void importSettlementsRejectsInvalidFiles() throws IOException {
        assertThatThrownBy(() -> service.importSettlements(actor(), null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.importSettlements(
                actor(),
                new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]),
                null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.importSettlements(
                actor(),
                csv("provider,order_id\n"),
                null))
                .isInstanceOf(BusinessException.class);

        MultipartFile unreadable = mock(MultipartFile.class);
        given(unreadable.isEmpty()).willReturn(false);
        given(unreadable.getInputStream()).willThrow(new IOException("unreadable"));

        assertThatThrownBy(() -> service.importSettlements(actor(), unreadable, null))
                .isInstanceOf(BusinessException.class)
                .hasCauseInstanceOf(IOException.class);

        MultipartFile missingHeader = mock(MultipartFile.class);
        given(missingHeader.isEmpty()).willReturn(false);
        given(missingHeader.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
        assertThatThrownBy(() -> service.importSettlements(actor(), missingHeader, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("importSettlements reports each invalid provider row without persisting partial evidence")
    void importSettlementsReportsInvalidRows() {
        String longOrderId = "O".repeat(65);
        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor(),
                csv("""
                        provider,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date,currency
                        UNKNOWN,ORDER-1,9900,0,0,0,9900,2026-05-26,KRW
                        TOSS,%s,9900,0,0,0,9900,2026-05-26,KRW
                        TOSS,ORDER-3,-1,0,0,0,0,2026-05-26,KRW
                        TOSS,ORDER-4,not-a-number,0,0,0,0,2026-05-26,KRW
                        TOSS,ORDER-5,9900,0,0,0,9900,26-05-2026,KRW
                        TOSS,ORDER-6,9900,0,0,0,9900,2026-05-26,WONN
                        TOSS,,9900,0,0,0,9900,2026-05-26,KRW
                        """.formatted(longOrderId)),
                null).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.failedRows()).isEqualTo(7);
        assertThat(result.errors())
                .extracting(error -> error.message())
                .containsExactly(
                        "provider is invalid.",
                        "order_id must be at most 64 characters.",
                        "gross_amount cannot be negative.",
                        "gross_amount must be numeric.",
                        "settlement_base_date must be yyyy-MM-dd.",
                        "currency must be a 3-letter ISO code.",
                        "order_id is required.");
        verify(paymentSettlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("importSettlements parses BOM, quoted commas, escaped quotes, payout dates, and optional evidence")
    void importSettlementsParsesQuotedEvidence() {
        given(paymentSettlementRepository.existsByDeduplicationKey(any())).willReturn(false);
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "s".repeat(260) + ".csv",
                "text/csv",
                ("\uFEFFprovider,provider_payment_key,provider_settlement_id,order_id,gross_amount,net_settlement_amount,"
                        + "settlement_base_date,settlement_payout_date,provider_status,currency,note\n"
                        + "TOSS,,SETTLEMENT-1,\"ORDER,WITH,COMMA\",9900,9900,2026-05-26,2026-05-27,DONE,krw,"
                        + "\"operator \"\"quoted\"\", note\"\n\n")
                        .getBytes(StandardCharsets.UTF_8));

        AdminPaymentSettlementImportResponse result = service.importSettlements(actor(), file, null).getData();

        assertThat(result.importedRows()).isEqualTo(1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).save(captor.capture());
        PaymentSettlement settlement = captor.getValue();
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND);
        assertThat(settlement.getOrderId()).isEqualTo("ORDER,WITH,COMMA");
        assertThat(settlement.getSettlementPayoutDate()).isEqualTo(LocalDate.of(2026, 5, 27));
        assertThat(settlement.getCurrency()).isEqualTo("KRW");
        assertThat(settlement.getOperatorNote()).isEqualTo("operator \"quoted\", note");
        assertThat(settlement.getSourceFileName()).hasSize(255);
        assertThat(settlement.getSourcePayload()).contains("provider_settlement_id=SETTLEMENT-1");
    }

    @Test
    @DisplayName("importSettlements reconciles by provider payment key when order lookup is unavailable")
    void importSettlementsFallsBackToProviderPaymentKey() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-ALIAS")).willReturn(Optional.empty());
        given(subscriptionPaymentRepository.findFirstByPgTransactionId("payment_key"))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor(),
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-ALIAS,9900,9900,2026-05-26
                        """),
                "operator note").getData();

        assertThat(result.statusCounts()).containsEntry("MATCHED", 1);
        verify(subscriptionPaymentRepository).findFirstByPgTransactionId("payment_key");
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements creates evidence and skips unusable or duplicate payments")
    void reconcileMissingProviderSettlementsCreatesAndSkipsEvidence() {
        Fixture fixture = fixture();
        SubscriptionPayment withoutOrder = SubscriptionPayment.builder()
                .id(31L)
                .amount(BigDecimal.valueOf(1000))
                .paymentStatus(PaymentStatus.DONE)
                .build();
        SubscriptionPayment duplicate = SubscriptionPayment.builder()
                .id(32L)
                .paymentOrder(fixture.order())
                .amount(BigDecimal.valueOf(9900))
                .paymentStatus(PaymentStatus.DONE)
                .build();
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of(withoutOrder, fixture.payment(), duplicate));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(
                "ORDER-1", PaymentSettlementSource.SYSTEM_RECONCILIATION))
                .willReturn(false);
        given(paymentSettlementRepository.existsByDeduplicationKey(any()))
                .willReturn(false, true);
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.valueOf(1000));
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = service.reconcileMissingProviderSettlements(
                actor(), null).getData();

        assertThat(result.totalRows()).isEqualTo(3);
        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        assertThat(result.statusCounts()).containsEntry("PROVIDER_SETTLEMENT_NOT_FOUND", 1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).save(captor.capture());
        assertThat(captor.getValue().getNetSettlementAmount()).isEqualByComparingTo("8900");
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements rejects an inverted date range")
    void reconcileMissingProviderSettlementsRejectsInvertedRange() {
        assertThatThrownBy(() -> service.reconcileMissingProviderSettlements(
                actor(),
                new AdminPaymentSettlementReconcileRequest(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 5, 1))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements supplies each omitted date boundary")
    void reconcileMissingProviderSettlementsSuppliesOmittedBoundaries() {
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of());

        service.reconcileMissingProviderSettlements(
                actor(), new AdminPaymentSettlementReconcileRequest(null, LocalDate.now()));
        service.reconcileMissingProviderSettlements(
                actor(), new AdminPaymentSettlementReconcileRequest(LocalDate.now().minusDays(1), null));

        verify(subscriptionPaymentRepository, org.mockito.Mockito.times(2))
                .findByPaymentStatusAndCreatedAtBetween(eq(PaymentStatus.DONE), any(), any());
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements falls back to the order provider")
    void reconcileMissingProviderSettlementsFallsBackToOrderProvider() {
        Fixture fixture = fixture();
        ReflectionTestUtils.setField(fixture.payment(), "provider", null);
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of(fixture.payment()));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(any(), any())).willReturn(false);
        given(paymentSettlementRepository.existsByDeduplicationKey(any())).willReturn(false);
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.save(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.reconcileMissingProviderSettlements(actor(), null);

        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(PaymentProviderType.TOSS);
    }

    @Test
    @DisplayName("ignoreSettlement records actor and nullable operator note")
    void ignoreSettlementRecordsActor() {
        User admin = User.builder()
                .id(99L)
                .nickname("admin")
                .email("admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        PaymentSettlement settlement = PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("dedup")
                .importBatchKey("batch")
                .orderId("ORDER-1")
                .grossAmount(BigDecimal.valueOf(9900))
                .refundAmount(BigDecimal.ZERO)
                .feeAmount(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .settlementBaseDate(LocalDate.of(2026, 5, 26))
                .build();
        ReflectionTestUtils.setField(settlement, "id", 1L);
        given(paymentSettlementRepository.findWithGraphById(1L)).willReturn(Optional.of(settlement));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        var response = service.ignoreSettlement(
                1L, actor(), new AdminPaymentSettlementIgnoreRequest("accepted variance"));

        assertThat(response.getData().status()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(settlement.getIgnoredBy()).isEqualTo(admin);
        assertThat(settlement.getOperatorNote()).isEqualTo("accepted variance");
    }

    @Test
    @DisplayName("ignoreSettlement supports a system actor and an omitted note")
    void ignoreSettlementSupportsSystemActor() {
        PaymentSettlement settlement = settlementForIgnore("system-dedup");
        given(paymentSettlementRepository.findWithGraphById(2L)).willReturn(Optional.of(settlement));

        var response = service.ignoreSettlement(2L, null, null);

        assertThat(response.getData().status()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(settlement.getIgnoredBy()).isNull();
        assertThat(settlement.getOperatorNote()).isNull();
    }

    @Test
    @DisplayName("ignoreSettlement treats an actor without an id as system context")
    void ignoreSettlementSupportsActorWithoutId() {
        PaymentSettlement settlement = settlementForIgnore("missing-id-dedup");
        given(paymentSettlementRepository.findWithGraphById(3L)).willReturn(Optional.of(settlement));
        CustomUserDetails missingId = CustomUserDetails.builder()
                .email("system@test.com")
                .role(UserRole.ADMIN)
                .build();

        service.ignoreSettlement(
                3L, missingId, new AdminPaymentSettlementIgnoreRequest("system reconciliation"));

        assertThat(settlement.getIgnoredBy()).isNull();
        assertThat(settlement.getOperatorNote()).isEqualTo("system reconciliation");
        verify(userRepository, never()).findById(any());
    }

    private MockMultipartFile csv(String content) {
        return new MockMultipartFile(
                "file",
                "settlements.csv",
                "text/csv",
                content.stripIndent().getBytes(StandardCharsets.UTF_8));
    }

    private PaymentSettlement settlementForIgnore(String deduplicationKey) {
        return PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey(deduplicationKey)
                .importBatchKey("batch")
                .orderId("ORDER-1")
                .grossAmount(BigDecimal.valueOf(9900))
                .refundAmount(BigDecimal.ZERO)
                .feeAmount(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .settlementBaseDate(LocalDate.of(2026, 5, 26))
                .build();
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
                .provider(PaymentProviderType.TOSS)
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
                .provider(PaymentProviderType.TOSS)
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
