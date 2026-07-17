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
import com.atstudio.atstudio.repository.PaymentOrderRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentEntitlementCorrectionService unit tests")
class AdminPaymentEntitlementCorrectionServiceTest {

    private static final LocalDateTime CORRECTION_CREATED_AT =
            LocalDateTime.of(2026, 7, 16, 10, 0);
    private static final LocalDateTime AGREEMENT_UPDATED_BEFORE_CORRECTION =
            CORRECTION_CREATED_AT.minusSeconds(1);

    @Mock PaymentEntitlementCorrectionRepository correctionRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
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
                paymentOrderRepository,
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
        given(billingAgreementRepository.findByUserAndProvider(
                fixture.user(), PaymentProviderType.TOSS))
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
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
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
        org.mockito.InOrder lockOrder = inOrder(billingAgreementRepository, userSubscriptionRepository);
        lockOrder.verify(billingAgreementRepository).findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS);
        lockOrder.verify(userSubscriptionRepository).findByIdForUpdate(fixture.userSubscription().getId());
    }

    @Test
    @DisplayName("createCorrection rejects non-succeeded refund records")
    void createCorrectionRejectsPendingRefund() {
        Fixture fixture = fixture(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(fixture.standard().getId())).willReturn(Optional.of(fixture.standard()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.createCorrection(actor(), request(fixture.standard().getId())))
                .isInstanceOf(BusinessException.class);

        verify(correctionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCorrection rejects another non-terminal correction for the refund and subscription")
    void createCorrectionRejectsDuplicateNonTerminalCorrection() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(fixture.standard().getId())).willReturn(Optional.of(fixture.standard()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(correctionRepository.existsByPaymentRefund_IdAndUserSubscription_IdAndStatusIn(
                org.mockito.ArgumentMatchers.eq(77L),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                any()))
                .willReturn(true);

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
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
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

    @Test
    @DisplayName("executeCorrection rejects a stale before-state snapshot")
    void executeCorrectionRejectsStaleBeforeState() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        fixture.userSubscription().applyEntitlementCorrection(
                fixture.standard(),
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE,
                LocalDate.now().plusMonths(1),
                true);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("stale")))
                .isInstanceOf(BusinessException.class);

        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
        verify(auditLogService, never()).recordPaymentEntitlementCorrectionEvent(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("executeCorrection rejects billing agreement drift from the captured before-state")
    void executeCorrectionRejectsAgreementDrift() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        fixture.agreement().cancel();
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("stale agreement")))
                .isInstanceOf(BusinessException.class);

        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
        assertThat(fixture.userSubscription().getSubscription()).isEqualTo(fixture.premium());
    }

    @Test
    @DisplayName("executeCorrection rejects a completed same-status billing agreement replacement")
    void executeCorrectionRejectsSameStatusAgreementReplacement() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        fixture.agreement().prepareRegistration("replacement-customer-key");
        fixture.agreement().activate(
                "replacement-encrypted",
                "replacement-fingerprint",
                "CARD",
                "**** 4321",
                LocalDate.of(2027, 5, 25));
        ReflectionTestUtils.setField(
                fixture.agreement(),
                "updatedAt",
                CORRECTION_CREATED_AT.plusSeconds(1));
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("stale replacement")))
                .isInstanceOf(BusinessException.class);

        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
        assertThat(fixture.userSubscription().getSubscription()).isEqualTo(fixture.premium());
        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(fixture.agreement().getBillingKeyFingerprint()).isEqualTo("replacement-fingerprint");
        verify(auditLogService, never()).recordPaymentEntitlementCorrectionEvent(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("executeCorrection treats equal agreement and correction timestamps as stale")
    void executeCorrectionRejectsEqualAgreementRevisionTimestamp() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        ReflectionTestUtils.setField(fixture.agreement(), "updatedAt", CORRECTION_CREATED_AT);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("ambiguous revision")))
                .isInstanceOf(BusinessException.class);

        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
    }

    @Test
    @DisplayName("executeCorrection treats missing agreement revision timestamps as stale")
    void executeCorrectionRejectsMissingAgreementRevisionTimestamp() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        ReflectionTestUtils.setField(fixture.agreement(), "updatedAt", null);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("missing revision")))
                .isInstanceOf(BusinessException.class);

        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
    }

    @Test
    @DisplayName("executeCorrection rejects an order that can still receive a Provider outcome")
    void executeCorrectionRejectsNonTerminalPayment() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.APPROVED);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(paymentOrderRepository.existsByBillingAgreementAndPurposeInAndStatusIn(any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("in flight")))
                .isInstanceOf(BusinessException.class);

        assertThat(correction.getStatus()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
        verify(auditLogService, never()).recordPaymentEntitlementCorrectionEvent(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("executeCorrection returns an already succeeded correction without applying it again")
    void executeCorrectionSucceededRetryIsIdempotent() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction =
                correction(fixture, PaymentEntitlementCorrectionStatus.SUCCEEDED);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));

        var response = service.executeCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionExecuteRequest("retry"));

        assertThat(response.getData().status()).isEqualTo(PaymentEntitlementCorrectionStatus.SUCCEEDED);
        verify(userSubscriptionRepository, never()).findByIdForUpdate(any());
        verify(auditLogService, never()).recordPaymentEntitlementCorrectionEvent(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("listCorrections and getCorrection return the persisted operator workflow")
    void listAndGetCorrections() {
        PaymentEntitlementCorrection correction = correction(
                fixture(PaymentRefundStatus.SUCCEEDED),
                PaymentEntitlementCorrectionStatus.REQUESTED);
        given(correctionRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(correction), PageRequest.of(0, 1), 1));
        given(correctionRepository.findDetailedById(88L)).willReturn(Optional.of(correction));

        var list = service.listCorrections(0, 0);
        var detail = service.getCorrection(88L);

        assertThat(list.getDataList()).hasSize(1);
        assertThat(detail.getData().id()).isEqualTo(88L);
        assertThat(detail.getData().status()).isEqualTo(PaymentEntitlementCorrectionStatus.REQUESTED);
    }

    @Test
    @DisplayName("getCorrection and approveCorrection reject missing or non-requested records")
    void correctionLookupAndApprovalRejectInvalidRecords() {
        given(correctionRepository.findDetailedById(404L)).willReturn(Optional.empty());
        given(correctionRepository.findByIdForUpdate(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCorrection(404L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.approveCorrection(
                404L, actor(), new AdminPaymentEntitlementCorrectionApproveRequest("approve")))
                .isInstanceOf(BusinessException.class);

        PaymentEntitlementCorrection approved = correction(
                fixture(PaymentRefundStatus.SUCCEEDED),
                PaymentEntitlementCorrectionStatus.APPROVED);
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.approveCorrection(
                88L, actor(), new AdminPaymentEntitlementCorrectionApproveRequest("duplicate")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("approveCorrection records the approving administrator")
    void approveCorrection() {
        Fixture fixture = fixture(PaymentRefundStatus.SUCCEEDED);
        PaymentEntitlementCorrection correction = correction(
                fixture,
                PaymentEntitlementCorrectionStatus.REQUESTED);
        User admin = admin();
        given(correctionRepository.findByIdForUpdate(88L)).willReturn(Optional.of(correction));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        var response = service.approveCorrection(
                88L,
                actor(),
                new AdminPaymentEntitlementCorrectionApproveRequest("reviewed evidence"));

        assertThat(response.getData().status()).isEqualTo(PaymentEntitlementCorrectionStatus.APPROVED);
        assertThat(correction.getApprovedBy()).isEqualTo(admin);
        verify(auditLogService).recordPaymentEntitlementCorrectionEvent(
                any(),
                any(PaymentEntitlementCorrection.class),
                org.mockito.ArgumentMatchers.eq(PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_APPROVED),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.REQUESTED),
                org.mockito.ArgumentMatchers.eq(PaymentEntitlementCorrectionStatus.APPROVED),
                org.mockito.ArgumentMatchers.eq("reviewed evidence"));
    }

    @Test
    @DisplayName("previewCorrection explains unsafe owner, plan, date, and no-op targets")
    void previewCorrectionExplainsInvalidTargets() {
        Fixture ownerMismatch = fixture(PaymentRefundStatus.SUCCEEDED);
        User otherUser = User.builder()
                .id(17L)
                .nickname("other")
                .email("other@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(ownerMismatch.userSubscription(), "user", otherUser);
        assertThat(previewReason(ownerMismatch, ownerMismatch.standard(), request(ownerMismatch.standard().getId())))
                .contains("owner do not match");

        Fixture wrongType = fixture(PaymentRefundStatus.SUCCEEDED);
        Subscription business = Subscription.builder()
                .id(40L)
                .name("BUSINESS")
                .userType(UserType.BUSINESS)
                .priceMonthly(BigDecimal.valueOf(49900))
                .priceYearly(BigDecimal.valueOf(499000))
                .downloadPerDay(100)
                .maxWhitelistChannels(20)
                .isActive(true)
                .build();
        assertThat(previewReason(wrongType, business, request(business.getId())))
                .contains("user type");

        Fixture inactiveTarget = fixture(PaymentRefundStatus.SUCCEEDED);
        Subscription inactive = Subscription.builder()
                .id(41L)
                .name("INACTIVE")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(3)
                .isActive(false)
                .build();
        assertThat(previewReason(inactiveTarget, inactive, request(inactive.getId())))
                .contains("inactive");

        Fixture expiredFuture = fixture(PaymentRefundStatus.SUCCEEDED);
        AdminPaymentEntitlementCorrectionRequest expiredFutureRequest = new AdminPaymentEntitlementCorrectionRequest(
                77L,
                expiredFuture.standard().getId(),
                BillingCycle.MONTHLY,
                SubscriptionStatus.EXPIRED,
                LocalDate.now().plusDays(1),
                true,
                true,
                "future expiry");
        assertThat(previewReason(expiredFuture, expiredFuture.standard(), expiredFutureRequest))
                .contains("must not have a future");

        Fixture activePast = fixture(PaymentRefundStatus.SUCCEEDED);
        AdminPaymentEntitlementCorrectionRequest activePastRequest = new AdminPaymentEntitlementCorrectionRequest(
                77L,
                activePast.standard().getId(),
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE,
                LocalDate.now().minusDays(1),
                true,
                false,
                "past active");
        assertThat(previewReason(activePast, activePast.standard(), activePastRequest))
                .contains("must not have a past");

        Fixture noOp = fixture(PaymentRefundStatus.SUCCEEDED);
        AdminPaymentEntitlementCorrectionRequest noOpRequest = new AdminPaymentEntitlementCorrectionRequest(
                77L,
                noOp.premium().getId(),
                BillingCycle.YEARLY,
                SubscriptionStatus.ACTIVE,
                LocalDate.of(2027, 5, 25),
                false,
                false,
                "unchanged");
        assertThat(previewReason(noOp, noOp.premium(), noOpRequest))
                .contains("identical");
    }

    @Test
    @DisplayName("previewCorrection reports a refund that is not finalized")
    void previewCorrectionReportsPendingRefund() {
        Fixture pending = fixture(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);

        assertThat(previewReason(pending, pending.standard(), request(pending.standard().getId())))
                .contains("Only succeeded refund");
    }

    private String previewReason(
            Fixture fixture,
            Subscription target,
            AdminPaymentEntitlementCorrectionRequest request) {
        given(paymentRefundRepository.findWithGraphById(77L)).willReturn(Optional.of(fixture.refund()));
        given(subscriptionRepository.findById(target.getId())).willReturn(Optional.of(target));
        given(billingAgreementRepository.findByUserAndProvider(
                fixture.user(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        return service.previewCorrection(request).getData().reason();
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
                .provider(PaymentProviderType.TOSS)
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
        ReflectionTestUtils.setField(correction, "createdAt", CORRECTION_CREATED_AT);
        ReflectionTestUtils.setField(correction, "updatedAt", CORRECTION_CREATED_AT);
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
                .provider(PaymentProviderType.TOSS)
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
                .provider(PaymentProviderType.TOSS)
                .amount(BigDecimal.valueOf(299000))
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key")
                .build();
        PaymentRefund refund = PaymentRefund.builder()
                .subscriptionPayment(payment)
                .paymentOrder(order)
                .user(user)
                .provider(PaymentProviderType.TOSS)
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
                .provider(PaymentProviderType.TOSS)
                .status(BillingAgreementStatus.ACTIVE)
                .providerCustomerKey("customer_key")
                .billingKeyCiphertext("encrypted")
                .billingKeyFingerprint("fingerprint")
                .nextBillingAt(LocalDate.of(2027, 5, 25))
                .build();
        ReflectionTestUtils.setField(
                agreement,
                "createdAt",
                AGREEMENT_UPDATED_BEFORE_CORRECTION.minusDays(1));
        ReflectionTestUtils.setField(
                agreement,
                "updatedAt",
                AGREEMENT_UPDATED_BEFORE_CORRECTION);
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
