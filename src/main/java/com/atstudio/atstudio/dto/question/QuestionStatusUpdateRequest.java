package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.enums.QuestionStatus;
import jakarta.validation.constraints.NotNull;

public record QuestionStatusUpdateRequest(
        @NotNull QuestionStatus status
) {}
