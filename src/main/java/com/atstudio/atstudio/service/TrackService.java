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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    public TrackResponse createTrack(TrackCreateRequest request, MultipartFile audioFile,
                                     MultipartFile thumbnail, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        String audioFilePath = storageService.store(audioFile, "tracks/audio");
        String thumbnailPath = (thumbnail != null && !thumbnail.isEmpty())
                ? storageService.store(thumbnail, "tracks/thumbnail")
                : null;

        int duration = extractDuration(audioFile);

        Track track = Track.builder()
                .title(request.getTitle())
                .bpm(request.getBpm())
                .tonality(request.getTonality())
                .description(request.getDescription())
                .audioFile(audioFilePath)
                .thumbnail(thumbnailPath)
                .duration(duration)
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

        Specification<Track> spec = TrackSpecification.isActive();
        spec = addSpec(spec, TrackSpecification.titleContains(request.getKeyword()));
        spec = addSpec(spec, TrackSpecification.hasBpmMin(request.getBpmMin()));
        spec = addSpec(spec, TrackSpecification.hasBpmMax(request.getBpmMax()));
        spec = addSpec(spec, TrackSpecification.hasTonality(request.getTonality()));
        spec = addSpec(spec, TrackSpecification.hasTagWithNameAndType(request.getGenre(), "GENRE"));
        spec = addSpec(spec, TrackSpecification.hasTagWithNameAndType(request.getMood(), "MOOD"));
        spec = addSpec(spec, TrackSpecification.hasTagWithNameAndType(request.getInstrument(), "INSTRUMENT"));

        Page<Track> page = trackRepository.findAll(spec, pageable);

        List<TrackListItemResponse> dataList = page.getContent().stream()
                .map(t -> {
                    List<Tag> tags = t.getTrackTags().stream().map(TrackTag::getTag).toList();
                    return TrackListItemResponse.from(t, tags);
                })
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
        Track track = trackRepository.findByIdWithTags(trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
        if (!track.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
        List<Tag> tags = track.getTrackTags().stream().map(TrackTag::getTag).toList();
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
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request,
                                     MultipartFile audioFile, MultipartFile thumbnail) {
        Track track = findTrackById(trackId);

        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());

        if (audioFile != null && !audioFile.isEmpty()) {
            storageService.delete(track.getAudioFile());
            track.updateAudioFile(storageService.store(audioFile, "tracks/audio"));
        }
        if (thumbnail != null && !thumbnail.isEmpty()) {
            storageService.delete(track.getThumbnail());
            track.updateThumbnail(storageService.store(thumbnail, "tracks/thumbnail"));
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
        trackTagRepository.deleteAllByTrack(track);
        track.deactivate();
    }

    public ResponseDTO<AdminTrackListItemResponse> getTracksForAdmin(Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Track> spec = Specification.where(TrackSpecification.hasIsActive(isActive));

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

    /**
     * Extract duration (seconds) from uploaded audio file.
     * Supports WAV (RIFF header) and falls back to 0 for unsupported formats.
     */
    private int extractDuration(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".wav")) {
            return extractWavDuration(file);
        }
        // MP3 and other formats: estimate from file size (128kbps assumed)
        if (filename != null && filename.toLowerCase().endsWith(".mp3")) {
            return (int) (file.getSize() / (128 * 1024 / 8));
        }
        return 0;
    }

    private int extractWavDuration(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[44];
            if (is.read(header) < 44) return 0;

            // WAV header: bytes 28-31 = byte rate (little-endian)
            int byteRate = ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (byteRate <= 0) return 0;

            return (int) (file.getSize() / byteRate);
        } catch (IOException e) {
            return 0;
        }
    }

}
