package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;

import java.time.LocalDateTime;
import java.util.List;

public record AdminTrackListItemResponse(
        Long id,
        String title,
        String artistName,
        int duration,
        int bpm,
        String tonality,
        String thumbnail,
        long playCount,
        long likeCount,
        long downloadCount,
        boolean isActive,
        List<TagResponse> tags,
        LocalDateTime createdAt
) {
    public static AdminTrackListItemResponse from(Track track, List<Tag> tags) {
        return new AdminTrackListItemResponse(
                track.getId(),
                track.getTitle(),
                track.getUser().getNickname(),
                track.getDuration(),
                track.getBpm(),
                track.getTonality(),
                track.getThumbnail(),
                track.getPlayCount(),
                track.getLikeCount(),
                track.getDownloadCount(),
                track.isActive(),
                tags.stream().map(TagResponse::from).toList(),
                track.getCreatedAt()
        );
    }
}
