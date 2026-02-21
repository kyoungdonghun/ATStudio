package com.atstudio.atstudio.dto.playlist;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaylistTrackItemResponse(
        int trackOrder,
        Long trackId,
        String title,
        Integer bpm,
        String tonality
) {
    public static PlaylistTrackItemResponse from(PlaylistTrack pt) {
        return new PlaylistTrackItemResponse(
                pt.getTrackOrder(),
                pt.getTrack().getId(),
                pt.getTrack().getTitle(),
                pt.getTrack().getBpm(),
                pt.getTrack().getTonality()
        );
    }
}
