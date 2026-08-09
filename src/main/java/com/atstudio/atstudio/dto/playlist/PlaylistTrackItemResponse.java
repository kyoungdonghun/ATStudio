package com.atstudio.atstudio.dto.playlist;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaylistTrackItemResponse(
        int trackOrder,
        Long trackId,
        String title,
        String artistName,
        int duration,
        String thumbnail,
        String waveformData,
        Integer bpm,
        String tonality
) {
    public static PlaylistTrackItemResponse from(PlaylistTrack pt) {
        return new PlaylistTrackItemResponse(
                pt.getTrackOrder(),
                pt.getTrack().getId(),
                pt.getTrack().getTitle(),
                pt.getTrack().getUser().getNickname(),
                pt.getTrack().getDuration(),
                pt.getTrack().getThumbnail(),
                pt.getTrack().getWaveformData(),
                pt.getTrack().getBpm(),
                pt.getTrack().getTonality()
        );
    }
}
