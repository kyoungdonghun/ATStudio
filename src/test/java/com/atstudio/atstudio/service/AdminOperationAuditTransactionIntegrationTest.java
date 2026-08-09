package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditAction;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.AdminOperationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        JpaConfig.class,
        AdminOperationAuditService.class,
        AdminOperationRejectionAuditService.class,
        AdminOperationAuditTransactionIntegrationTest.ProbeConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("관리자 작업 감사 트랜잭션 통합 테스트")
class AdminOperationAuditTransactionIntegrationTest {

    private static final String ADVERSARIAL_OPERATOR_NOTE =
            "contact=audit@example.test authorization=Bearer eyJhbGciOiJub25lIn0.fake.signature";

    @Autowired AdminOperationAuditLogRepository auditLogRepository;
    @Autowired AuditTransactionProbe auditTransactionProbe;

    @BeforeEach
    void clearAuditLogs() {
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("성공 감사는 outer transaction 커밋과 함께 저장")
    void successAudit_commitsWithOuterTransaction() {
        auditTransactionProbe.recordSuccessfulRoleChange();

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction()).isEqualTo(AdminOperationAuditAction.USER_ROLE_CHANGE);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.SUCCEEDED);
                    assertThat(audit.getActorUserId()).isEqualTo(10L);
                    assertThat(audit.getTargetId()).isEqualTo(20L);
                    assertThat(audit.getBeforeState())
                            .isEqualTo("{\"role\":\"ADMIN\",\"isDeleted\":false}");
                    assertThat(audit.getAfterState())
                            .isEqualTo("{\"role\":\"USER\",\"isDeleted\":false}");
                    assertThat(audit.getReasonNote()).isEqualTo("approved ticket 14");
                });
    }

    @Test
    @DisplayName("성공 감사는 outer transaction 실패 시 mutation과 함께 롤백")
    void successAudit_rollsBackWithOuterTransaction() {
        assertThatThrownBy(() -> auditTransactionProbe.recordSuccessfulRoleChangeThenFail())
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("거절 감사는 REQUIRES_NEW로 outer rollback 뒤에도 보존")
    void rejectionAudit_survivesOuterRollback() {
        assertThatThrownBy(() -> auditTransactionProbe.recordRejectedRoleChangeThenFail())
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @Test
    @DisplayName("self-demotion rejection audit records the stable reason code")
    void selfDemotionRejectionAudit_recordsStableReasonCode() {
        auditTransactionProbe.recordRejectedRoleChange(
                BUSINESS_ERROR.SELF_ADMIN_DEMOTION_FORBIDDEN);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction()).isEqualTo(AdminOperationAuditAction.USER_ROLE_CHANGE);
                    assertThat(audit.getTargetType()).isEqualTo(AdminOperationAuditTargetType.USER);
                    assertThat(audit.getTargetId()).isEqualTo(20L);
                    assertThat(audit.getActorUserId()).isEqualTo(10L);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.SELF_ADMIN_DEMOTION_FORBIDDEN.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @Test
    @DisplayName("admin withdrawal success audit stores the deleted-state transition")
    void adminWithdrawalSuccessAudit_recordsStateTransition() {
        auditTransactionProbe.recordSuccessfulAdminWithdrawal();

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction()).isEqualTo(AdminOperationAuditAction.ADMIN_WITHDRAWAL);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.SUCCEEDED);
                    assertThat(audit.getBeforeState())
                            .isEqualTo("{\"role\":\"ADMIN\",\"isDeleted\":false}");
                    assertThat(audit.getAfterState())
                            .isEqualTo("{\"role\":\"ADMIN\",\"isDeleted\":true}");
                });
    }

    @Test
    @DisplayName("last-admin withdrawal rejection audit records the stable reason code")
    void lastAdminWithdrawalRejectionAudit_recordsStableReasonCode() {
        auditTransactionProbe.recordRejectedAdminWithdrawal();

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction()).isEqualTo(AdminOperationAuditAction.ADMIN_WITHDRAWAL);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @Test
    @DisplayName("subscription correction success audit commits with the outer transaction")
    void subscriptionCorrectionSuccessAudit_commitsWithOuterTransaction() {
        auditTransactionProbe.recordSuccessfulSubscriptionCorrection(false);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction())
                            .isEqualTo(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.SUCCEEDED);
                    assertThat(audit.getTargetId()).isEqualTo(20L);
                    assertThat(audit.getBeforeState()).contains(
                            "\"subscriptionId\":1",
                            "\"status\":\"ACTIVE\"",
                            "\"pendingSubscriptionId\":2",
                            "\"billingAgreementStatus\":\"ACTIVE\"");
                    assertThat(audit.getAfterState()).contains(
                            "\"subscriptionId\":2",
                            "\"status\":\"CANCELLED\"",
                            "\"pendingSubscriptionId\":null",
                            "\"billingAgreementStatus\":\"CANCELLED\"");
                    assertThat(audit.getBeforeState()).doesNotContain(
                            "email", "nickname", "token", "billingKey", "providerCustomerKey");
                    assertThat(audit.getAfterState()).doesNotContain(
                            "email", "nickname", "token", "billingKey", "providerCustomerKey");
                    assertThat(audit.getReasonNote()).isEqualTo(ADVERSARIAL_OPERATOR_NOTE);
                });
    }

    @Test
    @DisplayName("subscription correction success audit rolls back with the outer transaction")
    void subscriptionCorrectionSuccessAudit_rollsBackWithOuterTransaction() {
        assertThatThrownBy(() -> auditTransactionProbe.recordSuccessfulSubscriptionCorrection(true))
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("subscription correction rejection audit survives the outer rollback")
    void subscriptionCorrectionRejectionAudit_survivesOuterRollback() {
        assertThatThrownBy(auditTransactionProbe::recordRejectedSubscriptionCorrectionThenFail)
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction())
                            .isEqualTo(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION);
                    assertThat(audit.getTargetType())
                            .isEqualTo(AdminOperationAuditTargetType.USER_SUBSCRIPTION);
                    assertThat(audit.getTargetId()).isEqualTo(20L);
                    assertThat(audit.getActorUserId()).isEqualTo(10L);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getBeforeState())
                            .contains("\"billingAgreementStatus\":\"SUSPENDED\"")
                            .doesNotContain("billingKey", "providerCustomerKey", "token");
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @Test
    @DisplayName("subscription correction request rejection audit survives the outer rollback")
    void subscriptionCorrectionRequestRejectionAudit_survivesOuterRollback() {
        assertThatThrownBy(auditTransactionProbe::recordRejectedSubscriptionCorrectionRequestThenFail)
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction())
                            .isEqualTo(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION_REQUEST);
                    assertThat(audit.getTargetType())
                            .isEqualTo(AdminOperationAuditTargetType.USER_SUBSCRIPTION);
                    assertThat(audit.getTargetId()).isEqualTo(20L);
                    assertThat(audit.getActorUserId()).isEqualTo(10L);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getBeforeState())
                            .contains("\"status\":\"ACTIVE\"")
                            .doesNotContain("email", "token", "billingKey", "providerCustomerKey");
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @Test
    @DisplayName("subscription correction approval rejection audit survives the outer rollback")
    void subscriptionCorrectionApprovalRejectionAudit_survivesOuterRollback() {
        assertThatThrownBy(auditTransactionProbe::recordRejectedSubscriptionCorrectionApprovalThenFail)
                .isInstanceOf(IllegalStateException.class);

        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getAction())
                            .isEqualTo(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION_APPROVAL);
                    assertThat(audit.getTargetType())
                            .isEqualTo(AdminOperationAuditTargetType.ADMIN_SUBSCRIPTION_CORRECTION);
                    assertThat(audit.getTargetId()).isEqualTo(40L);
                    assertThat(audit.getActorUserId()).isEqualTo(10L);
                    assertThat(audit.getOutcome()).isEqualTo(AdminOperationAuditOutcome.REJECTED);
                    assertThat(audit.getReasonCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION.name());
                    assertThat(audit.getBeforeState()).isEqualTo(audit.getAfterState());
                    assertThat(audit.getBeforeState()).isEqualTo(
                            "{\"userSubscriptionId\":20,\"correctionStatus\":\"REQUESTED\"}");
                    assertThat(audit.getReasonNote()).isNull();
                });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProbeConfiguration {

        @Bean
        AuditTransactionProbe auditTransactionProbe(
                AdminOperationAuditService successAuditService,
                AdminOperationRejectionAuditService rejectionAuditService) {
            return new AuditTransactionProbe(successAuditService, rejectionAuditService);
        }
    }

    static class AuditTransactionProbe {

        private final AdminOperationAuditService successAuditService;
        private final AdminOperationRejectionAuditService rejectionAuditService;

        AuditTransactionProbe(
                AdminOperationAuditService successAuditService,
                AdminOperationRejectionAuditService rejectionAuditService) {
            this.successAuditService = successAuditService;
            this.rejectionAuditService = rejectionAuditService;
        }

        @Transactional
        public void recordSuccessfulRoleChange() {
            successAuditService.recordRoleChangeSuccess(
                    10L,
                    20L,
                    UserRole.ADMIN,
                    UserRole.USER,
                    false,
                    "approved ticket 14");
        }

        @Transactional
        public void recordSuccessfulRoleChangeThenFail() {
            recordSuccessfulRoleChange();
            throw new IllegalStateException("force outer rollback");
        }

        @Transactional
        public void recordRejectedRoleChangeThenFail() {
            rejectionAuditService.recordRoleChangeRejected(
                    10L,
                    20L,
                    UserRole.ADMIN,
                    false,
                    BUSINESS_ERROR.LAST_ADMIN_REQUIRED,
                    null);
            throw new IllegalStateException("force outer rollback");
        }

        @Transactional
        public void recordRejectedRoleChange(BUSINESS_ERROR reason) {
            rejectionAuditService.recordRoleChangeRejected(
                    10L, 20L, UserRole.ADMIN, false, reason, null);
        }

        @Transactional
        public void recordSuccessfulAdminWithdrawal() {
            successAuditService.recordAdminWithdrawalSuccess(20L, UserRole.ADMIN);
        }

        @Transactional
        public void recordRejectedAdminWithdrawal() {
            rejectionAuditService.recordAdminWithdrawalRejected(
                    20L, UserRole.ADMIN, false, BUSINESS_ERROR.LAST_ADMIN_REQUIRED);
        }

        @Transactional
        public void recordSuccessfulSubscriptionCorrection(boolean failAfterAudit) {
            SubscriptionAuditFixture fixture = subscriptionAuditFixture();
            successAuditService.recordUserSubscriptionCorrectionSuccess(
                    10L, fixture.correction(), fixture.afterState());
            if (failAfterAudit) {
                throw new IllegalStateException("force outer rollback");
            }
        }

        @Transactional
        public void recordRejectedSubscriptionCorrectionThenFail() {
            String state = AdminOperationAuditState.userSubscription(
                    1L,
                    BillingCycle.MONTHLY,
                    SubscriptionStatus.ACTIVE,
                    "2026-09-08",
                    2L,
                    BillingCycle.YEARLY,
                    BillingAgreementStatus.SUSPENDED);
            rejectionAuditService.recordUserSubscriptionCorrectionRejected(
                    10L,
                    20L,
                    state,
                    BUSINESS_ERROR.INVALID_STATE_TRANSITION,
                    null);
            throw new IllegalStateException("force outer rollback");
        }

        @Transactional
        public void recordRejectedSubscriptionCorrectionRequestThenFail() {
            String state = AdminOperationAuditState.userSubscription(
                    1L,
                    BillingCycle.MONTHLY,
                    SubscriptionStatus.ACTIVE,
                    "2026-09-08",
                    2L,
                    BillingCycle.YEARLY,
                    BillingAgreementStatus.SUSPENDED);
            rejectionAuditService.recordUserSubscriptionCorrectionRequestRejected(
                    10L,
                    20L,
                    state,
                    BUSINESS_ERROR.RESOURCE_DUPLICATE);
            throw new IllegalStateException("force outer rollback");
        }

        @Transactional
        public void recordRejectedSubscriptionCorrectionApprovalThenFail() {
            String state = AdminOperationAuditState.userSubscriptionCorrection(
                    20L,
                    AdminSubscriptionCorrectionStatus.REQUESTED);
            rejectionAuditService.recordUserSubscriptionCorrectionApprovalRejected(
                    10L,
                    40L,
                    state,
                    BUSINESS_ERROR.INVALID_STATE_TRANSITION);
            throw new IllegalStateException("force outer rollback");
        }

        private SubscriptionAuditFixture subscriptionAuditFixture() {
            User targetUser = User.builder()
                    .id(30L)
                    .email("target@test.com")
                    .nickname("target")
                    .userType(UserType.INDIVIDUAL)
                    .role(UserRole.USER)
                    .build();
            User administrator = User.builder()
                    .id(10L)
                    .email("admin@test.com")
                    .nickname("admin")
                    .userType(UserType.INDIVIDUAL)
                    .role(UserRole.ADMIN)
                    .build();
            Subscription beforePlan = auditPlan(1L, "BEFORE");
            Subscription targetPlan = auditPlan(2L, "TARGET");
            UserSubscription afterState = UserSubscription.builder()
                    .id(20L)
                    .user(targetUser)
                    .subscription(targetPlan)
                    .billingCycle(BillingCycle.YEARLY)
                    .status(SubscriptionStatus.CANCELLED)
                    .startedAt(LocalDate.of(2026, 7, 8))
                    .expiresAt(LocalDate.of(2026, 9, 8))
                    .build();
            AdminSubscriptionCorrection correction = AdminSubscriptionCorrection.builder()
                    .id(40L)
                    .userSubscription(afterState)
                    .user(targetUser)
                    .beforeSubscription(beforePlan)
                    .beforeBillingCycle(BillingCycle.MONTHLY)
                    .beforeStatus(SubscriptionStatus.ACTIVE)
                    .beforeExpiresAt(LocalDate.of(2026, 9, 8))
                    .beforePendingSubscription(targetPlan)
                    .beforePendingBillingCycle(BillingCycle.YEARLY)
                    .targetSubscription(targetPlan)
                    .targetBillingCycle(BillingCycle.YEARLY)
                    .targetStatus(SubscriptionStatus.CANCELLED)
                    .targetExpiresAt(LocalDate.of(2026, 9, 8))
                    .clearPendingChange(true)
                    .beforeBillingAgreementStatus(BillingAgreementStatus.ACTIVE)
                    .afterBillingAgreementStatus(BillingAgreementStatus.CANCELLED)
                    .reasonNote(ADVERSARIAL_OPERATOR_NOTE)
                    .requestedBy(administrator)
                    .build();
            return new SubscriptionAuditFixture(correction, afterState);
        }

        private Subscription auditPlan(Long id, String name) {
            return Subscription.builder()
                    .id(id)
                    .name(name)
                    .userType(UserType.INDIVIDUAL)
                    .priceMonthly(BigDecimal.ONE)
                    .priceYearly(BigDecimal.TEN)
                    .downloadPerDay(1)
                    .maxWhitelistChannels(1)
                    .isActive(true)
                    .build();
        }

        private record SubscriptionAuditFixture(
                AdminSubscriptionCorrection correction,
                UserSubscription afterState) {
        }
    }
}
