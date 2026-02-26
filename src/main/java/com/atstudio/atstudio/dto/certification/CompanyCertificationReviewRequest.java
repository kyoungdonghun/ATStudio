package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import jakarta.validation.constraints.NotNull;

public record CompanyCertificationReviewRequest(
        @NotNull CompanyCertificationStatus status,
        String adminNote
) {}
