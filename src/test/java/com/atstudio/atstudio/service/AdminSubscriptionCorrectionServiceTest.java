package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionApproveRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionRequest;
import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.AdminSubscriptionCorrectionRepository;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Administrator local subscription correction service")
class AdminSubscriptionCorrectionServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 8);
    private static final LocalDateTime CORRECTION_CREATED_AT = LocalDateTime.of(2026, 8, 8, 10, 0);
    private static final String ADVERSARIAL_OPERATOR_NOTE =
            "contact=billing@example.test authorization=Bearer eyJhbGciOiJub25lIn0.fake.signature";
    private static final Set<AdminSubscriptionCorrectionStatus> NON_TERMINAL_STATUSES = Set.of(
            AdminSubscriptionCorrectionStatus.REQUESTED,
            AdminSubscriptionCorrectionStatus.APPROVED,
            AdminSubscriptionCorrectionStatus.PROCESSING);
    private static final Clock BUSINESS_CLOCK = Clock.fixed(
            Instant.parse("2026-08-08T01:30:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock AdminSubscriptionCorrectionRepository correctionRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock UserRepository userRepository;
    @Mock AdminOperationAuditService auditService;
    @Mock AdminOperationRejectionAuditService rejectionAuditService;

    private AdminSubscriptionCorrectionService service;

    @BeforeEach
    void setUp() {
        service = new AdminSubscriptionCorrectionService(
                correctionRepository,
                billingAgreementRepository,
                userSubscriptionRepository,
                subscriptionRepository,
                paymentOrderRepository,
                userRepository,
                auditService,
                rejectionAuditService,
                BUSINESS_CLOCK);
    }

    @ParameterizedTest(name = "{0} with expiresAt {1} executable={2}")
    @MethodSource("dateMatrix")
    @DisplayName("preview enforces the Asia/Seoul status and expiration matrix")
    void previewEnforcesDateMatrix(
            SubscriptionStatus status,
            LocalDate expiresAt,
            boolean executable) {
        Fixture fixture = fixture();
        stubPreview(fixture);

        var result = service.previewCorrection(
                actorDetails(),
                request(fixture, status, expiresAt, true, false, "approved support case"));

        assertThat(result.getData().executable()).isEqualTo(executable);
        assertThat(result.getData().externalPaymentExecuted()).isFalse();
        if (executable) {
            assertThat(result.getData().reason()).isNull();
        } else {
            assertThat(result.getData().reason()).isNotBlank();
        }
    }

    private static Stream<Arguments> dateMatrix() {
        return Stream.of(
                Arguments.of(SubscriptionStatus.ACTIVE, TODAY, true),
                Arguments.of(SubscriptionStatus.CANCELLED, TODAY, true),
                Arguments.of(SubscriptionStatus.EXPIRED, TODAY, true),
                Arguments.of(SubscriptionStatus.ACTIVE, TODAY.minusDays(1), false),
                Arguments.of(SubscriptionStatus.CANCELLED, TODAY.minusDays(1), false),
                Arguments.of(SubscriptionStatus.EXPIRED, TODAY.plusDays(1), false));
    }

    @Test
    @DisplayName("preview rejects inactive and user-type-mismatched target plans")
    void previewRejectsInvalidTargetPlans() {
        Fixture fixture = fixture();
        Subscription inactive = plan(4L, "INACTIVE", UserType.INDIVIDUAL, false);
        Subscription business = plan(5L, "BUSINESS", UserType.BUSINESS, true);
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(subscriptionRepository.findById(inactive.getId())).willReturn(Optional.of(inactive));

        var inactivePreview = service.previewCorrection(
                actorDetails(),
                new AdminSubscriptionCorrectionRequest(
                        fixture.userSubscription().getId(), inactive.getId(), BillingCycle.MONTHLY,
                        SubscriptionStatus.CANCELLED, TODAY, true, false, "inactive target"));

        given(subscriptionRepository.findById(business.getId())).willReturn(Optional.of(business));
        var businessPreview = service.previewCorrection(
                actorDetails(),
                new AdminSubscriptionCorrectionRequest(
                        fixture.userSubscription().getId(), business.getId(), BillingCycle.MONTHLY,
                        SubscriptionStatus.CANCELLED, TODAY, true, false, "wrong type"));

        assertThat(inactivePreview.getData().executable()).isFalse();
        assertThat(inactivePreview.getData().reason()).contains("inactive");
        assertThat(businessPreview.getData().executable()).isFalse();
        assertThat(businessPreview.getData().reason()).contains("type");
    }

    @Test
    @DisplayName("preview preserves an already expired local agreement")
    void previewPreservesExpiredAgreementStatus() {
        Fixture fixture = fixture();
        fixture.agreement().expire();
        stubPreview(fixture);

        var result = service.previewCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, true, "support case"));

        assertThat(result.getData().currentBillingAgreementStatus())
                .isEqualTo(BillingAgreementStatus.EXPIRED);
        assertThat(result.getData().targetBillingAgreementStatus())
                .isEqualTo(BillingAgreementStatus.EXPIRED);
    }

    @Test
    @DisplayName("preview rejects no-op and invalid operator reasons")
    void previewRejectsNoOpAndInvalidReason() {
        Fixture fixture = fixture();
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(subscriptionRepository.findById(fixture.premium().getId()))
                .willReturn(Optional.of(fixture.premium()));
        given(subscriptionRepository.findById(fixture.standard().getId()))
                .willReturn(Optional.of(fixture.standard()));

        var noOp = service.previewCorrection(
                actorDetails(),
                new AdminSubscriptionCorrectionRequest(
                        fixture.userSubscription().getId(), fixture.premium().getId(), BillingCycle.YEARLY,
                        SubscriptionStatus.ACTIVE, TODAY.plusMonths(1), false, false, "same state"));
        var blank = service.previewCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "   "));
        var tooLong = service.previewCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "x".repeat(501)));

        assertThat(noOp.getData().reason()).contains("does not change");
        assertThat(blank.getData().reason()).contains("nonblank");
        assertThat(tooLong.getData().reason()).contains("500");
    }

    @ParameterizedTest
    @EnumSource(
            value = AdminSubscriptionCorrectionStatus.class,
            names = {"REQUESTED", "APPROVED", "PROCESSING"})
    @DisplayName("open workflow lookup returns every non-terminal status")
    void openCorrectionReturnsNonTerminalStatus(AdminSubscriptionCorrectionStatus status) {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(fixture, status, true, false);
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(correctionRepository
                .findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(
                        fixture.userSubscription().getId(), NON_TERMINAL_STATUSES))
                .willReturn(Optional.of(correction));

        var result = service.getOpenCorrection(
                actorDetails(), fixture.userSubscription().getId());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getData().status()).isEqualTo(status);
        InOrder queryOrder = inOrder(userSubscriptionRepository, correctionRepository);
        queryOrder.verify(userSubscriptionRepository).findById(fixture.userSubscription().getId());
        queryOrder.verify(correctionRepository)
                .findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(
                        fixture.userSubscription().getId(), NON_TERMINAL_STATUSES);
    }

    @Test
    @DisplayName("open workflow lookup is empty when only terminal corrections exist")
    void openCorrectionIsEmptyForTerminalHistory() {
        Fixture fixture = fixture();
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(correctionRepository
                .findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(
                        fixture.userSubscription().getId(), NON_TERMINAL_STATUSES))
                .willReturn(Optional.empty());

        assertThat(service.getOpenCorrection(
                actorDetails(), fixture.userSubscription().getId())).isEmpty();
    }

    @Test
    @DisplayName("open workflow lookup distinguishes an unknown user subscription")
    void openCorrectionRejectsUnknownUserSubscription() {
        given(userSubscriptionRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOpenCorrection(actorDetails(), 404L))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        verify(correctionRepository, never())
                .findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(anyLong(), any());
    }

    @Test
    @DisplayName("request uses the canonical lock order and persists the before snapshot")
    void requestPersistsSnapshotWithDeterministicLockOrder() {
        Fixture fixture = fixture();
        stubRequest(fixture);
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(fixture.admin()));
        given(correctionRepository.save(any())).willAnswer(invocation -> {
            AdminSubscriptionCorrection correction = invocation.getArgument(0);
            ReflectionTestUtils.setField(correction, "id", 88L);
            ReflectionTestUtils.setField(correction, "createdAt", CORRECTION_CREATED_AT);
            ReflectionTestUtils.setField(correction, "updatedAt", CORRECTION_CREATED_AT);
            return correction;
        });

        var result = service.requestCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "  support case  "));

        assertThat(result.getData().id()).isEqualTo(88L);
        assertThat(result.getData().beforeSubscriptionId()).isEqualTo(fixture.premium().getId());
        assertThat(result.getData().beforePendingSubscriptionId()).isEqualTo(fixture.standard().getId());
        assertThat(result.getData().reasonNote()).isEqualTo("support case");
        InOrder lockOrder = inOrder(
                billingAgreementRepository,
                userSubscriptionRepository,
                subscriptionRepository,
                userRepository,
                correctionRepository);
        lockOrder.verify(billingAgreementRepository).findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS);
        lockOrder.verify(userSubscriptionRepository).findByIdForUpdate(fixture.userSubscription().getId());
        lockOrder.verify(subscriptionRepository).findByIdForUpdate(fixture.standard().getId());
        lockOrder.verify(correctionRepository).findNonTerminalByUserSubscriptionIDForUpdate(
                fixture.userSubscription().getId(), NON_TERMINAL_STATUSES);
        lockOrder.verify(userRepository).findByIdForUpdate(fixture.admin().getId());
        lockOrder.verify(correctionRepository).save(any(AdminSubscriptionCorrection.class));
    }

    @Test
    @DisplayName("request rejects a second non-terminal correction")
    void requestRejectsNonTerminalDuplicate() {
        Fixture fixture = fixture();
        stubRequest(fixture);
        given(correctionRepository.findNonTerminalByUserSubscriptionIDForUpdate(anyLong(), any()))
                .willReturn(List.of(correction(
                        fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false)));

        assertThatThrownBy(() -> service.requestCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "duplicate")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));

        verify(rejectionAuditService).recordUserSubscriptionCorrectionRequestRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                org.mockito.ArgumentMatchers.argThat(state -> state.contains("\"status\":\"ACTIVE\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.RESOURCE_DUPLICATE));
        verify(userRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("request rechecks the write-locked actor after domain and correction locks")
    void requestRejectsActorDemotedBeforeMutation() {
        Fixture fixture = fixture();
        stubRequest(fixture);
        User demotedActor = user(
                fixture.admin().getId(),
                fixture.admin().getNickname(),
                fixture.admin().getEmail(),
                UserRole.USER);
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(demotedActor));

        assertThatThrownBy(() -> service.requestCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "demoted")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        InOrder lockOrder = inOrder(correctionRepository, userRepository);
        lockOrder.verify(correctionRepository).findNonTerminalByUserSubscriptionIDForUpdate(
                fixture.userSubscription().getId(), NON_TERMINAL_STATUSES);
        lockOrder.verify(userRepository).findByIdForUpdate(fixture.admin().getId());
        verify(correctionRepository, never()).save(any());
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRequestRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                any(),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));
    }

    @Test
    @DisplayName("request rejection audit failure preserves the original business error")
    void requestRejectionAuditFailurePreservesOriginalBusinessError() {
        Fixture fixture = fixture();
        stubRequest(fixture);
        given(correctionRepository.findNonTerminalByUserSubscriptionIDForUpdate(anyLong(), any()))
                .willReturn(List.of(correction(
                        fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false)));
        doThrow(new IllegalStateException("audit unavailable"))
                .when(rejectionAuditService)
                .recordUserSubscriptionCorrectionRequestRejected(any(), any(), any(), any());

        assertThatThrownBy(() -> service.requestCorrection(
                actorDetails(),
                request(fixture, SubscriptionStatus.CANCELLED, TODAY, true, false, "duplicate")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> {
                    BusinessException businessException = (BusinessException) error;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE);
                    assertThat(businessException.getSuppressed())
                            .singleElement()
                            .isInstanceOf(IllegalStateException.class);
                });
    }

    @Test
    @DisplayName("approve is an explicit single-operator transition and does not require actor separation")
    void approveAllowsTheRequestingOperator() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false);
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.of(fixture.admin()));

        var result = service.approveCorrection(
                88L,
                actorDetails(),
                new AdminSubscriptionCorrectionApproveRequest("  explicit confirmation  "));

        assertThat(result.getData().status()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        assertThat(correction.getApprovedBy()).isSameAs(fixture.admin());
        assertThat(result.getData().approvalNote()).isEqualTo("explicit confirmation");
        InOrder lockOrder = inOrder(correctionRepository, userRepository);
        lockOrder.verify(correctionRepository).findByIDForUpdate(88L);
        lockOrder.verify(userRepository).findByIdForUpdate(99L);
    }

    @Test
    @DisplayName("approve rejects an oversized workflow note before transition")
    void approveRejectsOversizedNote() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false);
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));
        assertThatThrownBy(() -> service.approveCorrection(
                88L,
                actorDetails(),
                new AdminSubscriptionCorrectionApproveRequest("x".repeat(501))))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.REQUESTED);
        assertThat(correction.getApprovalNote()).isNull();
        verify(rejectionAuditService).recordUserSubscriptionCorrectionApprovalRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(88L),
                org.mockito.ArgumentMatchers.argThat(state -> state.contains(
                        "\"correctionStatus\":\"REQUESTED\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.INVALID_ARGUMENT));
        verify(userRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("approve rechecks the write-locked actor immediately before transition")
    void approveRejectsActorDemotedBeforeTransition() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false);
        User demotedActor = user(
                fixture.admin().getId(),
                fixture.admin().getNickname(),
                fixture.admin().getEmail(),
                UserRole.USER);
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(demotedActor));

        assertThatThrownBy(() -> service.approveCorrection(
                88L,
                actorDetails(),
                new AdminSubscriptionCorrectionApproveRequest("confirmed")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.REQUESTED);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionApprovalRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(88L),
                org.mockito.ArgumentMatchers.argThat(state -> state.contains(
                        "\"correctionStatus\":\"REQUESTED\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));
    }

    @Test
    @DisplayName("approval rejection audit failure preserves the original business error")
    void approvalRejectionAuditFailurePreservesOriginalBusinessError() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));
        doThrow(new IllegalStateException("audit unavailable"))
                .when(rejectionAuditService)
                .recordUserSubscriptionCorrectionApprovalRejected(any(), any(), any(), any());

        assertThatThrownBy(() -> service.approveCorrection(
                88L,
                actorDetails(),
                new AdminSubscriptionCorrectionApproveRequest("too late")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> {
                    BusinessException businessException = (BusinessException) error;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
                    assertThat(businessException.getSuppressed())
                            .singleElement()
                            .isInstanceOf(IllegalStateException.class);
                });
    }

    @Test
    @DisplayName("execute can keep pending state and the local billing agreement")
    void executeKeepsPendingAndAgreement() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, false, false);
        stubExecution(fixture, correction);
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(fixture.admin()));

        service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("  apply  "));

        assertThat(fixture.userSubscription().getPendingSubscription()).isSameAs(fixture.standard());
        assertThat(fixture.userSubscription().getPendingBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.SUCCEEDED);
        assertThat(correction.getExecutionNote()).isEqualTo("apply");
        assertThat(correction.getReasonNote()).isEqualTo(ADVERSARIAL_OPERATOR_NOTE);
        verify(auditService).recordUserSubscriptionCorrectionSuccess(
                fixture.admin().getId(), correction, fixture.userSubscription());
        InOrder lockOrder = inOrder(
                correctionRepository,
                billingAgreementRepository,
                userSubscriptionRepository,
                subscriptionRepository,
                userRepository);
        lockOrder.verify(correctionRepository).findExecutionLockProjectionByID(88L);
        lockOrder.verify(billingAgreementRepository).findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS);
        lockOrder.verify(userSubscriptionRepository).findByIdForUpdate(fixture.userSubscription().getId());
        lockOrder.verify(subscriptionRepository).findByIdForUpdate(fixture.standard().getId());
        lockOrder.verify(correctionRepository).findByIDForUpdate(88L);
        lockOrder.verify(userRepository).findByIdForUpdate(fixture.admin().getId());
    }

    @Test
    @DisplayName("execute rechecks the write-locked actor immediately before local mutation")
    void executeRejectsActorDemotedBeforeMutation() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, true);
        User demotedActor = user(
                fixture.admin().getId(),
                fixture.admin().getNickname(),
                fixture.admin().getEmail(),
                UserRole.USER);
        stubExecution(fixture, correction);
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(demotedActor));

        assertThatThrownBy(() -> service.executeCorrection(
                88L,
                actorDetails(),
                new AdminSubscriptionCorrectionExecuteRequest("apply")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        assertThat(fixture.userSubscription().getSubscription()).isSameAs(fixture.premium());
        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                any(),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED),
                org.mockito.ArgumentMatchers.isNull());
        verify(auditService, never()).recordUserSubscriptionCorrectionSuccess(anyLong(), any(), any());
    }

    @Test
    @DisplayName("execute clears pending state and cancels only the local agreement")
    void executeClearsPendingAndCancelsLocalAgreement() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, true);
        stubExecution(fixture, correction);
        given(userRepository.findByIdForUpdate(fixture.admin().getId()))
                .willReturn(Optional.of(fixture.admin()));

        var first = service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("apply"));
        var second = service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("retry"));

        assertThat(first.getData().status()).isEqualTo(AdminSubscriptionCorrectionStatus.SUCCEEDED);
        assertThat(second.getData().status()).isEqualTo(AdminSubscriptionCorrectionStatus.SUCCEEDED);
        assertThat(fixture.userSubscription().getPendingSubscription()).isNull();
        assertThat(fixture.userSubscription().getPendingBillingCycle()).isNull();
        assertThat(fixture.agreement().getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(fixture.agreement().getBillingKeyCiphertext()).isEqualTo("encrypted");
        assertThat(second.getData().executionNote()).isEqualTo("apply");
        verify(correctionRepository, times(2)).findExecutionLockProjectionByID(88L);
        verify(billingAgreementRepository, times(2)).findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS);
        verify(userSubscriptionRepository, times(2)).findByIdForUpdate(fixture.userSubscription().getId());
        verify(subscriptionRepository, times(2)).findByIdForUpdate(fixture.standard().getId());
        verify(correctionRepository, times(2)).findByIDForUpdate(88L);
        verify(userRepository, times(1)).findByIdForUpdate(fixture.admin().getId());
        verify(auditService, times(1)).recordUserSubscriptionCorrectionSuccess(
                fixture.admin().getId(), correction, fixture.userSubscription());
    }

    @Test
    @DisplayName("execute rejects a stale subscription snapshot and persists a minimal rejection audit")
    void executeRejectsStaleSnapshot() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        fixture.userSubscription().adminUpdate(SubscriptionStatus.CANCELLED, null, null);
        stubExecution(fixture, correction);

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest(ADVERSARIAL_OPERATOR_NOTE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        assertThat(correction.getReasonNote()).isEqualTo(ADVERSARIAL_OPERATOR_NOTE);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                org.mockito.ArgumentMatchers.argThat(
                        state -> state.contains("\"billingAgreementStatus\":\"ACTIVE\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.INVALID_STATE_TRANSITION),
                org.mockito.ArgumentMatchers.isNull());
        verify(auditService, never()).recordUserSubscriptionCorrectionSuccess(anyLong(), any(), any());
    }

    @Test
    @DisplayName("an early execution rejection records the correction agreement status when available")
    void executeBeforeApprovalRecordsAvailableAgreementStatus() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.REQUESTED, true, false);
        stubExecution(fixture, correction);

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("too early")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                org.mockito.ArgumentMatchers.argThat(
                        state -> state.contains("\"billingAgreementStatus\":\"ACTIVE\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.INVALID_STATE_TRANSITION),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("execute rejects a stale local billing-agreement snapshot")
    void executeRejectsStaleAgreementSnapshot() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        fixture.agreement().suspend();
        stubExecution(fixture, correction);

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("stale agreement")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                org.mockito.ArgumentMatchers.argThat(
                        state -> state.contains("\"billingAgreementStatus\":\"SUSPENDED\"")),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.INVALID_STATE_TRANSITION),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("execute blocks an order that can still receive a provider outcome")
    void executeBlocksPendingProviderOutcome() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        stubExecution(fixture, correction);
        given(paymentOrderRepository.existsByBillingAgreementAndPurposeInAndStatusIn(any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("pending")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                any(),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE),
                org.mockito.ArgumentMatchers.isNull());
        verify(userRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("execute revalidates the write-locked target plan before mutation")
    void executeRejectsTargetPlanDeactivatedAfterRequest() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        ReflectionTestUtils.setField(fixture.standard(), "isActive", false);
        stubExecution(fixture, correction);

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("stale plan")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        assertThat(correction.getStatus()).isEqualTo(AdminSubscriptionCorrectionStatus.APPROVED);
        assertThat(fixture.userSubscription().getSubscription()).isSameAs(fixture.premium());
        verify(auditService, never()).recordUserSubscriptionCorrectionSuccess(anyLong(), any(), any());
    }

    @Test
    @DisplayName("execute rejects correction IDs that changed between observation and the final lock")
    void executeRevalidatesObservedCorrectionIDsAfterLocking() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        AdminSubscriptionCorrectionRepository.ExecutionLockProjection observed =
                executionLockProjection(correction);
        ReflectionTestUtils.setField(
                correction,
                "targetSubscription",
                plan(4L, "DRIFTED", UserType.INDIVIDUAL, true));
        given(correctionRepository.findExecutionLockProjectionByID(88L))
                .willReturn(Optional.of(observed));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(subscriptionRepository.findByIdForUpdate(fixture.standard().getId()))
                .willReturn(Optional.of(fixture.standard()));
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest("drifted IDs")))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        verify(auditService, never()).recordUserSubscriptionCorrectionSuccess(anyLong(), any(), any());
    }

    @Test
    @DisplayName("rejection audit failure never masks the original business error")
    void rejectionAuditFailurePreservesOriginalBusinessError() {
        Fixture fixture = fixture();
        AdminSubscriptionCorrection correction = correction(
                fixture, AdminSubscriptionCorrectionStatus.APPROVED, true, false);
        fixture.userSubscription().adminUpdate(SubscriptionStatus.CANCELLED, null, null);
        stubExecution(fixture, correction);
        doThrow(new IllegalStateException("audit unavailable"))
                .when(rejectionAuditService)
                .recordUserSubscriptionCorrectionRejected(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.executeCorrection(
                88L, actorDetails(), new AdminSubscriptionCorrectionExecuteRequest(ADVERSARIAL_OPERATOR_NOTE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> {
                    BusinessException businessException = (BusinessException) error;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
                    assertThat(businessException.getSuppressed())
                            .singleElement()
                            .isInstanceOf(IllegalStateException.class);
                });
        assertThat(correction.getReasonNote()).isEqualTo(ADVERSARIAL_OPERATOR_NOTE);
        verify(rejectionAuditService).recordUserSubscriptionCorrectionRejected(
                org.mockito.ArgumentMatchers.eq(fixture.admin().getId()),
                org.mockito.ArgumentMatchers.eq(fixture.userSubscription().getId()),
                any(),
                org.mockito.ArgumentMatchers.eq(BUSINESS_ERROR.INVALID_STATE_TRANSITION),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("correction execution is serialized with a pessimistic row lock")
    void correctionExecutionUsesPessimisticWriteLock() throws Exception {
        Lock lock = AdminSubscriptionCorrectionRepository.class
                .getMethod("findByIDForUpdate", Long.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("execute observes only lock-target IDs without locking the correction entity")
    void executionLockTargetObservationIsNonLocking() throws Exception {
        Lock lock = AdminSubscriptionCorrectionRepository.class
                .getMethod("findExecutionLockProjectionByID", Long.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNull();
    }

    @Test
    @DisplayName("duplicate request detection is a pessimistic current read")
    void nonTerminalDuplicateQueryUsesPessimisticWriteLock() throws Exception {
        Lock lock = AdminSubscriptionCorrectionRepository.class
                .getMethod(
                        "findNonTerminalByUserSubscriptionIDForUpdate",
                        Long.class,
                        Set.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("target plan validation uses a pessimistic write lock")
    void targetPlanValidationUsesPessimisticWriteLock() throws Exception {
        Lock lock = SubscriptionRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("correction actors share the pessimistic user-row lock contract")
    void correctionActorUsesSharedPessimisticWriteLock() throws Exception {
        Lock lock = UserRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    private void stubPreview(Fixture fixture) {
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(subscriptionRepository.findById(fixture.standard().getId()))
                .willReturn(Optional.of(fixture.standard()));
        given(billingAgreementRepository.findByUserAndProvider(fixture.user(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
    }

    private void stubRequest(Fixture fixture) {
        given(userSubscriptionRepository.findById(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(subscriptionRepository.findByIdForUpdate(fixture.standard().getId()))
                .willReturn(Optional.of(fixture.standard()));
    }

    private void stubExecution(Fixture fixture, AdminSubscriptionCorrection correction) {
        given(correctionRepository.findExecutionLockProjectionByID(88L))
                .willReturn(Optional.of(executionLockProjection(correction)));
        given(correctionRepository.findByIDForUpdate(88L)).willReturn(Optional.of(correction));
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(
                fixture.user().getId(), PaymentProviderType.TOSS))
                .willReturn(Optional.of(fixture.agreement()));
        given(userSubscriptionRepository.findByIdForUpdate(fixture.userSubscription().getId()))
                .willReturn(Optional.of(fixture.userSubscription()));
        given(subscriptionRepository.findByIdForUpdate(fixture.standard().getId()))
                .willReturn(Optional.of(fixture.standard()));
    }

    private AdminSubscriptionCorrectionRepository.ExecutionLockProjection executionLockProjection(
            AdminSubscriptionCorrection correction) {
        Long correctionID = correction.getId();
        Long userID = correction.getUser().getId();
        Long userSubscriptionID = correction.getUserSubscription().getId();
        Long targetSubscriptionID = correction.getTargetSubscription().getId();
        Long billingAgreementID = correction.getBillingAgreement() == null
                ? null : correction.getBillingAgreement().getId();
        return new AdminSubscriptionCorrectionRepository.ExecutionLockProjection() {
            @Override
            public Long getCorrectionID() {
                return correctionID;
            }

            @Override
            public Long getUserID() {
                return userID;
            }

            @Override
            public Long getUserSubscriptionID() {
                return userSubscriptionID;
            }

            @Override
            public Long getTargetSubscriptionID() {
                return targetSubscriptionID;
            }

            @Override
            public Long getBillingAgreementID() {
                return billingAgreementID;
            }
        };
    }

    private AdminSubscriptionCorrectionRequest request(
            Fixture fixture,
            SubscriptionStatus status,
            LocalDate expiresAt,
            boolean clearPending,
            boolean cancelAgreement,
            String reason) {
        return new AdminSubscriptionCorrectionRequest(
                fixture.userSubscription().getId(),
                fixture.standard().getId(),
                BillingCycle.MONTHLY,
                status,
                expiresAt,
                clearPending,
                cancelAgreement,
                reason);
    }

    private AdminSubscriptionCorrection correction(
            Fixture fixture,
            AdminSubscriptionCorrectionStatus status,
            boolean clearPending,
            boolean cancelAgreement) {
        AdminSubscriptionCorrection correction = AdminSubscriptionCorrection.builder()
                .id(88L)
                .userSubscription(fixture.userSubscription())
                .user(fixture.user())
                .billingAgreement(fixture.agreement())
                .status(status)
                .beforeSubscription(fixture.premium())
                .beforeBillingCycle(BillingCycle.YEARLY)
                .beforeStatus(SubscriptionStatus.ACTIVE)
                .beforeExpiresAt(TODAY.plusMonths(1))
                .beforePendingSubscription(fixture.standard())
                .beforePendingBillingCycle(BillingCycle.MONTHLY)
                .targetSubscription(fixture.standard())
                .targetBillingCycle(BillingCycle.MONTHLY)
                .targetStatus(SubscriptionStatus.CANCELLED)
                .targetExpiresAt(TODAY)
                .clearPendingChange(clearPending)
                .cancelBillingAgreement(cancelAgreement)
                .beforeBillingAgreementStatus(BillingAgreementStatus.ACTIVE)
                .afterBillingAgreementStatus(BillingAgreementStatus.ACTIVE)
                .reasonNote(ADVERSARIAL_OPERATOR_NOTE)
                .requestedBy(fixture.admin())
                .approvedBy(fixture.admin())
                .approvedAt(CORRECTION_CREATED_AT.plusMinutes(1))
                .build();
        ReflectionTestUtils.setField(correction, "createdAt", CORRECTION_CREATED_AT);
        ReflectionTestUtils.setField(correction, "updatedAt", CORRECTION_CREATED_AT);
        return correction;
    }

    private Fixture fixture() {
        User user = User.builder()
                .id(16L)
                .nickname("buyer")
                .email("buyer@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        User admin = User.builder()
                .id(99L)
                .nickname("admin")
                .email("admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        Subscription standard = plan(1L, "STANDARD", UserType.INDIVIDUAL, true);
        Subscription premium = plan(3L, "PREMIUM", UserType.INDIVIDUAL, true);
        UserSubscription userSubscription = UserSubscription.builder()
                .id(20L)
                .user(user)
                .subscription(premium)
                .billingCycle(BillingCycle.YEARLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(TODAY.minusMonths(1))
                .expiresAt(TODAY.plusMonths(1))
                .pendingSubscription(standard)
                .pendingBillingCycle(BillingCycle.MONTHLY)
                .build();
        BillingAgreement agreement = BillingAgreement.builder()
                .id(7L)
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .status(BillingAgreementStatus.ACTIVE)
                .providerCustomerKey("customer_key")
                .billingKeyCiphertext("encrypted")
                .billingKeyFingerprint("fingerprint")
                .nextBillingAt(TODAY.plusMonths(1))
                .build();
        ReflectionTestUtils.setField(agreement, "createdAt", CORRECTION_CREATED_AT.minusDays(2));
        ReflectionTestUtils.setField(agreement, "updatedAt", CORRECTION_CREATED_AT.minusDays(1));
        return new Fixture(user, admin, standard, premium, userSubscription, agreement);
    }

    private Subscription plan(Long id, String name, UserType userType, boolean active) {
        return Subscription.builder()
                .id(id)
                .name(name)
                .userType(userType)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(3)
                .isActive(active)
                .build();
    }

    private User user(Long id, String nickname, String email, UserRole role) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .userType(UserType.INDIVIDUAL)
                .role(role)
                .build();
    }

    private CustomUserDetails actorDetails() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }

    private record Fixture(
            User user,
            User admin,
            Subscription standard,
            Subscription premium,
            UserSubscription userSubscription,
            BillingAgreement agreement) {
    }
}
