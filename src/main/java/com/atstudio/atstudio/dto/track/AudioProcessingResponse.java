package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.enums.AudioProcessingState;
import java.time.LocalDateTime;

public record AudioProcessingResponse(Long trackId, AudioProcessingState state, long generation,
        boolean streamReady, boolean retryAllowed, int attemptCount, String errorCode,
        LocalDateTime updatedAt) {
    public static AudioProcessingResponse from(Track track) {
        return new AudioProcessingResponse(track.getId(), track.getAudioProcessingState(),
                track.getAudioGeneration(), track.isStreamReady(), track.canRetryAudio(),
                track.getAudioAttemptCount(), track.getAudioErrorCode(), track.getUpdatedAt());
    }
}
