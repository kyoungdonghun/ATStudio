package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.AlbumTrack;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumTrackItemResponse(
        Long trackId,
        String title,
        String artistName,
        String thumbnailUrl,
        int order
) {
    public static AlbumTrackItemResponse from(AlbumTrack albumTrack) {
        return new AlbumTrackItemResponse(
                albumTrack.getTrack().getId(),
                albumTrack.getTrack().getTitle(),
                albumTrack.getTrack().getUser() != null
                        ? albumTrack.getTrack().getUser().getNickname()
                        : null,
                albumTrack.getTrack().getThumbnail(),
                albumTrack.getTrackOrder()
        );
    }
}
