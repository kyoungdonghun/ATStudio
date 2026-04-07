package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaylistBatchAddTrackRequest(
        @NotEmpty @Size(max = 50) List<Long> trackIds
) {}
