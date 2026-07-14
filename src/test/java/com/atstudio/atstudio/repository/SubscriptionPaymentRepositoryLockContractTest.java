package com.atstudio.atstudio.repository;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionPaymentRepository refund lock contract tests")
class SubscriptionPaymentRepositoryLockContractTest {

    @Test
    @DisplayName("source payment lookup uses a graph-complete pessimistic write lock")
    void sourcePaymentLookupUsesGraphCompletePessimisticWriteLock() throws NoSuchMethodException {
        Method method = SubscriptionPaymentRepository.class
                .getMethod("findWithGraphByIdForUpdate", Long.class);

        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(method.getAnnotation(EntityGraph.class).attributePaths()).containsExactlyInAnyOrder(
                "user",
                "userSubscription",
                "subscription",
                "paymentOrder",
                "billingAgreement");
    }
}
