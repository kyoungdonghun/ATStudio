package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;

import java.time.LocalDateTime;
import java.util.List;

public record TrackListItemResponse(
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
        List<TagResponse> tags,
        LocalDateTime createdAt
) {
    public static TrackListItemResponse from(Track track, List<Tag> tags) {
        return new TrackListItemResponse(
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
                tags.stream().map(TagResponse::from).toList(),
                track.getCreatedAt()
        );
    }
}
