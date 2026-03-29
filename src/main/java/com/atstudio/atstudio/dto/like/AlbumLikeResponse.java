package com.atstudio.atstudio.dto.like;

import com.atstudio.atstudio.entity.AlbumLike;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumLikeResponse(
        Long albumId,
        String title,
        String description,
        String thumbnailUrl,
        int trackCount,
        long likeCount,
        LocalDateTime createdAt
) {

    public static AlbumLikeResponse from(AlbumLike albumLike, int trackCount) {
        return new AlbumLikeResponse(
                albumLike.getAlbum().getId(),
                albumLike.getAlbum().getTitle(),
                albumLike.getAlbum().getDescription(),
                albumLike.getAlbum().getThumbnail(),
                trackCount,
                albumLike.getAlbum().getLikeCount(),
                albumLike.getCreatedAt()
        );
    }
}
