package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.Album;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumListItemResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        int trackCount,
        long likeCount,
        LocalDateTime createdAt
) {
    public static AlbumListItemResponse from(Album album, int trackCount) {
        return new AlbumListItemResponse(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getThumbnail(),
                trackCount,
                album.getLikeCount(),
                album.getCreatedAt()
        );
    }
}
