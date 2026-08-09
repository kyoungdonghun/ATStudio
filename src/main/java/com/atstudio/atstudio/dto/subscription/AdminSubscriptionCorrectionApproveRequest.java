package com.atstudio.atstudio.dto.subscription;

import jakarta.validation.constraints.Size;

public record AdminSubscriptionCorrectionApproveRequest(
        @Size(max = 500) String note
) {
}
