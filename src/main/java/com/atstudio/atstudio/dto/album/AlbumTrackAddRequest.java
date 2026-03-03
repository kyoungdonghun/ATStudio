package com.atstudio.atstudio.dto.album;

import jakarta.validation.constraints.NotNull;

public record AlbumTrackAddRequest(
        @NotNull Long trackId
) {}
