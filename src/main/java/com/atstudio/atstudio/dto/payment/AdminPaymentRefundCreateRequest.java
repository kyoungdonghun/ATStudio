package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminPaymentRefundCreateRequest(
        @NotNull Long subscriptionPaymentId,
        @NotNull @DecimalMin(value = "1.00") BigDecimal amount,
        @NotNull PaymentRefundReasonCode reasonCode,
        @Size(max = 500) String reasonNote
) {
}
