package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.AdminTrackAudioAnalysisDryRunItemResponse;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.service.audio.AudioAnalysisException;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminTrackAudioAnalysisService {

    static final int MAX_PAGE_SIZE = 100;
    static final int DURATION_TOLERANCE_SECONDS = 1;

    private final TrackRepository trackRepository;
    private final StorageService storageService;
    private final AudioAnalysisService audioAnalysisService;

    public ResponseDTO<AdminTrackAudioAnalysisDryRunItemResponse> dryRun(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.ASC, "id"));
        Page<Track> tracks = trackRepository.findAll(pageable);
        List<AdminTrackAudioAnalysisDryRunItemResponse> dataList = tracks.getContent().stream()
                .map(this::analyzeTrack)
                .toList();
        int total = tracks.getTotalElements() > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) tracks.getTotalElements();

        return ResponseDTO.<AdminTrackAudioAnalysisDryRunItemResponse>builder()
                .message("Audio analysis dry-run completed")
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    private AdminTrackAudioAnalysisDryRunItemResponse analyzeTrack(Track track) {
        AudioAnalysisFormat declaredFormat = AudioAnalysisFormat.fromFilename(track.getAudioFile());
        Resource resource;
        try {
            resource = storageService.loadAsResource(StorageRoot.PUBLIC, track.getAudioFile());
        } catch (RuntimeException exception) {
            return failure(
                    track,
                    false,
                    declaredFormat,
                    AdminTrackAudioAnalysisDryRunItemResponse.Status.UNREADABLE,
                    AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.RESTORE_OR_REUPLOAD_AUDIO);
        }

        try {
            AudioAnalysisResult result = audioAnalysisService.analyze(track.getAudioFile(), resource);
            return success(track, result);
        } catch (AudioAnalysisException exception) {
            AudioAnalysisFormat format = exception.getFormat() == AudioAnalysisFormat.UNKNOWN
                    ? declaredFormat
                    : exception.getFormat();
            if (exception.getReason() == AudioAnalysisException.Reason.UNREADABLE) {
                return failure(
                        track,
                        false,
                        format,
                        AdminTrackAudioAnalysisDryRunItemResponse.Status.UNREADABLE,
                        AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.RESTORE_OR_REUPLOAD_AUDIO);
            }
            if (exception.getReason() == AudioAnalysisException.Reason.UNSUPPORTED_FORMAT) {
                return failure(
                        track,
                        true,
                        format,
                        AdminTrackAudioAnalysisDryRunItemResponse.Status.UNSUPPORTED_FORMAT,
                        AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.REUPLOAD_SUPPORTED_AUDIO);
            }
            return failure(
                    track,
                    true,
                    format,
                    AdminTrackAudioAnalysisDryRunItemResponse.Status.ANALYSIS_FAILED,
                    AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.REUPLOAD_SUPPORTED_AUDIO);
        }
    }

    private AdminTrackAudioAnalysisDryRunItemResponse success(
            Track track,
            AudioAnalysisResult result) {
        int delta = result.durationSeconds() - track.getDuration();
        boolean storedWaveformPresent = hasStoredWaveform(track);
        boolean metadataMatches = track.getDuration() > 0
                && Math.abs(delta) <= DURATION_TOLERANCE_SECONDS
                && storedWaveformPresent;

        return new AdminTrackAudioAnalysisDryRunItemResponse(
                track.getId(),
                track.getTitle(),
                track.isActive(),
                true,
                track.getDuration(),
                result.durationSeconds(),
                delta,
                storedWaveformPresent,
                result.format(),
                metadataMatches
                        ? AdminTrackAudioAnalysisDryRunItemResponse.Status.MATCH
                        : AdminTrackAudioAnalysisDryRunItemResponse.Status.METADATA_MISMATCH,
                metadataMatches
                        ? AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.NONE
                        : AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.BACKFILL_ANALYSIS_METADATA,
                result.decodedFrameCount(),
                result.sampleRateHz(),
                result.channelCount());
    }

    private AdminTrackAudioAnalysisDryRunItemResponse failure(
            Track track,
            boolean readable,
            AudioAnalysisFormat format,
            AdminTrackAudioAnalysisDryRunItemResponse.Status status,
            AdminTrackAudioAnalysisDryRunItemResponse.Recommendation recommendation) {
        return new AdminTrackAudioAnalysisDryRunItemResponse(
                track.getId(),
                track.getTitle(),
                track.isActive(),
                readable,
                track.getDuration(),
                null,
                null,
                hasStoredWaveform(track),
                format,
                status,
                recommendation,
                null,
                null,
                null);
    }

    private boolean hasStoredWaveform(Track track) {
        return track.getWaveformData() != null && !track.getWaveformData().isBlank();
    }
}
