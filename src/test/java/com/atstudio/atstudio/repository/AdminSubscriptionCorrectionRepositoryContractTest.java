package com.atstudio.atstudio.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Administrator subscription correction repository contracts")
class AdminSubscriptionCorrectionRepositoryContractTest {

    @Test
    @DisplayName("open workflow lookup is graph-complete, deterministic, and non-locking")
    void openWorkflowLookupContract() throws Exception {
        Method method = AdminSubscriptionCorrectionRepository.class.getMethod(
                "findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc",
                Long.class,
                Set.class);

        assertThat(method.getAnnotation(Lock.class)).isNull();
        assertThat(method.getName()).endsWith("OrderByCreatedAtDescIdDesc");
        assertThat(method.getAnnotation(EntityGraph.class).attributePaths())
                .containsExactlyInAnyOrder(
                        "userSubscription",
                        "user",
                        "billingAgreement",
                        "beforeSubscription",
                        "beforePendingSubscription",
                        "targetSubscription",
                        "requestedBy",
                        "approvedBy",
                        "executedBy");
    }
}
