package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.constraints.NotNull;

public record PlaylistAddTrackRequest(
        @NotNull Long trackId
) {}
