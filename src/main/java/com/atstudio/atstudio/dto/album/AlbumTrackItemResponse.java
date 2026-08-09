package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.AlbumTrack;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumTrackItemResponse(
        Long trackId,
        String title,
        String artistName,
        String thumbnailUrl,
        int duration,
        String waveformData,
        int order
) {
    public static AlbumTrackItemResponse from(AlbumTrack albumTrack) {
        return new AlbumTrackItemResponse(
                albumTrack.getTrack().getId(),
                albumTrack.getTrack().getTitle(),
                albumTrack.getTrack().getUser().getNickname(),
                albumTrack.getTrack().getThumbnail(),
                albumTrack.getTrack().getDuration(),
                albumTrack.getTrack().getWaveformData(),
                albumTrack.getTrackOrder()
        );
    }
}
