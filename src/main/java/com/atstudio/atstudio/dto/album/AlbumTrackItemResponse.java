package com.atstudio.atstudio.dto.album;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumTrackItemResponse(
        Long trackId,
        String title,
        String artistName,
        String thumbnailUrl,
        int order,
        int duration,
        int bpm,
        String tonality,
        long playCount,
        long likeCount,
        long downloadCount,
        String waveformData,
        List<TagResponse> tags,
        LocalDateTime createdAt
) {
    public static AlbumTrackItemResponse from(AlbumTrack albumTrack) {
        Track track = albumTrack.getTrack();
        List<TagResponse> tags = track.getTrackTags().stream()
                .map(TrackTag::getTag)
                .map(TagResponse::from)
                .toList();

        return new AlbumTrackItemResponse(
                track.getId(),
                track.getTitle(),
                track.getUser() != null
                        ? track.getUser().getNickname()
                        : null,
                track.getThumbnail(),
                albumTrack.getTrackOrder(),
                track.getDuration(),
                track.getBpm(),
                track.getTonality(),
                track.getPlayCount(),
                track.getLikeCount(),
                track.getDownloadCount(),
                track.getWaveformData(),
                tags,
                track.getCreatedAt()
        );
    }
}
