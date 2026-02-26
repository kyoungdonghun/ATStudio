package com.atstudio.atstudio.dto.question;

import jakarta.validation.constraints.NotBlank;

public record AnswerCreateRequest(
        @NotBlank String content
) {}
