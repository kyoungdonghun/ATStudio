package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;

public record AdminTrackAudioAnalysisDryRunItemResponse(
        Long trackId,
        String title,
        boolean isActive,
        boolean readable,
        int storedDurationSeconds,
        Integer analyzedDurationSeconds,
        Integer durationDeltaSeconds,
        boolean storedWaveformPresent,
        AudioAnalysisFormat format,
        Status status,
        Recommendation recommendation,
        Long decodedFrameCount,
        Integer sampleRateHz,
        Integer channelCount) {

    public enum Status {
        MATCH,
        METADATA_MISMATCH,
        UNREADABLE,
        UNSUPPORTED_FORMAT,
        ANALYSIS_FAILED
    }

    public enum Recommendation {
        NONE,
        BACKFILL_ANALYSIS_METADATA,
        RESTORE_OR_REUPLOAD_AUDIO,
        REUPLOAD_SUPPORTED_AUDIO
    }
}
