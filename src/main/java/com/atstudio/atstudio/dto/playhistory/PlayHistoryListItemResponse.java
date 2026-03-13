package com.atstudio.atstudio.dto.playhistory;

import com.atstudio.atstudio.entity.PlayHistory;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayHistoryListItemResponse(
        Long id,
        TrackSummary track,
        LocalDateTime playedAt
) {

    public record TrackSummary(Long id, String title, String artistName, String thumbnail) {
    }

    public static PlayHistoryListItemResponse from(PlayHistory h) {
        return new PlayHistoryListItemResponse(
                h.getId(),
                new TrackSummary(
                        h.getTrack().getId(),
                        h.getTrack().getTitle(),
                        h.getTrack().getUser().getNickname(),
                        h.getTrack().getThumbnail()
                ),
                h.getPlayedAt()
        );
    }
}
