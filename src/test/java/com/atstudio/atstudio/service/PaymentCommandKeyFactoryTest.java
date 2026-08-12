package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentCommandKeyFactory unit tests")
class PaymentCommandKeyFactoryTest {

    private final PaymentCommandKeyFactory factory = new PaymentCommandKeyFactory();

    @Test
    @DisplayName("builds canonical command keys from immutable business identity")
    void buildsCanonicalCommandKeys() {
        assertThat(factory.billingConfirm("ORDER-1"))
                .isEqualTo("BILLING_CONFIRM:ORDER-1");
        assertThat(factory.upgrade(
                10L,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1),
                20L,
                BillingCycle.MONTHLY))
                .isEqualTo("UPGRADE:10:2026-07-01:2026-08-01:20:MONTHLY");
        assertThat(factory.renewal(30L, 10L, LocalDate.of(2026, 8, 1)))
                .isEqualTo("RENEWAL:30:10:2026-08-01");
    }

    @Test
    @DisplayName("builds stable provider-attempt keys with persisted attempt number")
    void buildsProviderAttemptKeys() {
        assertThat(factory.billingInitialAttempt("ORDER-1", 2))
                .isEqualTo("billing-initial-ORDER-1-attempt-2");
        assertThat(factory.upgradeAttempt("ORDER-2", 3))
                .isEqualTo("subscription-upgrade-ORDER-2-attempt-3");
        assertThat(factory.renewalAttempt("ORDER-3", 4))
                .isEqualTo("renewal-ORDER-3-attempt-4");
    }

    @Test
    @DisplayName("builds an opaque owner-scoped prepare key without embedding the raw key")
    void buildsOwnerScopedPrepareKey() {
        String rawKey = "123e4567-e89b-42d3-a456-426614174000";

        String first = factory.billingAgreementPrepare(10L, rawKey);
        String replay = factory.billingAgreementPrepare(10L, rawKey);
        String otherOwner = factory.billingAgreementPrepare(11L, rawKey);

        assertThat(first)
                .isEqualTo(replay)
                .startsWith("BILLING_PREPARE:v1:")
                .doesNotContain(rawKey);
        assertThat(otherOwner).isNotEqualTo(first);
    }

    @Test
    @DisplayName("prepare keys accept only canonical UUID v4 values")
    void rejectsInvalidPrepareKeys() {
        assertThatThrownBy(() -> factory.billingAgreementPrepare(10L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.billingAgreementPrepare(10L, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.billingAgreementPrepare(
                10L,
                "123E4567-E89B-42D3-A456-426614174000"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.billingAgreementPrepare(
                10L,
                "123e4567-e89b-12d3-a456-426614174000"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.billingAgreementPrepare(
                10L,
                "123e4567-e89b-42d3-a456-426614174000\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("settlement import digest is stable, owner-scoped, opaque, and exactly 64 hex characters")
    void buildsOpaqueSettlementImportDigest() {
        String rawKey = "123e4567-e89b-42d3-a456-426614174000";

        String digest = factory.settlementImportDigest(10L, rawKey);

        assertThat(digest)
                .isEqualTo(factory.settlementImportDigest(10L, rawKey))
                .hasSize(64)
                .matches("[0-9a-f]{64}")
                .doesNotContain(rawKey);
        assertThat(factory.settlementImportDigest(11L, rawKey)).isNotEqualTo(digest);
        assertThat(factory.settlementImportDigest(
                10L,
                "123e4567-e89b-42d3-a456-426614174001"))
                .isNotEqualTo(digest);
    }

    @Test
    @DisplayName("settlement import accepts only canonical lowercase UUID v4 keys")
    void rejectsInvalidSettlementImportKeys() {
        assertThatThrownBy(() -> factory.settlementImportDigest(10L, null))
                .isInstanceOf(IllegalArgumentException.class);
        for (String invalidKey : List.of(
                "",
                "123E4567-E89B-42D3-A456-426614174000",
                "123e4567-e89b-12d3-a456-426614174000",
                "123e4567-e89b-52d3-a456-426614174000",
                "123e4567-e89b-42d3-7456-426614174000",
                "123e4567e89b42d3a456426614174000",
                " 123e4567-e89b-42d3-a456-426614174000",
                "123e4567-e89b-42d3-a456-426614174000\n")) {
            assertThatThrownBy(() -> factory.settlementImportDigest(10L, invalidKey))
                    .as("invalid settlement import key: %s", invalidKey)
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> factory.settlementImportDigest(null,
                "123e4567-e89b-42d3-a456-426614174000"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.settlementImportDigest(0L,
                "123e4567-e89b-42d3-a456-426614174000"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
