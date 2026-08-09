package com.atstudio.atstudio.service.audio;

/**
 * Immutable evidence from one decoded PCM pass. Duration is rounded to the nearest
 * whole second; non-empty audio is clamped to a minimum of one second.
 */
public record AudioAnalysisResult(
        int durationSeconds,
        String waveformJson,
        AudioAnalysisFormat format,
        long decodedFrameCount,
        int sampleRateHz,
        int channelCount) {

    public AudioAnalysisResult {
        if (durationSeconds < 1
                || waveformJson == null
                || waveformJson.isBlank()
                || format == null
                || format == AudioAnalysisFormat.UNKNOWN
                || decodedFrameCount < 1
                || sampleRateHz < 1
                || channelCount < 1) {
            throw new IllegalArgumentException("Audio analysis result must contain complete PCM evidence");
        }
    }
}
