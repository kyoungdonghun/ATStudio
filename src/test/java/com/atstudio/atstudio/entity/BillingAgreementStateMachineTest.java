package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BillingAgreement recurring-payment state machine")
class BillingAgreementStateMachineTest {

    @Test
    @DisplayName("registration reset requires a customer key and clears all reusable payment state")
    void prepareRegistration_requiresIdentityAndClearsState() {
        BillingAgreement agreement = activeAgreement();
        agreement.recordSuccessfulCharge(LocalDate.of(2026, 9, 1));

        assertThatThrownBy(() -> agreement.prepareRegistration(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agreement.prepareRegistration(" "))
                .isInstanceOf(IllegalArgumentException.class);

        agreement.prepareRegistration("new-customer");

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(agreement.getProviderCustomerKey()).isEqualTo("new-customer");
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getBillingKeyFingerprint()).isNull();
        assertThat(agreement.getNextBillingAt()).isNull();
        assertThat(agreement.getLastChargedAt()).isNull();
    }

    @Test
    @DisplayName("issued key storage validates both protected values and clear removes every reusable field")
    void storeAndClearIssuedKey_areAtomic() {
        BillingAgreement agreement = readyAgreement();
        assertThatThrownBy(() -> agreement.storeIssuedKey(null, "fingerprint", "CARD", "masked"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agreement.storeIssuedKey("cipher", " ", "CARD", "masked"))
                .isInstanceOf(IllegalArgumentException.class);

        agreement.storeIssuedKey("v2:key:nonce:cipher", "fingerprint", "CARD", "masked");
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isTrue();

        agreement.clearIssuedKey();
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getBillingKeyFingerprint()).isNull();
        assertThat(agreement.getPayMethod()).isNull();
        assertThat(agreement.getMaskedMethod()).isNull();
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
    }

