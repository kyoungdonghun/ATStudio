package com.atstudio.atstudio.dto.album;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AlbumTrackOrderRequest(
        @NotEmpty @Valid List<AlbumTrackOrderItem> trackOrders
) {}
