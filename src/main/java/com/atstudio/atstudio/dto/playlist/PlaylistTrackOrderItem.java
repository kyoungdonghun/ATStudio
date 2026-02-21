package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlaylistTrackOrderItem(
        @NotNull Long trackId,
        @NotNull @Min(0) Integer trackOrder
) {}
