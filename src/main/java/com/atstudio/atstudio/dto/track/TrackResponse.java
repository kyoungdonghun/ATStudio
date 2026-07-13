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
        long likeCount,
        long downloadCount,
        String waveformData,
        List<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TrackResponse fromPublic(Track track, List<Tag> tags) {
        return from(track, tags, null);
    }

    public static TrackResponse fromAdmin(Track track, List<Tag> tags) {
        return from(track, tags, track.getAudioFile());
    }

    private static TrackResponse from(Track track, List<Tag> tags, String audioFile) {
        return new TrackResponse(
                track.getId(),
                track.getTitle(),
                track.getUser().getNickname(),
                track.getDuration(),
                track.getBpm(),
                track.getTonality(),
                track.getDescription(),
                audioFile,
                track.getThumbnail(),
                track.isActive(),
                track.getPlayCount(),
                track.getLikeCount(),
                track.getDownloadCount(),
                track.getWaveformData(),
                tags.stream().map(TagResponse::from).toList(),
                track.getCreatedAt(),
                track.getUpdatedAt()
        );
    }
}
