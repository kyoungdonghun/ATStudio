package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentSettlement domain tests")
class PaymentSettlementTest {

    @Test
    @DisplayName("ignore preserves the first decision when called again")
    void ignorePreservesFirstDecision() {
        User firstActor = User.builder()
                .id(91L)
                .nickname("first-admin")
                .email("first-admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        User laterActor = User.builder()
                .id(92L)
                .nickname("later-admin")
                .email("later-admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        PaymentSettlement settlement = settlement();

        settlement.ignore(firstActor, "first decision");
        LocalDateTime firstIgnoredAt = settlement.getIgnoredAt();

        assertThatThrownBy(() -> settlement.ignore(laterActor, "later decision"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(settlement.getIgnoredBy()).isEqualTo(firstActor);
        assertThat(settlement.getIgnoredAt()).isEqualTo(firstIgnoredAt);
        assertThat(settlement.getOperatorNote()).isEqualTo("first decision");
    }

    private PaymentSettlement settlement() {
        return PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("entity-ignore-dedup")
                .importBatchKey("entity-ignore-batch")
                .orderId("ORDER-ENTITY-IGNORE")
                .grossAmount(BigDecimal.valueOf(9900))
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .settlementBaseDate(LocalDate.of(2026, 8, 9))
                .build();
    }
}
