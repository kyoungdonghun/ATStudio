package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.entity.Album;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumListItemResponse(
        Long id,
        String title,
        String thumbnailUrl,
        int trackCount
) {
    public static AlbumListItemResponse from(Album album, int trackCount) {
        return new AlbumListItemResponse(
                album.getId(),
                album.getTitle(),
                album.getThumbnail(),
                trackCount
        );
    }
}
