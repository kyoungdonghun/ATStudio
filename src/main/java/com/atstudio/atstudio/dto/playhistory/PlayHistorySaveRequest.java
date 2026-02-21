package com.atstudio.atstudio.dto.playhistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayHistorySaveRequest(
        @NotNull Long trackId
) {
}
