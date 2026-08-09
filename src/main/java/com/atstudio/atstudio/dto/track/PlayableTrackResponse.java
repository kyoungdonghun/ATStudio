package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;

import java.util.List;

public record PlayableTrackResponse(
        Long id,
        String title,
        String artistName,
        int duration,
        String thumbnail,
        String waveformData,
        Integer bpm,
        String tonality,
        List<TagResponse> tags
) {
    public static PlayableTrackResponse from(Track track, List<Tag> tags) {
        return new PlayableTrackResponse(
                track.getId(),
                track.getTitle(),
                track.getUser().getNickname(),
                track.getDuration(),
                track.getThumbnail(),
                track.getWaveformData(),
                track.getBpm(),
                track.getTonality(),
                tags.stream().map(TagResponse::from).toList()
        );
    }
}
