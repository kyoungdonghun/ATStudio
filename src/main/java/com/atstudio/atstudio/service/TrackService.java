package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.TagNamePolicy;
import com.atstudio.atstudio.common.validation.ValidationConstants;
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
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
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
    @org.springframework.beans.factory.annotation.Autowired
    private ListeningCatalogService catalog;
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.beans.factory.ObjectProvider<TrackService> self;

    private TrackService transactionalSelf() { return self == null ? this : self.getObject(); }


    private java.util.List<Long> selectedIds() {
        if (catalog != null) return catalog.snapshot().trackIds();
        if (listening.getCatalogSource() == com.atstudio.atstudio.config.ListeningProperties.CatalogSource.DATABASE) {
            throw new IllegalStateException("Catalog service required in database mode");
        }
        return listening.getTrackIds().stream().sorted().toList();
    }


    // Initialized for standalone service fixtures; Spring always injects the bound policy.
    @org.springframework.beans.factory.annotation.Autowired
    private com.atstudio.atstudio.config.ListeningProperties listening =
            new com.atstudio.atstudio.config.ListeningProperties();

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

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @com.atstudio.atstudio.service.storage.StorageReferencePreparation
    public TrackResponse createTrack(TrackCreateRequest request, MultipartFile audioFile,
                                     MultipartFile thumbnail, CustomUserDetails userDetails) {
        if (catalog == null || !catalog.databaseMode()) {
            return transactionalSelf().createTrackTransactional(request, audioFile, thumbnail, userDetails);
        }
        MultipartFile image = canonicalizeTrackThumbnail(thumbnail);
        AudioAnalysisResult analysis = analyzeAudio(audioFile);
        try (var audio = stagePrepared(audioFile, "tracks/audio");
             var cover = image == null ? null : stagePrepared(image, "tracks/thumbnail")) {
            return transactionalSelf().attachCreatedTrack(request, userDetails.getId(), analysis, audio, cover);
        }
    }

    private StorageMutationCoordinator.GeneratedWrite stagePrepared(MultipartFile file, String directory) {
        return storageMutationCoordinator.generate(StorageDomain.TRACK, StorageRoot.PUBLIC, directory,
                file.getOriginalFilename(), path -> {
                    try (var input = file.getInputStream()) {
                        java.nio.file.Files.copy(input, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException failure) { throw new java.io.UncheckedIOException(failure); }
                });
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public TrackResponse attachCreatedTrack(TrackCreateRequest request, Long userId, AudioAnalysisResult analysis,
            StorageMutationCoordinator.GeneratedWrite audio, StorageMutationCoordinator.GeneratedWrite cover) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        audio.attachToTransaction();
        if (cover != null) cover.attachToTransaction();
        Track track = Track.builder()
                .title(java.text.Normalizer.normalize(request.getTitle(), java.text.Normalizer.Form.NFC))
                .bpm(request.getBpm()).tonality(request.getTonality()).description(request.getDescription())
                .audioFile(audio.key()).thumbnail(cover == null ? null : cover.key())
                .duration(analysis.durationSeconds()).waveformData(analysis.waveformJson()).user(user).build();
        if (analysis.format() == AudioAnalysisFormat.WAV) track.enqueueAudio(audio.key(), true);
        track = trackRepository.save(track);
        return TrackResponse.fromAdmin(track, saveTrackTags(track, request.getTagIds()));
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public TrackResponse createTrackTransactional(TrackCreateRequest request, MultipartFile audioFile,
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

        if (analysis.format() == AudioAnalysisFormat.WAV) {
            track.enqueueAudio(audioFilePath, true);
        }

        track = trackRepository.save(track);

        List<Tag> tags = saveTrackTags(track, request.getTagIds());
        return TrackResponse.fromAdmin(track, tags);
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
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
        if (listening.isEnabled()) {
            var selected = selectedIds();
            spec = spec.and((root, query, cb) -> selected.isEmpty()
                    ? cb.disjunction() : root.get("id").in(selected));
        }
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

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public TrackResponse getTrack(Long trackId) {
        requireSelectedTrack(trackId);
        Track track = trackRepository.findByIdWithTags(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
        if (!track.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        List<Tag> tags = track.getTrackTags().stream().map(TrackTag::getTag).toList();
        return TrackResponse.fromPublic(track, tags);
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public StreamResource getStreamResource(Long trackId) {
        requireSelectedTrack(trackId);
        Track track = findActiveTrack(trackId);
        String listeningKey = track.listeningAudioFile();
        if (listeningKey == null) throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        Resource resource = storageService.loadAsResource(
                StorageRoot.PUBLIC,
                listeningKey);

        try {
            return new StreamResource(resource, resource.contentLength());
        } catch (IOException e) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Resource getPublicThumbnail(String filename) {
        if (!filename.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9]+")) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        String key = "tracks/thumbnail/" + filename;
        if (listening.isEnabled()) {
            var selected = selectedIds();
            if (selected.isEmpty()) throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
            // DB collation may match differently cased keys. Never use the untrusted request key
            // after an equality-only ownership check; pin the exact persisted approved key.
            key = trackRepository.findSelectedThumbnailKeys(key, selected).stream()
                    .filter(("tracks/thumbnail/" + filename)::equals).findFirst()
                    .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
        }
        return storageService.loadAsResource(StorageRoot.PUBLIC, key);
    }

    public Resource getThumbnailForAdmin(Long trackId) {
        Track track = findTrackById(trackId);
        String key = track.getThumbnail();
        if (key == null || !key.startsWith("tracks/thumbnail/")) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        return storageService.loadAsResource(StorageRoot.PUBLIC, key);
    }

    public record EditStamp(long generation, String audio, String stream, String pending, String claim,
            String thumbnail, String title, int bpm, String tonality, String description, boolean active,
            java.time.LocalDateTime updatedAt, List<Long> tagIds) { }

    private EditStamp editStamp(Track track) {
        return new EditStamp(track.getAudioGeneration(), track.getAudioFile(), track.getStreamAudioFile(),
                track.getPendingAudioFile(), track.getAudioClaimToken(), track.getThumbnail(), track.getTitle(),
                track.getBpm(), track.getTonality(), track.getDescription(), track.isActive(), track.getUpdatedAt(),
                track.getTrackTags().stream().map(tag -> tag.getTag().getId()).sorted().toList());
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public EditStamp prepareEditStamp(Long trackId) {
        catalog.requireUnselected(trackId);
        return editStamp(findTrackById(trackId));
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @com.atstudio.atstudio.service.storage.StorageReferencePreparation
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request,
            MultipartFile audioFile, MultipartFile thumbnail) {
        if (catalog == null || !catalog.databaseMode()) {
            return transactionalSelf().updateTrackTransactional(trackId, request, audioFile, thumbnail);
        }
        EditStamp stamp = transactionalSelf().prepareEditStamp(trackId);
        MultipartFile image = canonicalizeTrackThumbnail(thumbnail);
        AudioAnalysisResult analysis = audioFile != null && !audioFile.isEmpty() ? analyzeAudio(audioFile) : null;
        try (var audio = analysis == null ? null : stagePrepared(audioFile, "tracks/audio");
             var cover = image == null ? null : stagePrepared(image, "tracks/thumbnail")) {
            return transactionalSelf().attachUpdatedTrack(trackId, stamp, request, analysis, audio, cover);
        }
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public TrackResponse attachUpdatedTrack(Long trackId, EditStamp expected, TrackUpdateRequest request,
            AudioAnalysisResult analysis, StorageMutationCoordinator.GeneratedWrite audio,
            StorageMutationCoordinator.GeneratedWrite cover) {
        catalog.requireUnselected(trackId);
        Track track = findTrackForUpdate(trackId);
        if (!editStamp(track).equals(expected)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                    "Track changed during preparation; reload before deciding");
        }
        if (audio != null) audio.attachToTransaction();
        if (cover != null) cover.attachToTransaction();
        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());
        if (audio != null) {
            String oldPending = track.getPendingAudioFile();
            if (analysis.format() == AudioAnalysisFormat.WAV) {
                track.enqueueAudio(audio.key(), false);
            } else {
                String oldOriginal = track.getAudioFile();
                String oldStream = track.getStreamAudioFile();
                track.useDirectAudio(audio.key(), analysis.durationSeconds(), analysis.waveformJson());
                deleteUnreferencedAudioAfterCommit(oldOriginal);
                deleteUnreferencedAudioAfterCommit(oldStream);
            }
            deleteUnreferencedAudioAfterCommit(oldPending);
        }
        if (cover != null) {
            String oldThumbnail = track.getThumbnail();
            track.updateThumbnail(cover.key());
            deleteUnreferencedAudioAfterCommit(oldThumbnail);
        }
        if (request.getIsActive() != null) {
            if (request.getIsActive() && !track.isStreamReady()) throw new BusinessException(BUSINESS_ERROR.AUDIO_STREAM_NOT_READY);
            if (!request.getIsActive() && track.isActive()) cancelPendingAudio(track);
            track.updateIsActive(request.getIsActive());
        }
        List<Tag> tags;
        if (Boolean.TRUE.equals(request.getReplaceTags())) {
            trackTagRepository.deleteAllByTrack(track);
            tags = saveTrackTags(track, request.getTagIds());
        } else {
            tags = trackTagRepository.findAllWithTagByTrack(track).stream().map(TrackTag::getTag).toList();
        }
        return TrackResponse.fromAdmin(track, tags);
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public TrackResponse updateTrackTransactional(Long trackId, TrackUpdateRequest request,
                                     MultipartFile audioFile, MultipartFile thumbnail) {
        if (catalog != null) catalog.requireUnselected(trackId);
        MultipartFile canonicalThumbnail = canonicalizeTrackThumbnail(thumbnail);
        AudioAnalysisResult audioAnalysis = audioFile != null && !audioFile.isEmpty()
                ? analyzeAudio(audioFile)
                : null;
        Track track = findTrackForUpdate(trackId);

        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());

        if (audioAnalysis != null) {
            String oldPending = track.getPendingAudioFile();
            if (audioAnalysis.format() == AudioAnalysisFormat.WAV) {
                String pending = storageMutationCoordinator.store(
                        StorageDomain.TRACK, StorageRoot.PUBLIC, audioFile, "tracks/audio");
                track.enqueueAudio(pending, false);
            } else {
                String oldStream = track.getStreamAudioFile();
                String newAudio = storageMutationCoordinator.replace(
                        StorageDomain.TRACK, StorageRoot.PUBLIC, audioFile, "tracks/audio", track.getAudioFile());
                track.useDirectAudio(newAudio, audioAnalysis.durationSeconds(), audioAnalysis.waveformJson());
                deleteUnreferencedAudioAfterCommit(oldStream);
            }
            deleteUnreferencedAudioAfterCommit(oldPending);
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
            if (request.getIsActive() && !track.isStreamReady()) {
                throw new BusinessException(BUSINESS_ERROR.AUDIO_STREAM_NOT_READY);
            }
            // Explicit deactivation of a published track fences its outstanding replacement.
            if (!request.getIsActive() && track.isActive()) cancelPendingAudio(track);
            track.updateIsActive(request.getIsActive());
        }

        List<Tag> tags;
        if (Boolean.TRUE.equals(request.getReplaceTags())) {
            trackTagRepository.deleteAllByTrack(track);
            tags = saveTrackTags(track, request.getTagIds());
        } else {
            tags = trackTagRepository.findAllWithTagByTrack(track)
                    .stream().map(TrackTag::getTag).toList();
        }

        return TrackResponse.fromAdmin(track, tags);
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public void deleteTrack(Long trackId) {
        if (catalog != null) catalog.requireUnselected(trackId);
        Track track = findTrackForUpdate(trackId);

        cancelPendingAudio(track);

        // Issued licenses and download events remain evidence and quota input after deactivation.
        likeRepository.deleteAllByTrack(track);
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

    private void requireSelectedTrack(Long trackId) {
        if (listening.isEnabled() && !selectedIds().contains(trackId)) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
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

    public AudioProcessingResponse getAudioProcessing(Long trackId) {
        return AudioProcessingResponse.from(findTrackById(trackId));
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public AudioProcessingResponse retryAudioProcessing(Long trackId, long generation) {
        if (catalog != null) catalog.requireUnselected(trackId);
        Track track = findTrackForUpdate(trackId);
        if (track.getAudioGeneration() != generation || !track.canRetryAudio()) {
            throw new BusinessException(BUSINESS_ERROR.AUDIO_PROCESSING_CONFLICT);
        }
        track.retryAudio();
        return AudioProcessingResponse.from(track);
    }

    private Track findTrackForUpdate(Long trackId) {
        return trackRepository.findByIdForUpdate(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
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
        if (audioFile != null && audioFile.getSize() > ValidationConstants.AUDIO_MAX_SIZE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.IO_LARGE);
        }
        try {
            return audioAnalysisService.analyze(audioFile);
        } catch (AudioAnalysisException exception) {
            throw new BusinessException(BUSINESS_ERROR.AUDIO_ANALYSIS_FAILED, exception);
        }
    }

    private void cancelPendingAudio(Track track) {
        String pending = track.getPendingAudioFile();
        track.cancelAudio();
        deleteUnreferencedAudioAfterCommit(pending);
    }

    private void deleteUnreferencedAudioAfterCommit(String key) {
        if (key != null) storageMutationCoordinator.deleteAfterCommit(StorageDomain.TRACK, StorageRoot.PUBLIC, key);
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
