package com.atstudio.atstudio.dto.like;

import com.atstudio.atstudio.entity.Like;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LikeResponse(
        Long trackId,
        String title,
        String artistName,
        int duration,
        Integer bpm,
        String tonality,
        String thumbnail,
        String waveformData,
        LocalDateTime createdAt
) {

    public static LikeResponse from(Like like) {
        return new LikeResponse(
                like.getTrack().getId(),
                like.getTrack().getTitle(),
                like.getTrack().getUser().getNickname(),
                like.getTrack().getDuration(),
                like.getTrack().getBpm(),
                like.getTrack().getTonality(),
                like.getTrack().getThumbnail(),
                like.getTrack().getWaveformData(),
                like.getCreatedAt()
        );
    }
}
