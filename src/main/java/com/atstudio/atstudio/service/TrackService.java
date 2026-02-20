package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.TrackTagId;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.spec.TrackSpecification;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        String audioFilePath = storageService.store(request.getAudioFile(), "tracks/audio");
        String thumbnailPath = (request.getThumbnail() != null && !request.getThumbnail().isEmpty())
                ? storageService.store(request.getThumbnail(), "tracks/thumbnail")
                : null;

        Track track = Track.builder()
                .title(request.getTitle())
                .bpm(request.getBpm())
                .tonality(request.getTonality())
                .description(request.getDescription())
                .audioFile(audioFilePath)
                .thumbnail(thumbnailPath)
                .user(user)
                .build();

        track = trackRepository.save(track);

        List<Tag> tags = saveTrackTags(track, request.getTagIds());
        return TrackResponse.from(track, tags);
    }

    public ResponseDTO<TrackListItemResponse> getTracks(TrackSearchRequest request) {
        Sort sort = "popular".equals(request.getSort())
                ? Sort.by(Sort.Direction.DESC, "playCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(Math.max(0, request.getPage() - 1), request.getSize(), sort);

        Specification<Track> spec = Specification.where(TrackSpecification.isActive())
                .and(TrackSpecification.titleContains(request.getKeyword()))
                .and(TrackSpecification.hasBpmMin(request.getBpmMin()))
                .and(TrackSpecification.hasBpmMax(request.getBpmMax()))
                .and(TrackSpecification.hasTonality(request.getTonality()))
                .and(TrackSpecification.hasTagWithNameAndType(request.getGenre(), "GENRE"))
                .and(TrackSpecification.hasTagWithNameAndType(request.getMood(), "MOOD"))
                .and(TrackSpecification.hasTagWithNameAndType(request.getInstrument(), "INSTRUMENT"));

        Page<Track> page = trackRepository.findAll(spec, pageable);

        List<Long> trackIds = page.getContent().stream().map(Track::getId).toList();
        Map<Long, List<Tag>> tagsByTrackId = buildTagsMap(trackIds);

        List<TrackListItemResponse> dataList = page.getContent().stream()
                .map(t -> TrackListItemResponse.from(t, tagsByTrackId.getOrDefault(t.getId(), List.of())))
                .toList();

        int total = (int) page.getTotalElements();
        PageInfo pageInfo = PageInfo.of(request.getPage(), request.getSize(), total, 10);

        return ResponseDTO.<TrackListItemResponse>builder()
                .message("Track list retrieved")
                .dataList(dataList)
                .pageInfo(pageInfo)
                .build();
    }

    public TrackResponse getTrack(Long trackId) {
        Track track = findActiveTrack(trackId);
        List<Tag> tags = trackTagRepository.findAllWithTagByTrack(track)
                .stream().map(TrackTag::getTag).toList();
        return TrackResponse.from(track, tags);
    }

    public Resource getStreamResource(Long trackId) {
        Track track = findActiveTrack(trackId);
        String filePath = (track.getPreviewFile() != null)
                ? track.getPreviewFile()
                : track.getAudioFile();
        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                resource = new UrlResource(Paths.get(track.getAudioFile()).toUri());
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request) {
        Track track = findTrackById(trackId);

        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());

        if (request.getAudioFile() != null && !request.getAudioFile().isEmpty()) {
            storageService.delete(track.getAudioFile());
            track.updateAudioFile(storageService.store(request.getAudioFile(), "tracks/audio"));
        }
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            storageService.delete(track.getThumbnail());
            track.updateThumbnail(storageService.store(request.getThumbnail(), "tracks/thumbnail"));
        }
        if (request.getIsActive() != null) {
            track.updateIsActive(request.getIsActive());
        }

        List<Tag> tags;
        if (request.getTagIds() != null) {
            trackTagRepository.deleteAllByTrack(track);
            tags = saveTrackTags(track, request.getTagIds());
        } else {
            tags = trackTagRepository.findAllWithTagByTrack(track)
                    .stream().map(TrackTag::getTag).toList();
        }

        return TrackResponse.from(track, tags);
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = findTrackById(trackId);
        track.deactivate();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Track findTrackById(Long trackId) {
        return trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    private Track findActiveTrack(Long trackId) {
        Track track = findTrackById(trackId);
        if (!track.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        return track;
    }

    private List<Tag> saveTrackTags(Track track, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return List.of();
        List<Tag> tags = tagIds.stream()
                .map(tagId -> tagRepository.findById(tagId)
                        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND)))
                .toList();
        List<TrackTag> trackTags = tags.stream()
                .map(tag -> TrackTag.builder()
                        .id(new TrackTagId(track.getId(), tag.getId()))
                        .track(track)
                        .tag(tag)
                        .build())
                .toList();
        trackTagRepository.saveAll(trackTags);
        return tags;
    }

    private Map<Long, List<Tag>> buildTagsMap(List<Long> trackIds) {
        if (trackIds.isEmpty()) return Map.of();
        return trackTagRepository.findAllWithTagByTrackIdIn(trackIds).stream()
                .collect(Collectors.groupingBy(
                        tt -> tt.getId().getTrackId(),
                        Collectors.mapping(TrackTag::getTag, Collectors.toList())
                ));
    }
}