    @Test
    @DisplayName("activation and resume refuse incomplete key evidence and restore active charge state")
    void activateAndResume_requireIssuedKey() {
        BillingAgreement agreement = readyAgreement();
        assertThatThrownBy(() -> agreement.activate("cipher", null, "CARD", "masked", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> agreement.resume(LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
        agreement.storeIssuedKey("cipher", "fingerprint", "CARD", "masked");
        agreement.cancel();
        LocalDate next = LocalDate.of(2026, 8, 17);
        agreement.resume(next);

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(agreement.getNextBillingAt()).isEqualTo(next);
        assertThat(agreement.getCancelledAt()).isNull();
        assertThat(agreement.getFailureCount()).isZero();
    }

    @Test
    @DisplayName("renewal retry must be scheduled before it can be consumed")
    void renewalRetry_hasExplicitLifecycle() {
        BillingAgreement agreement = activeAgreement();
        assertThatThrownBy(() -> agreement.recordFailedCharge(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(agreement::consumeRenewalRetry)
                .isInstanceOf(IllegalStateException.class);

        LocalDate retryAt = LocalDate.of(2026, 7, 20);
        agreement.recordFailedCharge(retryAt);
        assertThat(agreement.getFailureCount()).isOne();
        assertThat(agreement.getRenewalRetryAt()).isEqualTo(retryAt);
        agreement.consumeRenewalRetry();
        assertThat(agreement.getRenewalRetryAt()).isNull();

        agreement.recordFailedCharge();
        assertThat(agreement.getFailureCount()).isEqualTo(2);
        agreement.suspend();
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.SUSPENDED);
        agreement.expire();
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.EXPIRED);
    }

    @Test
    @DisplayName("chargeability requires ACTIVE status, a next date, and a due date")
    void isChargeableOn_requiresEveryCondition() {
        BillingAgreement agreement = activeAgreement();
        LocalDate next = agreement.getNextBillingAt();
        assertThat(agreement.isChargeableOn(next.minusDays(1))).isFalse();
        assertThat(agreement.isChargeableOn(next)).isTrue();
        ReflectionTestUtils.setField(agreement, "nextBillingAt", null);
        assertThat(agreement.isChargeableOn(next)).isFalse();
        ReflectionTestUtils.setField(agreement, "nextBillingAt", next);
        agreement.cancel();
        assertThat(agreement.isChargeableOn(next)).isFalse();
    }

    @Test
    @DisplayName("initial finalization eligibility fails closed for status, cleanup, cancellation, and key gaps")
    void initialFinalizationEligibility_requiresCompleteEvidence() {
        BillingAgreement agreement = readyAgreement();
        agreement.storeIssuedKey("cipher", "fingerprint", "CARD", "masked");
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isTrue();

        ReflectionTestUtils.setField(agreement, "status", BillingAgreementStatus.ACTIVE);
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
        ReflectionTestUtils.setField(agreement, "status", BillingAgreementStatus.READY);
        ReflectionTestUtils.setField(agreement, "billingKeyCleanupStatus", BillingKeyCleanupStatus.REQUIRED);
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
        ReflectionTestUtils.setField(agreement, "billingKeyCleanupStatus", BillingKeyCleanupStatus.NONE);
        ReflectionTestUtils.setField(agreement, "cancelledAt", LocalDateTime.now());
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
        ReflectionTestUtils.setField(agreement, "cancelledAt", null);
        ReflectionTestUtils.setField(agreement, "billingKeyCiphertext", " ");
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
        ReflectionTestUtils.setField(agreement, "billingKeyCiphertext", "cipher");
        ReflectionTestUtils.setField(agreement, "billingKeyFingerprint", null);
        assertThat(agreement.isInitialSubscriptionFinalizationEligible()).isFalse();
    }

    @Test
    @DisplayName("cleanup retry is allowed only for cancelled agreements with retained keys and retryable outcomes")
    void markBillingKeyCleanupRequired_enforcesRetryState() {
        BillingAgreement active = activeAgreement();
        assertThatThrownBy(active::markBillingKeyCleanupRequired)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cancelled agreement");

        BillingAgreement cancelled = activeAgreement();
        cancelled.cancel();
        assertThatThrownBy(cancelled::markBillingKeyCleanupRequired)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be retried");

        for (BillingKeyCleanupStatus retryable : new BillingKeyCleanupStatus[] {
                BillingKeyCleanupStatus.FAILED,
                BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION}) {
            BillingAgreement agreement = activeAgreement();
            agreement.cancel();
            ReflectionTestUtils.setField(agreement, "billingKeyCleanupStatus", retryable);
            agreement.markBillingKeyCleanupRequired();
            assertThat(agreement.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.REQUIRED);
        }
    }

    @Test
    @DisplayName("cleanup claim accepts NONE or REQUIRED and records a second-precision lease")
    void claimBillingKeyCleanup_validatesLeaseStartAndState() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 7, 17, 2, 0, 0, 123_000_000);
        BillingAgreement cancelled = activeAgreement();
        cancelled.cancel();
        assertThatThrownBy(() -> cancelled.claimBillingKeyCleanup(null))
                .isInstanceOf(IllegalArgumentException.class);

        cancelled.claimBillingKeyCleanup(startedAt);
        assertThat(cancelled.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
        assertThat(cancelled.getBillingKeyCleanupStartedAt()).isEqualTo(startedAt.withNano(0));
        assertThatThrownBy(() -> cancelled.claimBillingKeyCleanup(startedAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be claimed");

        BillingAgreement required = activeAgreement();
        required.cancel();
        ReflectionTestUtils.setField(required, "billingKeyCleanupStatus", BillingKeyCleanupStatus.REQUIRED);
        required.claimBillingKeyCleanup(startedAt);
        assertThat(required.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
    }

    @Test
    @DisplayName("cleanup completion requires the exact active lease and records each provider outcome")
    void cleanupCompletion_isLeaseFenced() {
        LocalDateTime lease = LocalDateTime.of(2026, 7, 17, 2, 0);
        BillingAgreement succeeded = claimedCleanup(lease);
        assertThatThrownBy(() -> succeeded.markBillingKeyCleanupSucceeded(lease.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class);
        succeeded.markBillingKeyCleanupSucceeded(lease);
        assertThat(succeeded.getBillingKeyCiphertext()).isNull();
        assertThat(succeeded.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.NONE);

        BillingAgreement failed = claimedCleanup(lease);
        failed.markBillingKeyCleanupFailed(lease);
        assertThat(failed.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.FAILED);

        BillingAgreement pending = claimedCleanup(lease);
        pending.markBillingKeyCleanupPendingProviderConfirmation(lease);
        assertThat(pending.getBillingKeyCleanupStatus())
                .isEqualTo(BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION);

        BillingAgreement notProcessing = activeAgreement();
        notProcessing.cancel();
        assertThatThrownBy(() -> notProcessing.markBillingKeyCleanupFailed(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cleanup staleness requires a boundary, PROCESSING state, start time, and elapsed lease")
    void cleanupStaleness_requiresAllEvidence() {
        LocalDateTime lease = LocalDateTime.of(2026, 7, 17, 2, 0);
        BillingAgreement agreement = claimedCleanup(lease);
        assertThat(agreement.isBillingKeyCleanupProcessingStale(null)).isFalse();
        assertThat(agreement.isBillingKeyCleanupProcessingStale(lease.minusSeconds(1))).isFalse();
        assertThat(agreement.isBillingKeyCleanupProcessingStale(lease)).isTrue();
        ReflectionTestUtils.setField(agreement, "billingKeyCleanupStartedAt", null);
        assertThat(agreement.isBillingKeyCleanupProcessingStale(lease)).isFalse();
        ReflectionTestUtils.setField(agreement, "billingKeyCleanupStartedAt", lease);
        ReflectionTestUtils.setField(agreement, "billingKeyCleanupStatus", BillingKeyCleanupStatus.FAILED);
        assertThat(agreement.isBillingKeyCleanupProcessingStale(lease)).isFalse();
    }

    @Test
    @DisplayName("agreement ownership rejects null, missing owners, and different users")
    void isOwnedBy_comparesPersistentIdentity() {
        BillingAgreement agreement = activeAgreement();
        assertThat(agreement.isOwnedBy(null)).isFalse();
        assertThat(agreement.isOwnedBy(user(1L))).isTrue();
        assertThat(agreement.isOwnedBy(user(2L))).isFalse();
        ReflectionTestUtils.setField(agreement, "user", null);
        assertThat(agreement.isOwnedBy(user(1L))).isFalse();
    }

    private BillingAgreement claimedCleanup(LocalDateTime lease) {
        BillingAgreement agreement = activeAgreement();
        agreement.cancel();
        agreement.claimBillingKeyCleanup(lease);
        return agreement;
    }

    private BillingAgreement activeAgreement() {
        BillingAgreement agreement = readyAgreement();
        agreement.activate(
                "v2:key:nonce:cipher",
                "fingerprint",
                "CARD",
                "1234-****-****-5678",
                LocalDate.of(2026, 8, 17));
        return agreement;
    }

    private BillingAgreement readyAgreement() {
        return BillingAgreement.builder()
                .user(user(1L))
                .provider(PaymentProviderType.TOSS)
                .status(BillingAgreementStatus.READY)
                .providerCustomerKey("customer-key")
                .build();
    }

    private User user(Long id) {
        User user = User.builder().email("user-" + id + "@example.com").nickname("user-" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
