package com.atstudio.atstudio.dto.subscription;

import jakarta.validation.constraints.Size;

public record AdminSubscriptionCorrectionExecuteRequest(
        @Size(max = 500) String note
) {
}
