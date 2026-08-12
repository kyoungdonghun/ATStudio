package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
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
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingAgreementApplicationService unit tests")
class BillingAgreementApplicationServiceTest {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174000";

    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock PlaylistService playlistService;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @Mock PaymentCommandTransactionService paymentCommandTransactionService;
    @Mock BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    @Mock BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    @Mock BillingAgreementPrepareTransactionService billingAgreementPrepareTransactionService;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    PaymentCommandKeyFactory paymentCommandKeyFactory = new PaymentCommandKeyFactory();

    BillingAgreementApplicationService service;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        lenient().when(recurringPaymentProvider.supportsPureDeterministicPrepare()).thenReturn(true);
        lenient().when(billingAgreementPrepareTransactionService.ensureAgreement(
                        anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(200L);
        lenient().when(billingAgreementPrepareTransactionService.claim(
                        anyLong(), any(), anyString(), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Long userID = invocation.getArgument(0);
                    BillingAgreementPrepareRequest request = invocation.getArgument(1);
                    String commandKey = invocation.getArgument(2);
                    BigDecimal amount = request.purpose() == PaymentPurpose.SUBSCRIBE
                            ? BigDecimal.valueOf(9900)
                            : BigDecimal.ZERO;
                    return new BillingAgreementPrepareTransactionService.PrepareClaim(
                            userID,
                            request.subscriptionId(),
                            request.billingCycle(),
                            request.purpose(),
                            200L,
                            "ATS-BILL-TEST",
                            commandKey,
                            "ats_billing_customer_1",
                            PaymentProviderType.TOSS,
                            BillingAgreementStatus.READY,
                            amount,
                            "KRW",
                            LocalDateTime.now().plusMinutes(15));
                });
        lenient().when(billingAgreementPrepareTransactionService.finalizeDescriptor(
                        any(), any(), any(LocalDateTime.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new BillingAgreementApplicationService(
                userRepository,
                userSubscriptionRepository,
                paymentOrderRepository,
                billingAgreementRepository,
                billingKeyCrypto,
                paymentCommandTransactionService,
                billingAgreementCleanupTransactionService,
                billingAgreementCleanupProviderExecutor,
                billingAgreementPrepareTransactionService,
                paymentCommandKeyFactory,
                List.of(recurringPaymentProvider)
        );
    }

    @Test
    @DisplayName("prepare creates READY billing agreement and TOSS_BILLING payment order")
    void prepareBillingAgreement_success() {
        given(recurringPaymentProvider.prepareAgreement(any()))
                .willReturn(new BillingAgreementPrepareResult(
                        PaymentProviderType.TOSS,
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
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);

        assertThat(response.provider()).isEqualTo(PaymentProviderType.TOSS);
        assertThat(response.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE);
        assertThat(response.agreementStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(response.subscriptionId()).isEqualTo(10L);
        assertThat(response.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(9900));
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.checkout().customerKey()).isEqualTo("ats_billing_customer_1");
        assertThat(response.checkout().method()).isEqualTo("CARD");

        InOrder ordering = inOrder(billingAgreementPrepareTransactionService, recurringPaymentProvider);
        ordering.verify(billingAgreementPrepareTransactionService)
                .ensureAgreement(eq(1L), any(), any(LocalDateTime.class));
        ordering.verify(billingAgreementPrepareTransactionService)
                .claim(eq(1L), any(), anyString(), any(LocalDateTime.class));
        ordering.verify(recurringPaymentProvider).prepareAgreement(any());
        ordering.verify(billingAgreementPrepareTransactionService)
                .finalizeDescriptor(any(), any(), any(LocalDateTime.class));
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
    }

    @Test
    @DisplayName("prepare rethrows an unexpected integrity failure instead of translating it to conflict")
    void prepareBillingAgreement_unexpectedIntegrityFailureIsRethrown() {
        DataIntegrityViolationException unexpected =
                new DataIntegrityViolationException("uq_unrelated_integrity_constraint");

        given(billingAgreementPrepareTransactionService.claim(
                eq(1L), any(), anyString(), any(LocalDateTime.class)))
                .willThrow(unexpected);

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY))
                .isSameAs(unexpected);

        verify(recurringPaymentProvider, never()).prepareAgreement(any());
    }

    @Test
    @DisplayName("prepare retries only the named first-agreement unique loser")
    void prepareBillingAgreement_retriesNamedAgreementUniqueLoser() {
        DataIntegrityViolationException expectedRace = new DataIntegrityViolationException(
                "duplicate agreement claim",
                new RuntimeException("constraint uq_billing_agreements_user_provider"));
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(expectedRace)
                .willReturn(200L);
        stubSuccessfulPrepareOrder();

        BillingAgreementPrepareResponse response = service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);

        assertThat(response.orderId()).isEqualTo("ATS-BILL-TEST");
        verify(billingAgreementPrepareTransactionService, times(2))
                .ensureAgreement(eq(1L), any(), any(LocalDateTime.class));
        verify(billingAgreementPrepareTransactionService)
                .claim(eq(1L), any(), anyString(), any(LocalDateTime.class));
        verify(recurringPaymentProvider).prepareAgreement(any());
    }

    @Test
    @DisplayName("prepare creates zero-amount billing agreement order for active subscription re-registration")
    void prepareBillingAgreement_activeSubscriptionReRegistration() {
        given(recurringPaymentProvider.prepareAgreement(any()))
                .willReturn(new BillingAgreementPrepareResult(
                        PaymentProviderType.TOSS,
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
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY);

        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.provider()).isEqualTo(PaymentProviderType.TOSS);
        assertThat(response.purpose()).isEqualTo(PaymentPurpose.BILLING_AGREEMENT);
        assertThat(response.subscriptionId()).isEqualTo(10L);
        assertThat(response.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(response.currency()).isEqualTo("KRW");
        verify(billingAgreementPrepareTransactionService)
                .claim(eq(1L), any(), anyString(), any(LocalDateTime.class));
        verify(recurringPaymentProvider).prepareAgreement(any());
    }

    @Test
    @DisplayName("prepare rejects billing-agreement intent for a non-subscriber before side effects")
    void prepareBillingAgreement_nonSubscriberPurposeMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.PAYMENT_PURPOSE_MISMATCH));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_PURPOSE_MISMATCH));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare rejects subscribe intent for an active subscriber before side effects")
    void prepareBillingAgreement_activeSubscriberPurposeMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.PAYMENT_PURPOSE_MISMATCH));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_PURPOSE_MISMATCH));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare rejects billing re-registration for a different current plan")
    void prepareBillingAgreement_currentPlanMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        11L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare rejects billing re-registration for a different current cycle")
    void prepareBillingAgreement_currentCycleMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.YEARLY,
                        PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare rejects an INDIVIDUAL principal selecting the same-name BUSINESS plan")
    void prepareBillingAgreement_individualToBusinessAudienceMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare rejects a BUSINESS principal selecting the same-name INDIVIDUAL plan")
    void prepareBillingAgreement_businessToIndividualAudienceMismatch() {
        given(billingAgreementPrepareTransactionService.ensureAgreement(
                eq(1L), any(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH));

        assertNoPrepareSideEffects();
    }

    @Test
    @DisplayName("prepare accepts a cancelled grace-period subscription for billing re-registration")
    void prepareBillingAgreement_cancelledGraceReRegistration() {
        stubSuccessfulPrepareOrder();

        BillingAgreementPrepareResponse response = service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY);

        assertThat(response.purpose()).isEqualTo(PaymentPurpose.BILLING_AGREEMENT);
        assertThat(response.subscriptionId()).isEqualTo(10L);
        assertThat(response.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.provider()).isEqualTo(PaymentProviderType.TOSS);
        assertThat(response.currency()).isEqualTo("KRW");

        verify(billingAgreementPrepareTransactionService)
                .claim(eq(1L), any(), anyString(), any(LocalDateTime.class));
        verify(recurringPaymentProvider).prepareAgreement(any());
    }

    @Test
    @DisplayName("prepare preserves approved BUSINESS subscription registration")
    void prepareBillingAgreement_businessSubscription() {
        stubSuccessfulPrepareOrder();

        BillingAgreementPrepareResponse response = service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(
                        10L,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);

        assertThat(response.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE);
        assertThat(response.subscriptionId()).isEqualTo(10L);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(9900));
        verify(recurringPaymentProvider).prepareAgreement(any());
    }

    @Test
    @DisplayName("confirm issues billing key, charges immediately, and activates subscription")
    void confirmBillingAgreement_success() {
        UserSubscription saved = buildUserSubscription(
                100L,
                buildUser(1L),
                buildSubscription(10L),
                SubscriptionStatus.ACTIVE);
        PaymentCommandTransactionService.BillingConfirmClaim claim = providerClaim(
                "ORDER-1",
                PaymentPurpose.SUBSCRIBE,
                BigDecimal.valueOf(9900));
        BillingAgreementConfirmResponse finalized = new BillingAgreementConfirmResponse(
                "ORDER-1",
                PaymentOrderStatus.DONE,
                PaymentProviderType.TOSS,
                BillingAgreementStatus.ACTIVE,
                saved.getExpiresAt(),
                UserSubscriptionResponse.from(saved));

        given(paymentCommandTransactionService.claimBillingConfirm(
                eq(1L),
                eq("ORDER-1"),
                eq("ats_billing_customer_1"),
                eq(BigDecimal.valueOf(9900)),
                any(LocalDateTime.class)))
                .willReturn(claim);
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
        given(paymentCommandTransactionService.finalizeInitialCharge(1L, 200L, "ORDER-1"))
                .willReturn(finalized);

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
        assertThat(response.toString()).doesNotContain("billing_raw_key");

        ArgumentCaptor<BillingChargeCommand> chargeCaptor = ArgumentCaptor.forClass(BillingChargeCommand.class);
        verify(recurringPaymentProvider).charge(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().idempotencyKey())
                .isEqualTo("billing-initial-ORDER-1-attempt-1");

        InOrder ordering = inOrder(
                paymentCommandTransactionService,
                recurringPaymentProvider,
                billingKeyCrypto);
        ordering.verify(paymentCommandTransactionService).claimBillingConfirm(
                eq(1L),
                eq("ORDER-1"),
                eq("ats_billing_customer_1"),
                eq(BigDecimal.valueOf(9900)),
                any(LocalDateTime.class));
        ordering.verify(recurringPaymentProvider).confirmAgreement(any());
        ordering.verify(billingKeyCrypto).encrypt("billing_raw_key");
        ordering.verify(paymentCommandTransactionService).storeIssuedBillingKey(
                200L,
                "ORDER-1",
                "encrypted-key",
                "fingerprint",
                "CARD",
                "1234");
        ordering.verify(recurringPaymentProvider).charge(any());
        ordering.verify(paymentCommandTransactionService).recordProviderSuccess(
                200L,
                "ORDER-1",
                "tx_1",
                "{\"paymentKey\":\"pay_1\"}",
                "CARD",
                "1234");
        ordering.verify(paymentCommandTransactionService).finalizeInitialCharge(1L, 200L, "ORDER-1");
    }

    @Test
    @DisplayName("confirm billing agreement re-registration stores key without an immediate charge")
    void confirmBillingAgreement_reRegistrationOnly() {
        UserSubscription activeSubscription = buildUserSubscription(
                100L,
                buildUser(1L),
                buildSubscription(10L),
                SubscriptionStatus.ACTIVE);
        PaymentCommandTransactionService.BillingConfirmClaim claim = providerClaim(
                "ORDER-REAUTH",
                PaymentPurpose.BILLING_AGREEMENT,
                BigDecimal.ZERO);
        BillingAgreementConfirmResponse finalized = new BillingAgreementConfirmResponse(
                "ORDER-REAUTH",
                PaymentOrderStatus.DONE,
                PaymentProviderType.TOSS,
                BillingAgreementStatus.ACTIVE,
                activeSubscription.getExpiresAt(),
                UserSubscriptionResponse.from(activeSubscription));

        given(paymentCommandTransactionService.claimBillingConfirm(
                eq(1L),
                eq("ORDER-REAUTH"),
                eq("ats_billing_customer_1"),
                eq(BigDecimal.ZERO),
                any(LocalDateTime.class)))
                .willReturn(claim);
        given(recurringPaymentProvider.confirmAgreement(any()))
                .willReturn(BillingAgreementConfirmResult.success(
                        "billing_raw_key",
                        "CARD",
                        "1234",
                        "{\"method\":\"CARD\"}"));
        given(billingKeyCrypto.encrypt("billing_raw_key"))
                .willReturn(new BillingKeyCrypto.ProtectedBillingKey("encrypted-key", "fingerprint"));
        given(paymentCommandTransactionService.finalizeInitialCharge(1L, 200L, "ORDER-REAUTH"))
                .willReturn(finalized);

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
        verify(recurringPaymentProvider, never()).charge(any());
        verify(paymentCommandTransactionService).recordProviderSuccess(
                200L,
                "ORDER-REAUTH",
                "billing-agreement-ORDER-REAUTH",
                "{\"method\":\"CARD\"}",
                "CARD",
                "1234");
    }

    @Test
    @DisplayName("initial charge failure leaves subscription inactive")
    void confirmBillingAgreement_chargeFailure() {
        PaymentCommandTransactionService.BillingConfirmClaim claim = providerClaim(
                "ORDER-1",
                PaymentPurpose.SUBSCRIBE,
                BigDecimal.valueOf(9900));

        given(paymentCommandTransactionService.claimBillingConfirm(
                eq(1L),
                eq("ORDER-1"),
                eq("ats_billing_customer_1"),
                eq(BigDecimal.valueOf(9900)),
                any(LocalDateTime.class)))
                .willReturn(claim);
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

        InOrder ordering = inOrder(paymentCommandTransactionService, recurringPaymentProvider);
        ordering.verify(paymentCommandTransactionService).recordProviderFailure(
                200L,
                "ORDER-1",
                "DECLINED",
                "Initial charge failed.",
                PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                true);
        ordering.verify(recurringPaymentProvider).cancelAgreement(any());
        ordering.verify(paymentCommandTransactionService).clearIssuedBillingKeyAfterCleanup(200L);
    }

    @Test
    @DisplayName("confirm rejects owner mismatch before provider call")
    void confirmBillingAgreement_ownerMismatch() {
        given(paymentCommandTransactionService.claimBillingConfirm(
                eq(1L),
                eq("ORDER-1"),
                eq("ats_billing_customer_1"),
                eq(BigDecimal.valueOf(9900)),
                any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

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
        given(paymentCommandTransactionService.claimBillingConfirm(
                eq(1L),
                eq("ORDER-1"),
                eq("wrong_customer_key"),
                eq(BigDecimal.valueOf(9900)),
                any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT));

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
        given(billingAgreementPrepareTransactionService.claim(
                eq(1L), any(), anyString(), any(LocalDateTime.class)))
                .willThrow(new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE));

        assertThatThrownBy(() -> service.prepareBillingAgreement(
                buildUserDetails(1L),
                new BillingAgreementPrepareRequest(10L, BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE));

        verify(recurringPaymentProvider, never()).prepareAgreement(any());
    }

    @Test
    @DisplayName("cancel deletes provider billing key and preserves paid access as CANCELLED")
    void cancelMyBillingAgreement_success() {
        User user = buildUser(1L);
        Subscription subscription = buildSubscription(10L);
        UserSubscription subscriptionAccess = buildUserSubscription(
                100L,
                user,
                subscription,
                SubscriptionStatus.ACTIVE);
        LocalDateTime leaseStartedAt = LocalDateTime.now().withNano(0);
        BillingAgreementCleanupTransactionService.UserCancellationClaim claim =
                BillingAgreementCleanupTransactionService.UserCancellationClaim.callProvider(
                        200L,
                        PaymentProviderType.TOSS,
                        "encrypted-key",
                        leaseStartedAt);
        BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult =
                BillingAgreementCleanupProviderExecutor.CleanupProviderResult.succeeded();
        BillingAgreementResponse expectedResponse = new BillingAgreementResponse(
                PaymentProviderType.TOSS,
                BillingAgreementStatus.CANCELLED,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                UserSubscriptionResponse.from(subscriptionAccess));
        given(billingAgreementCleanupTransactionService.claimUserCancellation(
                eq(1L),
                any(LocalDateTime.class))).willReturn(claim);
        given(billingAgreementCleanupProviderExecutor.deleteBillingKey(
                PaymentProviderType.TOSS,
                "encrypted-key")).willReturn(providerResult);
        given(billingAgreementCleanupTransactionService.recordUserCancellationResult(
                1L,
                claim,
                providerResult)).willReturn(expectedResponse);

        BillingAgreementResponse response = service.cancelMyBillingAgreement(buildUserDetails(1L));

        assertThat(response.status()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(response.payMethod()).isNull();
        assertThat(response.maskedMethod()).isNull();
        assertThat(response.toString()).doesNotContain("encrypted-key");
        InOrder ordering = inOrder(
                billingAgreementCleanupTransactionService,
                billingAgreementCleanupProviderExecutor);
        ordering.verify(billingAgreementCleanupTransactionService)
                .claimUserCancellation(eq(1L), any(LocalDateTime.class));
        ordering.verify(billingAgreementCleanupProviderExecutor)
                .deleteBillingKey(PaymentProviderType.TOSS, "encrypted-key");
        ordering.verify(billingAgreementCleanupTransactionService)
                .recordUserCancellationResult(1L, claim, providerResult);
    }

    private void stubSuccessfulPrepareOrder() {
        given(recurringPaymentProvider.prepareAgreement(any()))
                .willReturn(new BillingAgreementPrepareResult(
                        PaymentProviderType.TOSS,
                        "TOSS_BILLING_AUTH",
                        "{\"phase\":\"prepare\"}",
                        Map.of(
                                "clientKey", "test_ck",
                                "customerKey", "ats_billing_customer_1",
                                "successUrl", "http://localhost/success",
                                "failUrl", "http://localhost/fail",
                                "method", "CARD"
                        )));
    }

    private void assertNoPrepareSideEffects() {
        verifyNoInteractions(billingAgreementRepository, paymentOrderRepository);
        verify(billingAgreementPrepareTransactionService, never())
                .claim(anyLong(), any(), anyString(), any(LocalDateTime.class));
        verify(billingAgreementPrepareTransactionService, never())
                .finalizeDescriptor(any(), any(), any(LocalDateTime.class));
        verify(recurringPaymentProvider, never()).prepareAgreement(any());
    }

    private PaymentCommandTransactionService.BillingConfirmClaim providerClaim(
            String orderID,
            PaymentPurpose purpose,
            BigDecimal amount) {
        return new PaymentCommandTransactionService.BillingConfirmClaim(
                PaymentCommandTransactionService.BillingConfirmAction.CALL_PROVIDER,
                200L,
                orderID,
                purpose,
                "ats_billing_customer_1",
                "Basic recurring subscription",
                amount,
                "user1@test.com",
                "user1",
                "billing-initial-" + orderID + "-attempt-1",
                null);
    }

    private User buildUser(Long id) {
        return buildUser(id, UserType.INDIVIDUAL);
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

    private Subscription buildSubscription(Long id) {
        return buildSubscription(id, UserType.INDIVIDUAL, "Basic");
    }

    private Subscription buildSubscription(Long id, UserType userType, String name) {
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

    private BillingAgreement buildReadyAgreement(User user) {
        return BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_billing_customer_1")
                .build();
    }

    private PaymentOrder buildBillingOrder(User user, Subscription subscription, BillingAgreement agreement) {
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS)
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
