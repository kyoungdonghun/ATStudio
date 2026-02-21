package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PlaylistReorderRequest(
        @NotEmpty @Valid List<PlaylistTrackOrderItem> tracks
) {}
