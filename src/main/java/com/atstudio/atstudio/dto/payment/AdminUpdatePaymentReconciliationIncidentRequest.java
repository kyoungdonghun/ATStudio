package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUpdatePaymentReconciliationIncidentRequest(
        @NotNull PaymentReconciliationIncidentStatus status,
        @Size(max = 500) String note
) {
}
