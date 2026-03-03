package com.atstudio.atstudio.dto.album;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AlbumTrackOrderItem(
        @NotNull Long trackId,
        @NotNull @Min(0) Integer order
) {}
