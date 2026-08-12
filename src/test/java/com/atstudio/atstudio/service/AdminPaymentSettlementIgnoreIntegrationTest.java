package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        JpaConfig.class,
        AdminPaymentSettlementService.class,
        AdminPaymentSettlementAttemptTransactionService.class,
        AdminPaymentSettlementRowTransactionService.class,
        PaymentCommandKeyFactory.class,
        PaymentOperationAuditLogService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Settlement IGNORE H2 integration tests")
class AdminPaymentSettlementIgnoreIntegrationTest {

    @Autowired AdminPaymentSettlementService service;
    @Autowired PaymentSettlementRepository settlementRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("repeated same and conflicting requests preserve the first durable decision and audit row")
    void repeatedIgnorePreservesFirstDurableEvidence() {
        User firstActor = userRepository.saveAndFlush(User.builder()
                .nickname("settlement-first")
                .email("settlement-first-admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
        User laterActor = userRepository.saveAndFlush(User.builder()
                .nickname("settlement-later")
                .email("settlement-later-admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
        PaymentSettlement settlement = settlementRepository.saveAndFlush(PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("h2-ignore-dedup")
                .importBatchKey("h2-ignore-batch")
                .orderId("ORDER-H2-IGNORE")
                .grossAmount(BigDecimal.valueOf(9900))
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .settlementBaseDate(LocalDate.of(2026, 8, 9))
                .build());

        service.ignoreSettlement(
                settlement.getId(),
                actor(firstActor),
                new AdminPaymentSettlementIgnoreRequest("  first durable note  "));

        PaymentSettlement firstDecision = settlementRepository.findWithGraphById(settlement.getId())
                .orElseThrow();
        LocalDateTime firstIgnoredAt = firstDecision.getIgnoredAt();
        List<PaymentOperationAuditLog> firstAudits = auditsFor(settlement.getId());
        assertThat(firstDecision.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(firstDecision.getIgnoredBy().getId()).isEqualTo(firstActor.getId());
        assertThat(firstDecision.getOperatorNote()).isEqualTo("first durable note");
        assertThat(firstIgnoredAt).isNotNull();
        assertThat(firstAudits).singleElement().satisfies(audit -> {
            assertThat(audit.getAction()).isEqualTo(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED);
            assertThat(audit.getActorUser().getId()).isEqualTo(firstActor.getId());
            assertThat(audit.getBeforeStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED.name());
            assertThat(audit.getAfterStatus()).isEqualTo(PaymentSettlementStatus.IGNORED.name());
            assertThat(audit.getNote()).isEqualTo("first durable note");
        });
        Long firstAuditId = firstAudits.get(0).getId();
        LocalDateTime firstAuditCreatedAt = firstAudits.get(0).getCreatedAt();

        assertRepeatedIgnoreRejected(settlement.getId(), firstActor, "first durable note");
        assertRepeatedIgnoreRejected(settlement.getId(), laterActor, "conflicting later note");

        PaymentSettlement retained = settlementRepository.findWithGraphById(settlement.getId())
                .orElseThrow();
        List<PaymentOperationAuditLog> retainedAudits = auditsFor(settlement.getId());
        assertThat(retained.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(retained.getIgnoredBy().getId()).isEqualTo(firstActor.getId());
        assertThat(retained.getIgnoredAt()).isEqualTo(firstIgnoredAt);
        assertThat(retained.getOperatorNote()).isEqualTo("first durable note");
        assertThat(retainedAudits).singleElement().satisfies(audit -> {
            assertThat(audit.getId()).isEqualTo(firstAuditId);
            assertThat(audit.getCreatedAt()).isEqualTo(firstAuditCreatedAt);
            assertThat(audit.getActorUser().getId()).isEqualTo(firstActor.getId());
            assertThat(audit.getNote()).isEqualTo("first durable note");
        });
    }

    @Test
    @DisplayName("concurrent first decisions produce one transition, one audit, and one invalid-state loser")
    void concurrentIgnoreSerializesFirstDecision() throws Exception {
        User firstActor = userRepository.saveAndFlush(User.builder()
                .nickname("concurrent-first")
                .email("concurrent-first@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
        User secondActor = userRepository.saveAndFlush(User.builder()
                .nickname("concurrent-second")
                .email("concurrent-second@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
        PaymentSettlement settlement = settlementRepository.saveAndFlush(PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("h2-concurrent-ignore-dedup")
                .importBatchKey("h2-concurrent-ignore-batch")
                .orderId("ORDER-H2-CONCURRENT-IGNORE")
                .grossAmount(BigDecimal.valueOf(9900))
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .settlementBaseDate(LocalDate.of(2026, 8, 9))
                .build());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<IgnoreAttempt> first = executor.submit(() -> ignoreConcurrently(
                    ready,
                    start,
                    settlement.getId(),
                    firstActor,
                    "first concurrent note"));
            Future<IgnoreAttempt> second = executor.submit(() -> ignoreConcurrently(
                    ready,
                    start,
                    settlement.getId(),
                    secondActor,
                    "second concurrent note"));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)))
                    .extracting(IgnoreAttempt::error)
                    .containsExactlyInAnyOrder(null, BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        } finally {
            executor.shutdownNow();
        }

        PaymentSettlement retained = settlementRepository.findWithGraphById(settlement.getId())
                .orElseThrow();
        List<PaymentOperationAuditLog> retainedAudits = auditsFor(settlement.getId());
        String expectedNote = retained.getIgnoredBy().getId().equals(firstActor.getId())
                ? "first concurrent note"
                : "second concurrent note";
        assertThat(retained.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(retained.getIgnoredBy().getId()).isIn(firstActor.getId(), secondActor.getId());
        assertThat(retained.getOperatorNote()).isEqualTo(expectedNote);
        assertThat(retained.getIgnoredAt()).isNotNull();
        assertThat(retainedAudits).singleElement().satisfies(audit -> {
            assertThat(audit.getAction()).isEqualTo(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED);
            assertThat(audit.getActorUser().getId()).isEqualTo(retained.getIgnoredBy().getId());
            assertThat(audit.getBeforeStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED.name());
            assertThat(audit.getAfterStatus()).isEqualTo(PaymentSettlementStatus.IGNORED.name());
            assertThat(audit.getNote()).isEqualTo(expectedNote);
        });
    }

    private void assertRepeatedIgnoreRejected(Long settlementId, User actor, String note) {
        assertThatThrownBy(() -> service.ignoreSettlement(
                settlementId,
                actor(actor),
                new AdminPaymentSettlementIgnoreRequest(note)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
    }

    private IgnoreAttempt ignoreConcurrently(
            CountDownLatch ready,
            CountDownLatch start,
            Long settlementId,
            User actor,
            String note) throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent IGNORE start gate timed out.");
        }
        try {
            service.ignoreSettlement(
                    settlementId,
                    actor(actor),
                    new AdminPaymentSettlementIgnoreRequest(note));
            return new IgnoreAttempt(null);
        } catch (BusinessException exception) {
            return new IgnoreAttempt(exception.getErrorCode());
        }
    }

    private CustomUserDetails actor(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(UserRole.ADMIN)
                .build();
    }

    private List<PaymentOperationAuditLog> auditsFor(Long settlementId) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged()).getContent().stream()
                .filter(audit -> audit.getTargetId().equals(settlementId))
                .toList();
    }

    private record IgnoreAttempt(BUSINESS_ERROR error) {
    }
}
