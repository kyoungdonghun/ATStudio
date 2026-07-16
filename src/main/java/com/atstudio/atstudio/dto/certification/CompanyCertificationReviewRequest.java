package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.atstudio.atstudio.common.validation.ValidationConstants.CERTIFICATION_REVIEW_NOTE_MAX;

public record CompanyCertificationReviewRequest(
        @NotNull CompanyCertificationStatus status,
        @Size(max = CERTIFICATION_REVIEW_NOTE_MAX) String adminNote
) {}
