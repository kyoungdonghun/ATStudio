package com.atstudio.atstudio.dto.playlist;

import com.atstudio.atstudio.entity.Playlist;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaylistListItemResponse(
        Long id,
        String title,
        String thumbnail,
        int trackCount,
        LocalDateTime createdAt
) {
    public static PlaylistListItemResponse from(Playlist playlist, int trackCount) {
        return new PlaylistListItemResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getThumbnail(),
                trackCount,
                playlist.getCreatedAt()
        );
    }
}
