package com.atstudio.atstudio.dto.playhistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayHistoryDeleteRequest(
        @NotNull List<Long> historyIds
) {
}
