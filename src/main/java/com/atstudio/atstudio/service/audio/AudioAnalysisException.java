package com.atstudio.atstudio.service.audio;

import lombok.Getter;

@Getter
public class AudioAnalysisException extends RuntimeException {

    private final Reason reason;
    private final AudioAnalysisFormat format;

    public AudioAnalysisException(Reason reason, AudioAnalysisFormat format) {
        super(reason.name());
        this.reason = reason;
        this.format = format;
    }

    public AudioAnalysisException(Reason reason, AudioAnalysisFormat format, Throwable cause) {
        super(reason.name(), cause);
        this.reason = reason;
        this.format = format;
    }

    public enum Reason {
        EMPTY_FILE,
        UNSUPPORTED_FORMAT,
        UNREADABLE,
        INVALID_AUDIO,
        NO_DECODABLE_FRAMES
    }
}
