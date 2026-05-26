package com.atstudio.atstudio.dto.payment;

import java.time.LocalDate;

public record AdminPaymentSettlementReconcileRequest(
        LocalDate baseDateFrom,
        LocalDate baseDateTo) {
}
