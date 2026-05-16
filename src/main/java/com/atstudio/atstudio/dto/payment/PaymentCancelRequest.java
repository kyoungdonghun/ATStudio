package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCancelRequest(
        @NotBlank String orderId,
        @NotNull PaymentOrderStatus status,
        String reason
) {}
