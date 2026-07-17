package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentOrder financial state machine")
class PaymentOrderStateMachineTest {

    @Test
    @DisplayName("ownership and expiry require complete matching evidence")
    void ownershipAndExpiry_areFailClosed() {
        User owner = user(1L);
        PaymentOrder order = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, owner);
        User sameID = user(1L);
        User other = user(2L);

        assertThat(order.isOwnedBy(null)).isFalse();
        assertThat(order.isOwnedBy(sameID)).isTrue();
        assertThat(order.isOwnedBy(other)).isFalse();
        assertThat(order.isExpired(order.getExpiresAt().minusSeconds(1))).isFalse();
        assertThat(order.isExpired(order.getExpiresAt())).isTrue();
        ReflectionTestUtils.setField(order, "expiresAt", null);
        assertThat(order.isExpired(LocalDateTime.now())).isFalse();
    }

    @Test
    @DisplayName("markInProgress only advances READY while retaining provider payload on retries")
    void markInProgress_isIdempotentForAdvancedState() {
        PaymentOrder ready = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(1L));
        ready.markInProgress("first");
        ready.markInProgress("second");

        assertThat(ready.getStatus()).isEqualTo(PaymentOrderStatus.IN_PROGRESS);
        assertThat(ready.getProviderPayload()).isEqualTo("second");
    }

    @Test
    @DisplayName("provider claim validates required evidence, immutable command identity, and allowed states")
    void claimProviderAttempt_validatesCommandFence() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 7, 17, 1, 0);
        PaymentOrder order = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(1L));

        assertThatThrownBy(() -> order.claimProviderAttempt(null, "attempt", startedAt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.claimProviderAttempt(" ", "attempt", startedAt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.claimProviderAttempt("command", "", startedAt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.claimProviderAttempt("command", "attempt", null))
                .isInstanceOf(IllegalArgumentException.class);

        order.claimProviderAttempt("command", "attempt-1", startedAt);
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
        assertThat(order.getProviderAttempt()).isOne();
        assertThat(order.getFailureCode()).isNull();

        assertThatThrownBy(() -> order.claimProviderAttempt("other-command", "attempt-2", startedAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be changed");
        assertThatThrownBy(() -> order.claimProviderAttempt("command", "attempt-2", startedAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot claim");
    }

    @Test
    @DisplayName("READY, IN_PROGRESS, and FAILED orders may each claim a new provider attempt")
    void claimProviderAttempt_acceptsRetryableStates() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 7, 17, 1, 0);
        for (PaymentOrderStatus status : new PaymentOrderStatus[] {
                PaymentOrderStatus.READY,
                PaymentOrderStatus.IN_PROGRESS,
                PaymentOrderStatus.FAILED}) {
            PaymentOrder order = order(PaymentPurpose.RENEWAL, status, user(status.ordinal() + 1L));

            order.claimProviderAttempt("command-" + status, "attempt-" + status, startedAt);

            assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
            assertThat(order.getProviderAttempt()).isOne();
        }
    }

    @Test
    @DisplayName("purpose-specific fields are enforced before provider execution")
    void claimProviderAttempt_enforcesPurposeFields() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 7, 17, 1, 0);
        PaymentOrder missingUpgradeCycle = order(PaymentPurpose.UPGRADE, PaymentOrderStatus.READY, user(1L));
        ReflectionTestUtils.setField(missingUpgradeCycle, "upgradeTargetBillingCycle", null);
        PaymentOrder illegalUpgradeCycle = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(2L));
        ReflectionTestUtils.setField(illegalUpgradeCycle, "upgradeTargetBillingCycle", BillingCycle.YEARLY);

        assertThatThrownBy(() -> missingUpgradeCycle.claimProviderAttempt("upgrade", "attempt", startedAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required");
        assertThatThrownBy(() -> illegalUpgradeCycle.claimProviderAttempt("renewal", "attempt", startedAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only for upgrade");
    }

    @Test
    @DisplayName("provider success is single-transition and same-transaction replay is idempotent")
    void markProviderSucceeded_enforcesTransactionFence() {
        PaymentOrder processing = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(1L));
        assertThatThrownBy(() -> processing.markProviderSucceeded(" ", "{}"))
                .isInstanceOf(IllegalArgumentException.class);

        processing.markProviderSucceeded("tx-1", "{success:true}");
        processing.markProviderSucceeded("tx-1", "ignored-replay");

        assertThat(processing.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(processing.getPgTransactionId()).isEqualTo("tx-1");
        assertThat(processing.getProviderPayload()).isEqualTo("{success:true}");

        PaymentOrder ready = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(2L));
        assertThatThrownBy(() -> ready.markProviderSucceeded("tx-2", "{}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot record");
    }

    @Test
    @DisplayName("reconciliation only converts stale PROCESSING or pending-confirmation evidence")
    void markProviderSucceededFromReconciliation_enforcesStalenessAndTransactionIdentity() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 17, 1, 0);
        PaymentOrder fresh = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(1L));
        ReflectionTestUtils.setField(fresh, "processingStartedAt", boundary.plusSeconds(1));
        assertThatThrownBy(() -> fresh.markProviderSucceededFromReconciliation("tx", "{}", boundary))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fresh payment");

        PaymentOrder invalid = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(2L));
        assertThatThrownBy(() -> invalid.markProviderSucceededFromReconciliation("tx", "{}", boundary))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot reconcile");

        PaymentOrder conflict = order(
                PaymentPurpose.RENEWAL,
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                user(3L));
        ReflectionTestUtils.setField(conflict, "pgTransactionId", "old-tx");
        assertThatThrownBy(() -> conflict.markProviderSucceededFromReconciliation("new-tx", "{}", boundary))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be changed");

        PaymentOrder stale = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(4L));
        ReflectionTestUtils.setField(stale, "processingStartedAt", boundary.minusSeconds(1));
        stale.markProviderSucceededFromReconciliation("tx-stale", "{}", boundary);
        PaymentOrder pending = order(
                PaymentPurpose.RENEWAL,
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                user(5L));
        pending.markProviderSucceededFromReconciliation("tx-pending", "{}", boundary);

        assertThat(stale.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(pending.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThatThrownBy(() -> pending.markProviderSucceededFromReconciliation(null, "{}", boundary))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("unknown outcomes require PROCESSING and become pending provider confirmation")
    void markProviderOutcomeUnknown_isFenced() {
        PaymentOrder ready = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, user(1L));
        assertThatThrownBy(() -> ready.markProviderOutcomeUnknown("TIMEOUT", "unknown"))
                .isInstanceOf(IllegalStateException.class);

        PaymentOrder processing = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(2L));
        processing.markProviderOutcomeUnknown("TIMEOUT", "unknown");

        assertThat(processing.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(processing.getFailureCode()).isEqualTo("TIMEOUT");
        assertThat(processing.getProcessingStartedAt()).isNull();
    }

    @Test
    @DisplayName("stale processing requires a boundary, PROCESSING state, and an elapsed claim time")
    void isProcessingStale_requiresAllEvidence() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 17, 1, 0);
        PaymentOrder order = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(1L));
        assertThat(order.isProcessingStale(null)).isFalse();
        assertThat(order.isProcessingStale(boundary)).isFalse();
        ReflectionTestUtils.setField(order, "processingStartedAt", boundary.plusSeconds(1));
        assertThat(order.isProcessingStale(boundary)).isFalse();
        ReflectionTestUtils.setField(order, "processingStartedAt", boundary);
        assertThat(order.isProcessingStale(boundary)).isTrue();
        ReflectionTestUtils.setField(order, "status", PaymentOrderStatus.FAILED);
        assertThat(order.isProcessingStale(boundary)).isFalse();
    }

    @Test
    @DisplayName("terminal transitions clear processing evidence and retain their explicit outcome")
    void terminalTransitions_clearProcessingEvidence() {
        PaymentOrder done = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(1L));
        done.markDone("tx", mockSubscription(), "{}");
        assertThat(done.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(done.getConfirmedAt()).isNotNull();

        PaymentOrder failed = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(2L));
        failed.markFailed("DECLINED", "declined");
        assertThat(failed.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);

        PaymentOrder cancelled = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(3L));
        cancelled.markCancelled("operator request");
        assertThat(cancelled.getStatus()).isEqualTo(PaymentOrderStatus.CANCELLED);
        assertThat(cancelled.getFailureCode()).isEqualTo("CANCELLED");

        PaymentOrder expired = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PROCESSING, user(4L));
        expired.markExpired();
        assertThat(expired.getStatus()).isEqualTo(PaymentOrderStatus.EXPIRED);
        assertThat(expired.getFailureCode()).isEqualTo("EXPIRED");
    }

    private PaymentOrder order(PaymentPurpose purpose, PaymentOrderStatus status, User user) {
        return PaymentOrder.builder()
                .orderId("ORDER-" + user.getId())
                .user(user)
                .purpose(purpose)
                .provider(PaymentProviderType.TOSS)
                .status(status)
                .subscription(Subscription.builder().name("Plan").build())
                .billingCycle(BillingCycle.MONTHLY)
                .upgradeTargetBillingCycle(purpose == PaymentPurpose.UPGRADE ? BillingCycle.YEARLY : null)
                .billingPeriodStart(purpose == PaymentPurpose.RENEWAL ? java.time.LocalDate.of(2026, 8, 1) : null)
                .amount(BigDecimal.valueOf(9_900))
                .expiresAt(LocalDateTime.of(2026, 8, 1, 0, 0))
                .build();
    }

    private User user(Long id) {
        User user = User.builder().email("user-" + id + "@example.com").nickname("user-" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserSubscription mockSubscription() {
        return UserSubscription.builder()
                .user(user(99L))
                .subscription(Subscription.builder().name("Plan").build())
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(java.time.LocalDate.of(2026, 7, 1))
                .expiresAt(java.time.LocalDate.of(2026, 8, 1))
                .build();
    }
}
