package com.atstudio.atstudio.service.audio;

public class AudioTranscodeException extends RuntimeException {
    public enum Code {
        FFMPEG_NOT_FOUND, AUDIO_TRANSCODE_TIMEOUT, AUDIO_TRANSCODE_FAILED,
        AUDIO_OUTPUT_INVALID, AUDIO_OUTPUT_TOO_LARGE, AUDIO_INPUT_UNAVAILABLE, AUDIO_STORAGE_FAILED,
        AUDIO_PROCESSING_INTERRUPTED, AUDIO_FORMAT_UNSUPPORTED, AUDIO_PROCESS_TERMINATION_FAILED
    }

    private final Code code;

    public AudioTranscodeException(Code code) {
        super(code.name());
        this.code = code;
    }

    public Code code() { return code; }
}
