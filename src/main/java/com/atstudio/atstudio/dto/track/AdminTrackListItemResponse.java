package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;

import java.time.LocalDateTime;
import java.util.List;

public record AdminTrackListItemResponse(
        Long id,
        String title,
        int bpm,
        String tonality,
        String thumbnail,
        long playCount,
        boolean isActive,
        List<TagResponse> tags,
        LocalDateTime createdAt
) {
    public static AdminTrackListItemResponse from(Track track, List<Tag> tags) {
        return new AdminTrackListItemResponse(
                track.getId(),
                track.getTitle(),
                track.getBpm(),
                track.getTonality(),
                track.getThumbnail(),
                track.getPlayCount(),
                track.isActive(),
                tags.stream().map(TagResponse::from).toList(),
                track.getCreatedAt()
        );
    }
}
