package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.track.PlayableTrackResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayableTrackService {

    public static final int MAX_BATCH_SIZE = 100;

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;

    public List<PlayableTrackResponse> hydrate(List<Long> requestedIds) {
        List<Long> ids = new LinkedHashSet<>(requestedIds).stream()
                .limit(MAX_BATCH_SIZE + 1L)
                .toList();
        if (ids.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Playable track batch is limited to 100 IDs");
        }
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Track> tracks = trackRepository.findAllActiveByIdIn(ids);
        Map<Long, Track> tracksById = tracks.stream()
                .collect(Collectors.toMap(Track::getId, Function.identity()));
        Map<Long, List<Tag>> tagsByTrackId = trackTagRepository.findAllWithTagByTrackIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        trackTag -> trackTag.getId().getTrackId(),
                        Collectors.mapping(TrackTag::getTag, Collectors.toList())
                ));

        return ids.stream()
                .map(tracksById::get)
                .filter(track -> track != null)
                .map(track -> PlayableTrackResponse.from(
                        track,
                        tagsByTrackId.getOrDefault(track.getId(), List.of())))
                .toList();
    }
}
