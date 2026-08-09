package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.TagNamePolicy;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.TrackTagId;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.repository.spec.TrackSpecification;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.audio.AudioAnalysisException;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import com.atstudio.atstudio.service.storage.StorageWriteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrackService {

    private static final int MAX_PUBLIC_TRACK_PAGE_SIZE = 100;

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AudioAnalysisService audioAnalysisService;
    private final CanonicalImageService canonicalImageService;
    private final StorageService storageService;
    private final StorageMutationCoordinator storageMutationCoordinator;
    private final LikeRepository likeRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final LicenseRepository licenseRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final AlbumTrackRepository albumTrackRepository;

    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request, MultipartFile audioFile,
                                     MultipartFile thumbnail, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        MultipartFile canonicalThumbnail = canonicalizeTrackThumbnail(thumbnail);
        AudioAnalysisResult analysis = analyzeAudio(audioFile);

        List<StorageWriteRequest> writes = new ArrayList<>();
        writes.add(StorageWriteRequest.create(audioFile, "tracks/audio"));
        if (canonicalThumbnail != null) {
            writes.add(StorageWriteRequest.create(canonicalThumbnail, "tracks/thumbnail"));
        }
        List<String> storedKeys = storageMutationCoordinator.writeAll(
                StorageDomain.TRACK,
                StorageRoot.PUBLIC,
                writes);
        String audioFilePath = storedKeys.get(0);
        String thumbnailPath = storedKeys.size() > 1 ? storedKeys.get(1) : null;

        Track track = Track.builder()
                .title(java.text.Normalizer.normalize(request.getTitle(), java.text.Normalizer.Form.NFC))
                .bpm(request.getBpm())
                .tonality(request.getTonality())
                .description(request.getDescription())
                .audioFile(audioFilePath)
                .thumbnail(thumbnailPath)
                .duration(analysis.durationSeconds())
                .waveformData(analysis.waveformJson())
                .user(user)
                .build();

        track = trackRepository.save(track);

        List<Tag> tags = saveTrackTags(track, request.getTagIds());
        return TrackResponse.fromAdmin(track, tags);
    }

    public ResponseDTO<TrackListItemResponse> getTracks(TrackSearchRequest request) {
        validatePublicSearchPage(request);
        int pageNumber = request.getPage();
        int pageSize = request.getSize();

        Sort sort = switch (request.getSort() != null ? request.getSort() : "latest") {
            case "popular" -> Sort.by(Sort.Direction.DESC, "playCount");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "downloads" -> Sort.by(Sort.Direction.DESC, "downloadCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Specification<Track> spec = TrackSpecification.isActive();
        spec = addSpec(spec, TrackSpecification.keywordContains(request.getKeyword()));
        spec = addSpec(spec, TrackSpecification.hasBpmMin(request.getBpmMin()));
        spec = addSpec(spec, TrackSpecification.hasBpmMax(request.getBpmMax()));
        spec = addSpec(spec, TrackSpecification.hasTonality(request.getTonality()));
        spec = addSpec(spec, parseMultiTagSpec(request.getGenre(), "GENRE"));
        spec = addSpec(spec, parseMultiTagSpec(request.getMood(), "MOOD"));
        spec = addSpec(spec, parseMultiTagSpec(request.getInstrument(), "INSTRUMENT"));
        spec = addSpec(spec, parseMultiTagSpec(request.getUsage(), "USAGE"));

        Page<Track> page = trackRepository.findAll(spec, pageable);

        List<TrackListItemResponse> dataList = page.getContent().stream()
                .map(t -> {
                    List<Tag> tags = t.getTrackTags().stream().map(TrackTag::getTag).toList();
                    return TrackListItemResponse.from(t, tags);
                })
                .toList();

        int total = (int) page.getTotalElements();
        PageInfo pageInfo = PageInfo.of(pageNumber, pageSize, total, 10);

        return ResponseDTO.<TrackListItemResponse>builder()
                .message("Track list retrieved")
                .dataList(dataList)
                .pageInfo(pageInfo)
                .build();
    }

    public TrackResponse getTrack(Long trackId) {
        Track track = trackRepository.findByIdWithTags(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
        if (!track.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        List<Tag> tags = track.getTrackTags().stream().map(TrackTag::getTag).toList();
        return TrackResponse.fromPublic(track, tags);
    }

    public StreamResource getStreamResource(Long trackId) {
        Track track = findActiveTrack(trackId);
        Resource resource = storageService.loadAsResource(
                StorageRoot.PUBLIC,
                track.getAudioFile());

        try {
            return new StreamResource(resource, resource.contentLength());
        } catch (IOException e) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request,
                                     MultipartFile audioFile, MultipartFile thumbnail) {
        Track track = findTrackById(trackId);
        MultipartFile canonicalThumbnail = canonicalizeTrackThumbnail(thumbnail);
        AudioAnalysisResult audioAnalysis = audioFile != null && !audioFile.isEmpty()
                ? analyzeAudio(audioFile)
                : null;

        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());

        if (audioAnalysis != null) {
            String oldAudioFile = track.getAudioFile();
            String newAudioFile = storageMutationCoordinator.replace(
                    StorageDomain.TRACK,
                    StorageRoot.PUBLIC,
                    audioFile,
                    "tracks/audio",
                    oldAudioFile);
            track.updateAudioAnalysis(
                    newAudioFile,
                    audioAnalysis.durationSeconds(),
                    audioAnalysis.waveformJson());
        }
        if (canonicalThumbnail != null) {
            String oldThumbnail = track.getThumbnail();
            track.updateThumbnail(storageMutationCoordinator.replace(
                    StorageDomain.TRACK,
                    StorageRoot.PUBLIC,
                    canonicalThumbnail,
                    "tracks/thumbnail",
                    oldThumbnail));
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

        return TrackResponse.fromAdmin(track, tags);
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = findTrackById(trackId);

        // 관련 레코드 정리 (고아 레코드 방지)
        likeRepository.deleteAllByTrack(track);
        trackDownloadRepository.deleteAllByTrack(track);
        licenseRepository.deleteAllByTrack(track);
        playlistTrackRepository.deleteAllByIdTrackId(track.getId());
        albumTrackRepository.deleteAllByTrack(track);
        trackTagRepository.deleteAllByTrack(track);

        track.deactivate();
    }

    public TrackResponse getTrackForAdmin(Long trackId) {
        Track track = trackRepository.findByIdWithTags(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
        List<Tag> tags = track.getTrackTags().stream().map(TrackTag::getTag).toList();
        return TrackResponse.fromAdmin(track, tags);
    }

    public ResponseDTO<AdminTrackListItemResponse> getTracksForAdmin(Boolean isActive, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Track> spec = Specification.where(TrackSpecification.hasIsActive(isActive));
        spec = addSpec(spec, TrackSpecification.titleContains(keyword));

        Page<Track> trackPage = trackRepository.findAll(spec, pageable);

        List<AdminTrackListItemResponse> dataList = trackPage.getContent().stream()
                .map(t -> {
                    List<Tag> tags = t.getTrackTags().stream().map(TrackTag::getTag).toList();
                    return AdminTrackListItemResponse.from(t, tags);
                })
                .toList();

        int total = (int) trackPage.getTotalElements();
        PageInfo pageInfo = PageInfo.of(page, size, total, 10);

        return ResponseDTO.<AdminTrackListItemResponse>builder()
                .message("Admin track list retrieved")
                .dataList(dataList)
                .pageInfo(pageInfo)
                .build();
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

    private void validatePublicSearchPage(TrackSearchRequest request) {
        if (request.getPage() < 1
                || request.getSize() < 1
                || request.getSize() > MAX_PUBLIC_TRACK_PAGE_SIZE) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    public record StreamResource(Resource resource, long publicLength) {
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

    private Specification<Track> addSpec(Specification<Track> base, Specification<Track> other) {
        return other != null ? base.and(other) : base;
    }

    private AudioAnalysisResult analyzeAudio(MultipartFile audioFile) {
        try {
            return audioAnalysisService.analyze(audioFile);
        } catch (AudioAnalysisException exception) {
            throw new BusinessException(BUSINESS_ERROR.AUDIO_ANALYSIS_FAILED, exception);
        }
    }

    private MultipartFile canonicalizeTrackThumbnail(MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }
        return canonicalImageService.canonicalizeSquareTrackThumbnail(thumbnail);
    }

    private Specification<Track> parseMultiTagSpec(List<String> rawNames, String tagType) {
        if (rawNames == null || rawNames.isEmpty()) return null;
        List<String> names = rawNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(TagNamePolicy::canonicalize)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
        if (names.isEmpty()) return null;
        if (names.size() == 1) return TrackSpecification.hasTagWithNameAndType(names.get(0), tagType);
        return TrackSpecification.hasAllTagsWithType(names, tagType);
    }
}
