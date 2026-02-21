package com.atstudio.atstudio.dto.downloadqueue;

import com.atstudio.atstudio.entity.DownloadQueue;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadQueueResponse(
        Long trackId,
        String title,
        Integer bpm,
        String tonality,
        String thumbnail,
        LocalDateTime createdAt
) {

    public static DownloadQueueResponse from(DownloadQueue dq) {
        return new DownloadQueueResponse(
                dq.getTrack().getId(),
                dq.getTrack().getTitle(),
                dq.getTrack().getBpm(),
                dq.getTrack().getTonality(),
                dq.getTrack().getThumbnail(),
                dq.getCreatedAt()
        );
    }
}
