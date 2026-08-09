package com.atstudio.atstudio.dto.download;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackDownload;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One row in the "다운로드 기록" page — a single historical download event
 * plus the track metadata needed to render the list row (title, thumbnail,
 * duration, tags, etc).
 */
public record DownloadHistoryItemResponse(
        Long downloadId,
        Long trackId,
        String title,
        String artistName,
        String thumbnail,
        int bpm,
        String tonality,
        int duration,
        String waveformData,
        List<TagResponse> tags,
        LocalDateTime downloadedAt
) {
    public static DownloadHistoryItemResponse from(TrackDownload td, List<Tag> tags) {
        Track t = td.getTrack();
        return new DownloadHistoryItemResponse(
                td.getId(),
                t.getId(),
                t.getTitle(),
                t.getUser().getNickname(),
                t.getThumbnail(),
                t.getBpm(),
                t.getTonality(),
                t.getDuration(),
                t.getWaveformData(),
                tags.stream().map(TagResponse::from).toList(),
                td.getDownloadedAt()
        );
    }
}
