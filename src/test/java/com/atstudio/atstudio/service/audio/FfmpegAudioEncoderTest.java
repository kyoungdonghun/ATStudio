package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.testfixture.SyntheticAudioFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

class FfmpegAudioEncoderTest {
    @TempDir Path root;
    private final AudioAnalysisResult input = new AudioAnalysisResult(2, "[0.5]", AudioAnalysisFormat.WAV, 88200, 44100, 2);

    @Test
    void configurationBoundsAreFailClosed() {
        assertThatThrownBy(() -> new FfmpegAudioEncoder(" ", 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 1801)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidInputOrUnsupportedFormatFailsClosed() {
        for (AudioAnalysisResult unsupported : List.of(
                new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.WAV, 44100, 44100, 33),
                new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.MP3, 44100, 44100, 2))) {
            assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 1).encode(root.resolve("in.wav"), root.resolve("out.mp3"), unsupported))
                    .isInstanceOfSatisfying(AudioTranscodeException.class,
                            e -> assertThat(e.code()).isEqualTo(AudioTranscodeException.Code.AUDIO_FORMAT_UNSUPPORTED));
        }
        assertThatThrownBy(() -> FfmpegAudioEncoder.outputFormat(null)).isInstanceOf(AudioTranscodeException.class);
        assertThatThrownBy(() -> new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.WAV, 44100, 44100, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.WAV, 44100, 0, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"16000,1,16000,1", "22050,2,22050,2", "24000,1,24000,1",
            "32000,2,32000,2", "44100,1,44100,1", "48000,2,48000,2",
            "96000,2,48000,2", "44100,6,44100,2", "96000,6,48000,2",
            "8000,1,16000,1", "23000,1,22050,1", "23025,2,22050,2", "23026,2,24000,2",
            "46050,2,44100,2", "46051,2,48000,2", "192000,8,48000,2"})
    void derivativeUsesNearestCompatibleRateAndAtMostStereo(int inputRate, int inputChannels,
            int expectedRate, int expectedChannels) {
        AudioAnalysisResult source = new AudioAnalysisResult(2, "[0.5]", AudioAnalysisFormat.WAV,
                (long) inputRate * 2, inputRate, inputChannels);
        assertThat(FfmpegAudioEncoder.outputFormat(source))
                .isEqualTo(new FfmpegAudioEncoder.OutputFormat(expectedRate, expectedChannels));
        assertThat(FfmpegAudioEncoder.outputByteLimit(source)).isEqualTo(97536);
    }

    @ParameterizedTest
    @CsvSource({"268369919,268435455", "268369920,268435456"})
    void outputBudgetIncludesFramingAndAcceptsExactBoundary(long frames, long expectedLimit) {
        AudioAnalysisResult source = new AudioAnalysisResult(16773, "[0]", AudioAnalysisFormat.WAV,
                frames, 16000, 1);
        assertThat(FfmpegAudioEncoder.outputByteLimit(source)).isEqualTo(expectedLimit);
    }

    @ParameterizedTest
    @CsvSource({"268369921,16000", "104857600,1", "9223372036854775807,1"})
    void oversizedOutputFailsBeforeProcessConstructionAndPreservesSource(long frames, int rate) throws Exception {
        byte[] original = SyntheticAudioFixtures.wav16(44100, 2, 88200);
        Path source = Files.write(root.resolve("retained.wav"), original);
        Path output = root.resolve("out.mp3");
        AudioAnalysisResult modeled = new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.WAV, frames, rate, 1);
        try (MockedConstruction<ProcessBuilder> builders = mockConstruction(ProcessBuilder.class)) {
            assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 10).encode(source, output, modeled))
                    .isInstanceOfSatisfying(AudioTranscodeException.class,
                            e -> assertThat(e.code()).isEqualTo(AudioTranscodeException.Code.AUDIO_OUTPUT_TOO_LARGE));
            assertThat(builders.constructed()).isEmpty();
        }
        assertThat(Files.readAllBytes(source)).isEqualTo(original);
        assertThat(output).doesNotExist();
    }

    @Test
    void eightKhzMonoEightBitAtUploadCapRemainsWithinBudget() {
        // Model 100MiB WAV with a 44-byte header; do not allocate or encode a multi-hour test file.
        AudioAnalysisResult source = new AudioAnalysisResult(13107, "[0]", AudioAnalysisFormat.WAV,
                100L * 1024 * 1024 - 44, 8000, 1);
        assertThat(FfmpegAudioEncoder.outputByteLimit(source)).isEqualTo(209780648);
        assertThat(FfmpegAudioEncoder.outputFormat(source))
                .isEqualTo(new FfmpegAudioEncoder.OutputFormat(16000, 1));
    }

    @Test
    void childEnvironmentContainsOnlyCaseInsensitiveAllowlistBeforeStart() throws Exception {
        Path source = Files.write(root.resolve("source.wav"), new byte[]{1});
        Path output = root.resolve("out.mp3");
        Map<String, String> allowed = Map.ofEntries(
                Map.entry("SystemRoot", "synthetic-root"), Map.entry("windir", "synthetic-windir"),
                Map.entry("SYSTEMDRIVE", "synthetic-drive"), Map.entry("Path", "synthetic-path"),
                Map.entry("Pathext", "synthetic-extensions"), Map.entry("TEMP", "synthetic-temp"),
                Map.entry("tmp", "synthetic-tmp"), Map.entry("LANG", "synthetic-lang"),
                Map.entry("LC_ALL", "synthetic-locale"), Map.entry("lc_ctype", "synthetic-ctype"),
                Map.entry("TZ", "synthetic-zone"));
        Map<String, String> environment = new HashMap<>(allowed);
        for (String denied : List.of("DB_PASSWORD", "TOSS_SECRET_KEY", "MAIL_PASSWORD", "JAVA_TOOL_OPTIONS",
                "FFREPORT", "ffreport", "SPRING_DATASOURCE_PASSWORD", "APP_JWT_SECRET", "UNLISTED")) {
            environment.put(denied, "synthetic-sensitive-value");
        }
        Process process = mock(Process.class);
        when(process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
        try (MockedConstruction<ProcessBuilder> builders = mockConstruction(ProcessBuilder.class, (builder, context) -> {
            when(builder.environment()).thenReturn(environment);
            when(builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.redirectError(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.start()).thenAnswer(i -> {
                assertThat(environment).containsExactlyInAnyOrderEntriesOf(allowed);
                Files.write(output, new byte[]{1});
                return process;
            });
        })) {
            new FfmpegAudioEncoder("ffmpeg", 10).encode(source, output, input);
            assertThat(builders.constructed()).hasSize(1);
            verify(builders.constructed().get(0)).start();
        }
    }

    @Test
    void argumentsAreLocalFixed128kAndBoundedWithNoSilentTruncation() throws Exception {
        Path source = Files.write(root.resolve("input with spaces.wav"), SyntheticAudioFixtures.wav16(44100, 2, 88200));
        Path output = root.resolve("out.mp3");
        Process process = mock(Process.class);
        when(process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
        try (MockedConstruction<ProcessBuilder> builders = mockConstruction(ProcessBuilder.class, (builder, context) -> {
            @SuppressWarnings("unchecked") List<String> arguments = (List<String>) context.arguments().get(0);
            assertThat(arguments).containsSubsequence("-b:a", "128k").containsSubsequence("-abr", "0")
                    .containsSubsequence("-protocol_whitelist", "file").containsSubsequence("-fs", "97536")
                    .contains(source.toString()).doesNotContain("-t", "-af", "-filter:a", "-y");
            when(builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.redirectError(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.start()).thenAnswer(i -> { Files.write(output, new byte[97536]); return process; });
        })) {
            assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 10).encode(source, output, input))
                    .isInstanceOfSatisfying(AudioTranscodeException.class,
                            e -> assertThat(e.code()).isEqualTo(AudioTranscodeException.Code.AUDIO_OUTPUT_INVALID));
            assertThat(builders.constructed()).hasSize(1);
        }
    }

    @Test
    void timeoutKillsAndReapsOnlyOwnedProcessWithinBound() throws Exception {
        Path source = Files.write(root.resolve("source.wav"), new byte[]{1});
        Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);
        when(process.descendants()).thenReturn(Stream.empty());
        when(process.waitFor(anyLong(), eq(TimeUnit.NANOSECONDS))).thenReturn(true);
        try (MockedConstruction<ProcessBuilder> ignored = builders(process)) {
            assertThatThrownBy(() -> new FfmpegAudioEncoder("ffmpeg", 1).encode(source, root.resolve("out.mp3"), input))
                    .isInstanceOfSatisfying(AudioTranscodeException.class,
                            e -> assertThat(e.code()).isEqualTo(AudioTranscodeException.Code.AUDIO_TRANSCODE_TIMEOUT));
        }
        verify(process).destroyForcibly();
        verify(process).waitFor(anyLong(), eq(TimeUnit.NANOSECONDS));
        verify(process, never()).waitFor();
    }

    @Test
    void nonzeroExitHasOnlySafeCode() throws Exception {
        Path source = Files.write(root.resolve("source.wav"), new byte[]{1});
        Process process = mock(Process.class);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(1);
        try (MockedConstruction<ProcessBuilder> ignored = builders(process)) {
            assertThatThrownBy(() -> new FfmpegAudioEncoder("private executable path", 1).encode(source, root.resolve("out.mp3"), input))
                    .hasMessage("AUDIO_TRANSCODE_FAILED");
        }
    }

    private MockedConstruction<ProcessBuilder> builders(Process process) {
        return mockConstruction(ProcessBuilder.class, (builder, context) -> {
            when(builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.redirectError(ProcessBuilder.Redirect.DISCARD)).thenReturn(builder);
            when(builder.start()).thenReturn(process);
        });
    }
}
