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
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.repository.spec.TrackSpecification;
import com.atstudio.atstudio.security.CustomUserDetails;
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrackService {

    private static final String PREVIEW_DIRECTORY_PREFIX = "tracks/preview/";

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final StorageMutationCoordinator storageMutationCoordinator;
    private final LikeRepository likeRepository;
    private final DownloadQueueRepository downloadQueueRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final LicenseRepository licenseRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final AlbumTrackRepository albumTrackRepository;

    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request, MultipartFile audioFile,
                                     MultipartFile thumbnail, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        int duration = extractDuration(audioFile);
        String waveformData = extractWaveformPeaks(audioFile);

        List<StorageWriteRequest> writes = new ArrayList<>();
        writes.add(StorageWriteRequest.create(audioFile, "tracks/audio"));
        if (thumbnail != null && !thumbnail.isEmpty()) {
            writes.add(StorageWriteRequest.create(thumbnail, "tracks/thumbnail"));
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
                .duration(duration)
                .waveformData(waveformData)
                .user(user)
                .build();

        track = trackRepository.save(track);

        List<Tag> tags = saveTrackTags(track, request.getTagIds());
        return TrackResponse.fromAdmin(track, tags);
    }

    public ResponseDTO<TrackListItemResponse> getTracks(TrackSearchRequest request) {
        Sort sort = switch (request.getSort() != null ? request.getSort() : "latest") {
            case "popular" -> Sort.by(Sort.Direction.DESC, "playCount");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "downloads" -> Sort.by(Sort.Direction.DESC, "downloadCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(Math.max(0, request.getPage() - 1), request.getSize(), sort);

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
        return TrackResponse.fromPublic(track, tags);
    }

    public StreamResource getStreamResource(Long trackId) {
        Track track = findActiveTrack(trackId);
        boolean hasPreviewFile = isDedicatedPreviewFile(
                track.getPreviewFile(),
                track.getAudioFile());
        String filePath = hasPreviewFile
                ? track.getPreviewFile()
                : track.getAudioFile();
        Resource resource = storageService.loadAsResource(StorageRoot.PUBLIC, filePath);

        try {
            long resourceLength = resource.contentLength();
            long publicLength = hasPreviewFile
                    ? resourceLength
                    : calculateOriginalFallbackLength(resourceLength, track.getDuration());
            return new StreamResource(resource, publicLength);
        } catch (IOException e) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request,
                                     MultipartFile audioFile, MultipartFile thumbnail) {
        Track track = findTrackById(trackId);

        track.update(request.getTitle(), request.getBpm(), request.getTonality(), request.getDescription());

        if (audioFile != null && !audioFile.isEmpty()) {
            String oldAudioFile = track.getAudioFile();
            String waveformData = extractWaveformPeaks(audioFile);
            track.updateAudioFile(storageMutationCoordinator.replace(
                    StorageDomain.TRACK,
                    StorageRoot.PUBLIC,
                    audioFile,
                    "tracks/audio",
                    oldAudioFile));
            track.updateWaveformData(waveformData);
        }
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String oldThumbnail = track.getThumbnail();
            track.updateThumbnail(storageMutationCoordinator.replace(
                    StorageDomain.TRACK,
                    StorageRoot.PUBLIC,
                    thumbnail,
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
        downloadQueueRepository.deleteAllByTrack(track);
        playHistoryRepository.deleteAllByTrack(track);
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

    private long calculateOriginalFallbackLength(long resourceLength, int durationSeconds) {
        if (resourceLength <= 1) {
            return 0;
        }

        double previewRatio = durationSeconds > 0
                ? Math.min(30.0, durationSeconds * 0.5) / durationSeconds
                : 0.25;
        long estimatedLength = (long) Math.floor(resourceLength * previewRatio);

        return Math.max(1, Math.min(resourceLength - 1, estimatedLength));
    }

    private boolean isDedicatedPreviewFile(String previewFile, String audioFile) {
        String normalizedPreview = normalizeStoragePath(previewFile);
        if (normalizedPreview == null
                || !normalizedPreview.startsWith(PREVIEW_DIRECTORY_PREFIX)) {
            return false;
        }

        String normalizedAudio = normalizeStoragePath(audioFile);
        return normalizedAudio == null
                || !normalizedPreview.equalsIgnoreCase(normalizedAudio);
    }

    private String normalizeStoragePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return null;
        }

        try {
            Path normalizedPath = Paths.get(storagePath.replace('\\', '/')).normalize();
            if (normalizedPath.isAbsolute()) {
                return null;
            }
            return normalizedPath.toString().replace('\\', '/');
        } catch (InvalidPathException e) {
            return null;
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

    /**
     * Extract 200-point waveform peak data from an audio file.
     * WAV → pure Java RIFF parser (no external lib).
     * MP3/other → Java Sound SPI + mp3spi.
     * Returns "[0.12,0.45,...]" or null on failure.
     */
    private String extractWaveformPeaks(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String name = file.getOriginalFilename();
        if (name == null) return null;
        try {
            if (name.toLowerCase().endsWith(".wav")) {
                return extractWavPeaks(file);
            } else {
                return extractAudioSystemPeaks(file);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(TrackService.class)
                    .warn("[waveform] extraction failed for '{}': {}", name, e.toString());
            return null;
        }
    }

    /**
     * Pure Java WAV peak extractor — no external dependencies.
     * Handles PCM 8/16/24-bit, mono/stereo, and arbitrary chunk order.
     */
    private String extractWavPeaks(MultipartFile file) throws IOException {
        try (InputStream is = new java.io.BufferedInputStream(file.getInputStream())) {
            byte[] riff = new byte[12];
            if (is.read(riff) < 12) return null;
            if (!"RIFF".equals(new String(riff, 0, 4)) || !"WAVE".equals(new String(riff, 8, 4))) return null;

            int numChannels = 1;
            int bitsPerSample = 16;

            // Scan chunks until "data"
            byte[] chunkHdr = new byte[8];
            while (is.read(chunkHdr) == 8) {
                String id = new String(chunkHdr, 0, 4);
                int size = ByteBuffer.wrap(chunkHdr, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if ("fmt ".equals(id) && size >= 16) {
                    byte[] fmt = new byte[size];
                    is.read(fmt);
                    numChannels   = ByteBuffer.wrap(fmt, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                    bitsPerSample = ByteBuffer.wrap(fmt, 14, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                } else if ("data".equals(id)) {
                    // PCM bytes follow immediately
                    long dataSize  = Integer.toUnsignedLong(size);
                    int  bytesPerSample = Math.max(1, bitsPerSample / 8);
                    int  frameSize = bytesPerSample * Math.max(1, numChannels);
                    long totalFrames = dataSize / frameSize;
                    return peaksFromPcmStream(is, totalFrames, numChannels, bitsPerSample, frameSize);
                } else {
                    // skip unknown / extra fmt bytes (word-aligned)
                    long skip = size + (size % 2 != 0 ? 1 : 0);
                    is.skip(skip);
                }
            }
        }
        return null;
    }

    /**
     * Java Sound SPI path for MP3 and other formats.
     * Keeps source channel count to avoid conversion providers that don't support downmix.
     */
    private String extractAudioSystemPeaks(MultipartFile file) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(
                new java.io.BufferedInputStream(file.getInputStream(), 65536));

        AudioFormat src = ais.getFormat();
        float sr       = src.getSampleRate() > 0 ? src.getSampleRate() : 44100f;
        int channels   = src.getChannels() > 0 ? src.getChannels() : 2; // keep source channels

        AudioFormat pcm = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, sr, 16,
                channels, 2 * channels, sr, false);
        AudioInputStream pcmAis = AudioSystem.getAudioInputStream(pcm, ais);

        long frames = pcmAis.getFrameLength();
        if (frames <= 0) {
            frames = (long) (file.getSize() * sr / (128L * 1024 / 8));
        }
        int frameSize = 2 * channels;
        String result = peaksFromPcmStream(pcmAis, frames, channels, 16, frameSize);
        pcmAis.close();
        return result;
    }

    /**
     * Shared peak-bin extraction over a raw PCM stream.
     * Uses chunk-based approach — does NOT rely on totalFrames estimation.
     * Reads the entire stream, accumulates max peak per CHUNK frames,
     * then resamples the chunks into exactly PEAKS output bins.
     * This avoids the "half-filled" artifact when bitrate estimation is wrong.
     */
    private String peaksFromPcmStream(InputStream is, long ignoredTotalFrames,
                                      int channels, int bitsPerSample, int frameSize) throws IOException {
        final int PEAKS  = 200;
        final int CHUNK  = 1000; // frames per accumulation chunk
        final java.util.List<Double> chunkPeaks = new java.util.ArrayList<>(10000);

        byte[] buf       = new byte[4096];
        int    n;
        int    bytesPerSample = Math.max(1, bitsPerSample / 8);
        int    chunkFrameIdx  = 0;
        double chunkMax       = 0.0;

        while ((n = is.read(buf)) != -1) {
            int i = 0;
            while (i + frameSize <= n) {
                double peak = 0;
                for (int ch = 0; ch < channels; ch++) {
                    int off = i + ch * bytesPerSample;
                    double sample;
                    if (bitsPerSample == 16) {
                        short s = (short) ((buf[off] & 0xFF) | (buf[off + 1] << 8));
                        sample = Math.abs(s) / 32768.0;
                    } else if (bitsPerSample == 24) {
                        int s = (buf[off] & 0xFF) | ((buf[off+1] & 0xFF) << 8) | (buf[off+2] << 16);
                        sample = Math.abs(s) / 8388608.0;
                    } else { // 8-bit unsigned
                        sample = Math.abs((buf[off] & 0xFF) - 128) / 128.0;
                    }
                    peak = Math.max(peak, sample);
                }
                chunkMax = Math.max(chunkMax, peak);
                i += frameSize;
                if (++chunkFrameIdx >= CHUNK) {
                    chunkPeaks.add(chunkMax);
                    chunkMax = 0.0;
                    chunkFrameIdx = 0;
                }
            }
        }
        if (chunkFrameIdx > 0) chunkPeaks.add(chunkMax); // last partial chunk

        int total = chunkPeaks.size();

        // Resample chunks → PEAKS output bins (max-pool within each range)
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < PEAKS; i++) {
            if (i > 0) sb.append(",");
            double val = 0.0;
            if (total > 0) {
                int start = (int) ((long) i       * total / PEAKS);
                int end   = (int) ((long) (i + 1) * total / PEAKS);
                if (end <= start) end = start + 1;
                for (int c = start; c < end && c < total; c++) {
                    val = Math.max(val, chunkPeaks.get(c));
                }
            }
            sb.append(String.format("%.3f", val));
        }
        sb.append("]");
        return sb.toString();
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

    private Specification<Track> parseMultiTagSpec(String csv, String tagType) {
        if (csv == null || csv.isBlank()) return null;
        List<String> names = java.util.Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (names.isEmpty()) return null;
        if (names.size() == 1) return TrackSpecification.hasTagWithNameAndType(names.get(0), tagType);
        return TrackSpecification.hasAllTagsWithType(names, tagType);
    }
}
