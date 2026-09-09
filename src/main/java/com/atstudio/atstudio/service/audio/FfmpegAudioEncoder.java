package com.atstudio.atstudio.service.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.atstudio.atstudio.service.audio.AudioTranscodeException.Code.*;

@Service
public class FfmpegAudioEncoder {
    private static final int[] MP3_128K_SAMPLE_RATES = {16000, 22050, 24000, 32000, 44100, 48000};
    private static final long OUTPUT_BUDGET_BYTES = 256L * 1024 * 1024;
    private static final long OUTPUT_FRAMING_BYTES = 65536;
    private static final Set<String> CHILD_ENVIRONMENT_ALLOWLIST = Set.of(
            "SYSTEMROOT", "WINDIR", "SYSTEMDRIVE", "PATH", "PATHEXT", "TEMP", "TMP",
            "LANG", "LC_ALL", "LC_CTYPE", "TZ");
    private final String executable;
    private final long timeoutSeconds;

    public FfmpegAudioEncoder(@Value("${app.audio.ffmpeg-path:ffmpeg}") String executable,
            @Value("${app.audio.timeout-seconds:300}") long timeoutSeconds) {
        if (executable == null || executable.isBlank() || executable.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("A non-blank FFmpeg executable is required");
        }
        this.executable = executable;
        if (timeoutSeconds < 1 || timeoutSeconds > 1800) {
            throw new IllegalArgumentException("Audio timeout must be between 1 and 1800 seconds");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public void encode(Path source, Path target, AudioAnalysisResult input) {
        OutputFormat outputFormat = outputFormat(input);
        long outputLimit = outputByteLimit(input);
        if (!source.isAbsolute() || !target.isAbsolute() || source.equals(target)
                || source.toUri().getAuthority() != null || target.toUri().getAuthority() != null
                || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
                || Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new AudioTranscodeException(AUDIO_INPUT_UNAVAILABLE);
        }
        // Adapt only the derivative to MP3 compatibility; FFmpeg supplies its standard channel downmix.
        List<String> command = List.of(executable, "-nostdin", "-hide_banner", "-loglevel", "error", "-n",
                "-protocol_whitelist", "file", "-f", "wav", "-i", source.toString(),
                "-map", "0:a:0", "-vn", "-sn", "-dn", "-map_metadata", "-1",
                "-c:a", "libmp3lame", "-b:a", "128k", "-abr", "0", "-threads", "1",
                "-ar", Integer.toString(outputFormat.sampleRateHz()), "-ac", Integer.toString(outputFormat.channelCount()),
                "-fs", Long.toString(outputLimit), "-f", "mp3", target.toString());
        Process process;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().keySet().removeIf(
                    key -> !CHILD_ENVIRONMENT_ALLOWLIST.contains(key.toUpperCase(Locale.ROOT)));
            // Discard both pipes: no unbounded buffering/log file or stderr/command disclosure.
            process = builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        } catch (IOException | RuntimeException exception) {
            throw new AudioTranscodeException(FFMPEG_NOT_FOUND);
        }
        try {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new AudioTranscodeException(AUDIO_TRANSCODE_TIMEOUT);
            }
            if (process.exitValue() != 0) throw new AudioTranscodeException(AUDIO_TRANSCODE_FAILED);
            // -fs can stop successfully at its bound: hitting it is never a successful full-length result.
            if (Files.size(target) >= outputLimit) throw new AudioTranscodeException(AUDIO_OUTPUT_INVALID);
        } catch (IOException exception) {
            throw new AudioTranscodeException(AUDIO_OUTPUT_INVALID);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AudioTranscodeException(AUDIO_PROCESSING_INTERRUPTED);
        } finally {
            if (process.isAlive()) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                // Reap the child before releasing its durable input/output ownership.
                boolean interrupted = Thread.interrupted();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                try {
                    while (process.isAlive()) {
                        long remaining = deadline - System.nanoTime();
                        if (remaining <= 0) throw new AudioTranscodeException(AUDIO_PROCESS_TERMINATION_FAILED);
                        try {
                            if (process.waitFor(remaining, TimeUnit.NANOSECONDS)) break;
                            throw new AudioTranscodeException(AUDIO_PROCESS_TERMINATION_FAILED);
                        } catch (InterruptedException exception) {
                            interrupted = true;
                        }
                    }
                } finally {
                    if (interrupted) Thread.currentThread().interrupt();
                }
            }
        }
    }

    public record OutputFormat(int sampleRateHz, int channelCount) { }

    public static OutputFormat outputFormat(AudioAnalysisResult input) {
        if (input == null || input.format() != AudioAnalysisFormat.WAV
                || input.sampleRateHz() < 1 || input.channelCount() < 1 || input.channelCount() > 32) {
            throw new AudioTranscodeException(AUDIO_FORMAT_UNSUPPORTED);
        }
        int nearest = MP3_128K_SAMPLE_RATES[0];
        for (int candidate : MP3_128K_SAMPLE_RATES) {
            // Sorted candidates and strict comparison give the lower rate on an exact tie.
            if (Math.abs((long) input.sampleRateHz() - candidate)
                    < Math.abs((long) input.sampleRateHz() - nearest)) nearest = candidate;
        }
        return new OutputFormat(nearest, Math.min(2, input.channelCount()));
    }

    static long outputByteLimit(AudioAnalysisResult input) {
        long wholeSeconds = input.decodedFrameCount() / input.sampleRateHz();
        if (wholeSeconds > (OUTPUT_BUDGET_BYTES - OUTPUT_FRAMING_BYTES) / 16000) {
            throw new AudioTranscodeException(AUDIO_OUTPUT_TOO_LARGE);
        }
        // Quotient/remainder arithmetic keeps rounding exact without overflowing on hostile metadata.
        long remainder = input.decodedFrameCount() % input.sampleRateHz();
        long partialBytes = (remainder * 16000 + input.sampleRateHz() - 1L) / input.sampleRateHz();
        long limit = wholeSeconds * 16000 + partialBytes + OUTPUT_FRAMING_BYTES;
        if (limit > OUTPUT_BUDGET_BYTES) throw new AudioTranscodeException(AUDIO_OUTPUT_TOO_LARGE);
        return limit;
    }
}
