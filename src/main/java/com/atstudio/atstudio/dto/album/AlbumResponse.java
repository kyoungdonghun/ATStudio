package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.Album;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        int trackCount,
        LocalDateTime createdAt
) {
    public static AlbumResponse from(Album album, int trackCount) {
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getThumbnail(),
                trackCount,
                album.getCreatedAt()
        );
    }
}
