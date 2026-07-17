package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReceiptType;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReceiptRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReceiptEvidenceService unit tests")
class PaymentReceiptEvidenceServiceTest {

    @Mock ApplicationEventPublisher eventPublisher;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentReceiptRepository paymentReceiptRepository;
    @Mock PaymentOperationAuditLogService auditLogService;

    PaymentReceiptEvidenceService service;

    @BeforeEach
    void setUp() {
        service = new PaymentReceiptEvidenceService(
                eventPublisher,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentReceiptRepository,
                auditLogService);
    }

    @Test
    @DisplayName("recordCommittedPayment stores payment and cash receipt evidence without raw card details")
    void recordCommittedPayment_storesReceiptEvidence() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findById(1L)).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findById(2L)).willReturn(Optional.of(fixture.payment()));
        given(paymentReceiptRepository.existsByPaymentOrderAndType(
                fixture.order(), PaymentReceiptType.PAYMENT_RECEIPT)).willReturn(false);
        given(paymentReceiptRepository.existsByPaymentOrderAndType(
                fixture.order(), PaymentReceiptType.CASH_RECEIPT)).willReturn(false);
        given(paymentReceiptRepository.save(any(PaymentReceipt.class)))
                .willAnswer(invocation -> {
                    PaymentReceipt receipt = invocation.getArgument(0);
                    ReflectionTestUtils.setField(receipt, "id",
                            receipt.getType() == PaymentReceiptType.PAYMENT_RECEIPT ? 10L : 11L);
                    return receipt;
                });

        service.recordCommittedPayment(1L, 2L, """
                {
                  "paymentKey": "payment_key",
                  "orderId": "ORDER-1",
                  "status": "DONE",
                  "method": "카드",
                  "approvedAt": "2026-05-25T10:00:00+09:00",
                  "totalAmount": 9900,
                  "receipt": {
                    "url": "https://dashboard.tosspayments.com/receipt/payment_key"
                  },
                  "cashReceipt": {
                    "receiptKey": "cash_receipt_key",
                    "receiptUrl": "https://dashboard.tosspayments.com/cash-receipts/cash_receipt_key",
                    "requestedAt": "2026-05-25T10:00:01+09:00"
                  },
                  "card": {
                    "number": "1234-****-****-5678"
                  }
                }
                """);

        ArgumentCaptor<PaymentReceipt> captor = ArgumentCaptor.forClass(PaymentReceipt.class);
        verify(paymentReceiptRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<PaymentReceipt> receipts = captor.getAllValues();
        assertThat(receipts).extracting(PaymentReceipt::getType)
                .containsExactly(PaymentReceiptType.PAYMENT_RECEIPT, PaymentReceiptType.CASH_RECEIPT);
        assertThat(receipts.get(0).getReceiptUrl())
                .isEqualTo("https://dashboard.tosspayments.com/receipt/payment_key");
        assertThat(receipts.get(1).getReceiptKey()).isEqualTo("cash_receipt_key");
        assertThat(receipts.get(0).getEvidencePayload()).doesNotContain("1234-****-****-5678");
        assertThat(receipts.get(0).getIssuedAt()).isEqualTo(LocalDateTime.of(2026, 5, 25, 10, 0));
        verify(auditLogService, org.mockito.Mockito.times(2)).recordReceiptEvidenceCreated(any(PaymentReceipt.class));
    }

    @Test
    @DisplayName("publishSuccessfulChargeEvidence emits an after-commit receipt event")
    void publishSuccessfulChargeEvidence_publishesEvent() {
        Fixture fixture = fixture();

        service.publishSuccessfulChargeEvidence(fixture.order(), fixture.payment(), "{\"paymentKey\":\"pay_1\"}");

        ArgumentCaptor<PaymentReceiptEvidenceRequestedEvent> captor =
                ArgumentCaptor.forClass(PaymentReceiptEvidenceRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().paymentOrderId()).isEqualTo(1L);
        assertThat(captor.getValue().subscriptionPaymentId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("recordCommittedPayment skips duplicate receipt types")
    void recordCommittedPayment_skipsDuplicateReceipt() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findById(1L)).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findById(2L)).willReturn(Optional.of(fixture.payment()));
        given(paymentReceiptRepository.existsByPaymentOrderAndType(
                fixture.order(), PaymentReceiptType.PAYMENT_RECEIPT)).willReturn(true);

        service.recordCommittedPayment(1L, 2L, """
                {
                  "paymentKey": "payment_key",
                  "receipt": {"url": "https://dashboard.tosspayments.com/receipt/payment_key"}
                }
                """);

        verify(paymentReceiptRepository, never()).save(any(PaymentReceipt.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "data:text/html,test",
            "file:///tmp/receipt",
            "ftp://receipts.example.com/r/1",
            "//receipts.example.com/r/1",
            "https://user:password@receipts.example.com/r/1",
            "https://receipts.example.com:8443/r/1",
            "not a url"
    })
    @DisplayName("recordCommittedPayment rejects unsafe provider receipt URLs")
    void recordCommittedPayment_rejectsUnsafeReceiptUrls(String receiptUrl) {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findById(1L)).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findById(2L)).willReturn(Optional.of(fixture.payment()));

        service.recordCommittedPayment(1L, 2L, """
                {
                  "paymentKey": "payment_key",
                  "receipt": {"url": "%s"}
                }
                """.formatted(receiptUrl));

        verify(paymentReceiptRepository, never()).save(any(PaymentReceipt.class));
    }

    @Test
    @DisplayName("recordCommittedPayment keeps cash receipt evidence but suppresses its unsafe URL")
    void recordCommittedPayment_suppressesUnsafeCashReceiptUrl() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findById(1L)).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findById(2L)).willReturn(Optional.of(fixture.payment()));
        given(paymentReceiptRepository.existsByPaymentOrderAndType(
                fixture.order(), PaymentReceiptType.CASH_RECEIPT)).willReturn(false);
        given(paymentReceiptRepository.save(any(PaymentReceipt.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.recordCommittedPayment(1L, 2L, """
                {
                  "paymentKey": "payment_key",
                  "cashReceipt": {
                    "receiptKey": "cash_receipt_key",
                    "receiptUrl": "javascript:alert('provider_key')"
                  }
                }
                """);

        ArgumentCaptor<PaymentReceipt> captor = ArgumentCaptor.forClass(PaymentReceipt.class);
        verify(paymentReceiptRepository).save(captor.capture());
        assertThat(captor.getValue().getReceiptUrl()).isNull();
        assertThat(captor.getValue().getEvidencePayload()).doesNotContain("javascript", "provider_key");
    }

    private Fixture fixture() {
        User user = User.builder()
                .id(1L)
                .nickname("buyer")
                .email("buyer@test.com")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        Subscription subscription = Subscription.builder()
                .id(10L)
                .name("STANDARD")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(1)
                .maxPlaylists(3)
                .build();
        UserSubscription userSubscription = UserSubscription.builder()
                .id(100L)
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.of(2026, 5, 25))
                .expiresAt(LocalDate.of(2026, 6, 25))
                .build();
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .pgTransactionId("payment_key")
                .expiresAt(LocalDateTime.of(2026, 5, 25, 10, 10))
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .paymentOrder(order)
                .provider(PaymentProviderType.TOSS)
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key")
                .build();
        ReflectionTestUtils.setField(payment, "id", 2L);
        return new Fixture(order, payment);
    }

    private record Fixture(PaymentOrder order, SubscriptionPayment payment) {
    }
}
