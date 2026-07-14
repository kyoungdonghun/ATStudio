package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
}
