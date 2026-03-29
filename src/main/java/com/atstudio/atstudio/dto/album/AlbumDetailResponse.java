package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.Album;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumDetailResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        List<AlbumTrackItemResponse> tracks,
        long likeCount,
        LocalDateTime createdAt
) {
    public static AlbumDetailResponse from(Album album, List<AlbumTrackItemResponse> tracks) {
        return new AlbumDetailResponse(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getThumbnail(),
                tracks,
                album.getLikeCount(),
                album.getCreatedAt()
        );
    }
}
