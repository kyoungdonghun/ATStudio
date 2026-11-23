package com.atstudio.atstudio.dto.playlist;

import com.atstudio.atstudio.entity.Playlist;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaylistDetailResponse(
        Long id,
        String title,
        String description,
        String thumbnail,
        List<PlaylistTrackItemResponse> tracks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaylistDetailResponse from(Playlist playlist, List<PlaylistTrackItemResponse> tracks, String thumbnail) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                thumbnail,
                tracks,
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
