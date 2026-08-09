package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.testfixture.SyntheticAudioFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.AbstractResource;
import org.springframework.mock.web.MockMultipartFile;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AudioAnalysisService decoded PCM analysis")
class AudioAnalysisServiceTest {

    private final AudioAnalysisService service = new AudioAnalysisService();

    @Test
    @DisplayName("WAV duration and waveform come from the same decoded PCM frames")
    void wavAnalysisUsesDecodedPcmFrames() {
        int sampleRate = 8_000;
        int frames = 19_200;

        AudioAnalysisResult result = service.analyze(file(
                "fixture.wav",
                "audio/wav",
                SyntheticAudioFixtures.wav16(sampleRate, 2, frames)));

        assertThat(result.format()).isEqualTo(AudioAnalysisFormat.WAV);
        assertThat(result.durationSeconds()).isEqualTo(2);
        assertThat(result.decodedFrameCount()).isEqualTo(frames);
        assertThat(result.sampleRateHz()).isEqualTo(sampleRate);
        assertThat(result.channelCount()).isEqualTo(2);
        assertWaveform(result.waveformJson());
        assertThat(result.waveformJson()).contains("0.750");
    }

    @Test
    @DisplayName("128 and 320 kbps CBR MP3 durations ignore file-size bitrate estimates")
    void cbrMp3AnalysisCountsDecodedFramesAtDifferentBitrates() {
        int frameCount = 100;
        int expectedSeconds = roundedMp3Seconds(frameCount);

        AudioAnalysisResult cbr128 = service.analyze(file(
                "cbr-128.mp3",
                "audio/mpeg",
                SyntheticAudioFixtures.mp3Cbr(128, frameCount)));
        AudioAnalysisResult cbr320 = service.analyze(file(
                "cbr-320.mp3",
                "audio/mpeg",
                SyntheticAudioFixtures.mp3Cbr(320, frameCount)));

        assertMp3Result(cbr128, expectedSeconds);
        assertMp3Result(cbr320, expectedSeconds);
        assertThat(cbr128.durationSeconds()).isEqualTo(cbr320.durationSeconds());
    }

    @Test
    @DisplayName("VBR MP3 with Xing and ID3v2 is decoded through the same PCM path")
    void vbrMp3WithId3AnalysisCountsDecodedFrames() {
        int frameCount = 103;

        AudioAnalysisResult result = service.analyze(file(
                "vbr-id3.mp3",
                "audio/mpeg",
                SyntheticAudioFixtures.mp3VbrWithId3(frameCount)));

        assertMp3Result(result, roundedMp3Seconds(frameCount));
    }

    @Test
    @DisplayName("PCM frame bytes split across 4096-byte reads are retained")
    void decodedPcmRetainsCrossBufferFrameRemainders() throws IOException {
        int frames = 1_501;
        byte[] pcm = new byte[frames * 4];
        for (int offset = 0; offset < pcm.length; offset += 4) {
            pcm[offset] = 0;
            pcm[offset + 1] = 64;
            pcm[offset + 2] = 0;
            pcm[offset + 3] = (byte) 0xC0;
        }
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                1_000,
                16,
                2,
                4,
                1_000,
                false);

        AudioAnalysisResult result = service.analyzeDecodedPcm(
                new FragmentingInputStream(pcm, 3),
                format,
                AudioAnalysisFormat.WAV);

        assertThat(result.decodedFrameCount()).isEqualTo(frames);
        assertThat(result.durationSeconds()).isEqualTo(2);
        assertWaveform(result.waveformJson());
    }

    @Test
    @DisplayName("malformed and unsupported audio fail closed")
    void malformedAndUnsupportedAudioFailClosed() {
        assertReason(
                file("broken.wav", "audio/wav", new byte[44]),
                AudioAnalysisException.Reason.INVALID_AUDIO);
        assertReason(
                file("unsupported.bin", "application/octet-stream", new byte[] {1, 2, 3}),
                AudioAnalysisException.Reason.UNSUPPORTED_FORMAT);
    }

    @Test
    @DisplayName("unreadable resources are classified without exposing their storage key")
    void unreadableResourceFailsClosed() {
        AbstractResource resource = new AbstractResource() {
            @Override
            public String getDescription() {
                return "unreadable fixture";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("synthetic read failure");
            }
        };

        assertThatThrownBy(() -> service.analyze("tracks/audio/private-key.mp3", resource))
                .isInstanceOf(AudioAnalysisException.class)
                .satisfies(exception -> assertThat(((AudioAnalysisException) exception).getReason())
                        .isEqualTo(AudioAnalysisException.Reason.UNREADABLE))
                .hasMessageNotContaining("private-key");
    }

    private MockMultipartFile file(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("audioFile", filename, contentType, content);
    }

    private int roundedMp3Seconds(int frameCount) {
        return (int) Math.max(1, Math.round(
                frameCount * SyntheticAudioFixtures.MP3_SAMPLES_PER_FRAME
                        / (double) SyntheticAudioFixtures.MP3_SAMPLE_RATE));
    }

    private void assertMp3Result(AudioAnalysisResult result, int expectedSeconds) {
        assertThat(result.format()).isEqualTo(AudioAnalysisFormat.MP3);
        assertThat(result.durationSeconds()).isCloseTo(expectedSeconds, withinOneSecond());
        assertThat(result.decodedFrameCount()).isPositive();
        assertThat(result.sampleRateHz()).isEqualTo(SyntheticAudioFixtures.MP3_SAMPLE_RATE);
        assertWaveform(result.waveformJson());
    }

    private org.assertj.core.data.Offset<Integer> withinOneSecond() {
        return org.assertj.core.data.Offset.offset(1);
    }

    private void assertReason(
            MockMultipartFile file,
            AudioAnalysisException.Reason expectedReason) {
        assertThatThrownBy(() -> service.analyze(file))
                .isInstanceOf(AudioAnalysisException.class)
                .satisfies(exception -> assertThat(((AudioAnalysisException) exception).getReason())
                        .isEqualTo(expectedReason));
    }

    private void assertWaveform(String waveform) {
        assertThat(waveform).startsWith("[").endsWith("]");
        assertThat(waveform.substring(1, waveform.length() - 1).split(","))
                .hasSize(200);
    }

    private static final class FragmentingInputStream extends ByteArrayInputStream {

        private final int maxChunk;

        private FragmentingInputStream(byte[] bytes, int maxChunk) {
            super(bytes);
            this.maxChunk = maxChunk;
        }

        @Override
        public synchronized int read(byte[] target, int offset, int length) {
            return super.read(target, offset, Math.min(length, maxChunk));
        }
    }
}
