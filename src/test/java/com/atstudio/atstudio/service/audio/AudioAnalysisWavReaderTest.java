package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.testfixture.SyntheticAudioFixtures;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.AbstractResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AudioAnalysisWavReaderTest {
    private final AudioAnalysisService service = new AudioAnalysisService();

    @Test
    void pcmContainingMpegSyncBytesIsStillDecodedAsWav() throws Exception {
        byte[] pcm = SyntheticAudioFixtures.mp3Cbr(128, 10);
        assertThat(pcm.length % 6).isZero();
        byte[] wav = wav(1, 24, pcm, 28);
        // These bytes are valid PCM samples, but the retained MP3 SPI also claims them.
        try (var wrong = new MpegAudioFileReader().getAudioInputStream(new ByteArrayInputStream(wav))) {
            assertThat(wrong.getFormat().getEncoding().toString()).contains("MPEG");
        }
        AudioAnalysisResult result = service.analyze(file("fixture.wav", wav));
        assertThat(result.format()).isEqualTo(AudioAnalysisFormat.WAV);
        assertThat(result.sampleRateHz()).isEqualTo(48_000);
        assertThat(result.channelCount()).isEqualTo(2);
        assertThat(result.decodedFrameCount()).isEqualTo(pcm.length / 6);
    }

    @Test
    void pcm24WithOddPaddedMetadataRetainsFramesAndPeaks() {
        byte[] pcm = new byte[48_000 * 6];
        for (int i = 0; i < pcm.length; i += 6) {
            pcm[i + 2] = 0x40;
            pcm[i + 5] = (byte) 0xA0;
        }
        AudioAnalysisResult result = service.analyze(file("fixture.WAV", wav(1, 24, pcm, 3)));
        assertThat(result.decodedFrameCount()).isEqualTo(48_000);
        assertThat(result.durationSeconds()).isEqualTo(1);
        assertThat(result.waveformJson()).contains("0.750");
    }

    @Test
    void float32WavUsesPlatformFloatReader() {
        byte[] pcm = new byte[48_000 * 8];
        ByteBuffer samples = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
        while (samples.hasRemaining()) {
            samples.putFloat(0.5f).putFloat(-0.75f);
        }
        AudioAnalysisResult result = service.analyze(file("float.wav", wav(3, 32, pcm, 0)));
        assertThat(result.decodedFrameCount()).isEqualTo(48_000);
        assertThat(result.channelCount()).isEqualTo(2);
        assertThat(result.waveformJson()).contains("0.750");
    }

    @Test
    void extensiblePcm24WavUsesPlatformExtensibleReader() {
        byte[] pcm = new byte[48_000 * 6];
        AudioAnalysisResult result = service.analyze(file("extensible.wav", wav(0xFFFE, 24, pcm, 0)));
        assertThat(result.decodedFrameCount()).isEqualTo(48_000);
        assertThat(result.sampleRateHz()).isEqualTo(48_000);
        assertThat(result.channelCount()).isEqualTo(2);
    }

    @Test
    void truncatedPcm24FailsFrameCountValidation() {
        byte[] complete = wav(1, 24, new byte[48_000 * 6], 28);
        assertInvalid(Arrays.copyOf(complete, complete.length - 600));
    }

    @Test
    void mp3RenamedAsWavFailsContainerValidation() {
        assertInvalid(SyntheticAudioFixtures.mp3Cbr(128, 20));
    }

    @Test
    void mpegInsideWavCodecContainerIsNotAcceptedAsPcmWav() {
        assertInvalid(wav(0x55, 16, SyntheticAudioFixtures.mp3Cbr(128, 10), 0));
    }

    @Test
    void missingFormatChunkFailsClosed() {
        byte[] malformed = wav(1, 24, new byte[600], 0);
        System.arraycopy("JUNK".getBytes(StandardCharsets.US_ASCII), 0, malformed, 12, 4);
        assertInvalid(malformed);
    }

    @Test
    void readerAttemptsCloseEveryOpenedSource() {
        byte[] content = wav(3, 32, new byte[8_000], 3);
        AtomicInteger opened = new AtomicInteger();
        AtomicInteger closed = new AtomicInteger();
        AbstractResource resource = new AbstractResource() {
            @Override public String getDescription() { return "synthetic resource"; }
            @Override public InputStream getInputStream() {
                opened.incrementAndGet();
                return new FilterInputStream(new ByteArrayInputStream(content)) {
                    private boolean counted;
                    @Override public void close() throws IOException {
                        if (!counted) { closed.incrementAndGet(); counted = true; }
                        super.close();
                    }
                };
            }
        };
        assertThat(service.analyze("fixture.wav", resource).decodedFrameCount()).isEqualTo(1_000);
        assertThat(opened.get()).isPositive().isEqualTo(closed.get());
    }

    @Test
    void unreadableWavResourceRemainsUnreadableAndDoesNotExposeLocation() {
        AbstractResource resource = new AbstractResource() {
            @Override public String getDescription() { return "private storage"; }
            @Override public InputStream getInputStream() throws IOException {
                throw new IOException("private storage location");
            }
        };
        assertThatThrownBy(() -> service.analyze("private.wav", resource))
                .isInstanceOfSatisfying(AudioAnalysisException.class,
                        error -> assertThat(error.getReason()).isEqualTo(AudioAnalysisException.Reason.UNREADABLE))
                .hasMessageNotContaining("private");
    }

    private void assertInvalid(byte[] content) {
        assertThatThrownBy(() -> service.analyze(file("invalid.wav", content)))
                .isInstanceOfSatisfying(AudioAnalysisException.class,
                        error -> assertThat(error.getReason()).isEqualTo(AudioAnalysisException.Reason.INVALID_AUDIO));
    }

    private MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("audioFile", name, "audio/wav", content);
    }

    private byte[] wav(int codec, int bits, byte[] pcm, int junkSize) {
        ByteArrayOutputStream chunks = new ByteArrayOutputStream();
        if (junkSize > 0) {
            chunks.writeBytes("JUNK".getBytes(StandardCharsets.US_ASCII));
            little(chunks, junkSize, 4);
            chunks.writeBytes(new byte[junkSize + junkSize % 2]);
        }
        chunks.writeBytes("fmt ".getBytes(StandardCharsets.US_ASCII));
        little(chunks, codec == 0xFFFE ? 40 : 16, 4);
        little(chunks, codec, 2);
        little(chunks, 2, 2);
        little(chunks, 48_000, 4);
        little(chunks, 48_000 * 2 * bits / 8, 4);
        little(chunks, 2 * bits / 8, 2);
        little(chunks, bits, 2);
        if (codec == 0xFFFE) {
            little(chunks, 22, 2);
            little(chunks, bits, 2);
            little(chunks, 3, 4);
            chunks.writeBytes(new byte[]{1, 0, 0, 0, 0, 0, 0x10, 0, (byte) 0x80, 0, 0, (byte) 0xAA, 0, 0x38, (byte) 0x9B, 0x71});
        }
        chunks.writeBytes("data".getBytes(StandardCharsets.US_ASCII));
        little(chunks, pcm.length, 4);
        chunks.writeBytes(pcm);
        if (pcm.length % 2 != 0) chunks.write(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes("RIFF".getBytes(StandardCharsets.US_ASCII));
        little(output, 4 + chunks.size(), 4);
        output.writeBytes("WAVE".getBytes(StandardCharsets.US_ASCII));
        output.writeBytes(chunks.toByteArray());
        return output.toByteArray();
    }

    private void little(ByteArrayOutputStream output, int number, int bytes) {
        for (int i = 0; i < bytes; i++) output.write((number >>> (8 * i)) & 0xFF);
    }
}
