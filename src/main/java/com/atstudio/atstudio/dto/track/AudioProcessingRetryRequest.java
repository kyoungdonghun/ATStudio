package com.atstudio.atstudio.dto.track;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AudioProcessingRetryRequest(@NotNull @PositiveOrZero Long generation) { }
