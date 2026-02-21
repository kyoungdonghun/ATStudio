package com.atstudio.atstudio.dto.playlist;

import com.atstudio.atstudio.entity.Playlist;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaylistResponse(
        Long id,
        String title,
        String description,
        String thumbnail,
        int trackCount,
        LocalDateTime createdAt
) {
    public static PlaylistResponse from(Playlist playlist, int trackCount) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getThumbnail(),
                trackCount,
                playlist.getCreatedAt()
        );
    }
}
