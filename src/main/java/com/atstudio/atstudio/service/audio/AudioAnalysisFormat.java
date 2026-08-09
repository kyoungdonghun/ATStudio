package com.atstudio.atstudio.service.audio;

import java.util.Locale;

public enum AudioAnalysisFormat {
    MP3,
    WAV,
    UNKNOWN;

    public static AudioAnalysisFormat fromFilename(String filename) {
        if (filename == null) {
            return UNKNOWN;
        }
        String normalized = filename.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".mp3")) {
            return MP3;
        }
        if (normalized.endsWith(".wav")) {
            return WAV;
        }
        return UNKNOWN;
    }
}
