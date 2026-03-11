package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;

import java.time.LocalDateTime;
import java.util.List;

public record TrackResponse(
        Long id,
        String title,
        String artistName,
        int duration,
        int bpm,
        String tonality,
        String description,
        String audioFile,
        String thumbnail,
        boolean isActive,
        long playCount,
        List<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TrackResponse from(Track track, List<Tag> tags) {
        return new TrackResponse(
                track.getId(),
                track.getTitle(),
                track.getUser().getNickname(),
                track.getDuration(),
                track.getBpm(),
                track.getTonality(),
                track.getDescription(),
                track.getAudioFile(),
                track.getThumbnail(),
                track.isActive(),
                track.getPlayCount(),
                tags.stream().map(TagResponse::from).toList(),
                track.getCreatedAt(),
                track.getUpdatedAt()
        );
    }
}
