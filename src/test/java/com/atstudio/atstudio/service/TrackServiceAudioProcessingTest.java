package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.track.TrackCreateRequest;
import com.atstudio.atstudio.dto.track.TrackResponse;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.AlbumTrackRepository;
import com.atstudio.atstudio.repository.LicenseRepository;
import com.atstudio.atstudio.repository.LikeRepository;
import com.atstudio.atstudio.repository.PlaylistTrackRepository;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackService audio metadata and waveform processing")
class TrackServiceAudioProcessingTest {

    @Mock TrackRepository trackRepository;
    @Mock TrackTagRepository trackTagRepository;
    @Mock TagRepository tagRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;
    @Mock LikeRepository likeRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock LicenseRepository licenseRepository;
    @Mock PlaylistTrackRepository playlistTrackRepository;
    @Mock AlbumTrackRepository albumTrackRepository;
    @Mock CustomUserDetails userDetails;

    private TrackService service;
    private final AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

    @BeforeEach
    void setUp() {
        service = new TrackService(
                trackRepository,
                trackTagRepository,
                tagRepository,
                userRepository,
                audioAnalysisService,
                new CanonicalImageService(),
                storageService,
                storageMutationCoordinator,
                likeRepository,
                trackDownloadRepository,
                licenseRepository,
                playlistTrackRepository,
                albumTrackRepository);

        User user = User.builder().email("creator@example.com").nickname("creator").build();
        ReflectionTestUtils.setField(user, "id", 11L);
        given(userDetails.getId()).willReturn(11L);
        given(userRepository.findById(11L)).willReturn(Optional.of(user));
        lenient().when(storageMutationCoordinator.writeAll(any(), any(), any()))
                .thenReturn(List.of("tracks/audio/generated.wav"));
        lenient().when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            ReflectionTestUtils.setField(track, "id", 101L);
            return track;
        });
    }

    @Test
    @DisplayName("8-bit mono WAV produces full duration and a bounded 200-point waveform")
    void createTrack_8BitMonoWav_extractsDurationAndWaveform() throws IOException {
        byte[] wav = wav(8_000, 1, 8, 8_000, true);

        TrackResponse response = create(new MockMultipartFile("audioFile", "mono-8.wav", "audio/wav", wav));

        assertThat(response.duration()).isEqualTo(1);
        assertWaveform(response.waveformData());
        assertThat(response.waveformData()).contains("0.504");
    }

    @Test
    @DisplayName("16-bit stereo WAV processes both channels and partial accumulation chunks")
    void createTrack_16BitStereoWav_extractsChannelMaximums() throws IOException {
        byte[] wav = wav(8_000, 2, 16, 2_501, false);

        TrackResponse response = create(new MockMultipartFile("audioFile", "stereo-16.WAV", "audio/wav", wav));

        assertWaveform(response.waveformData());
        assertThat(response.waveformData()).contains("0.750");
    }

    @Test
    @DisplayName("24-bit WAV uses signed 24-bit peak normalization")
    void createTrack_24BitWav_extractsWaveform() throws IOException {
        byte[] wav = wav(4_000, 1, 24, 1_200, false);

        TrackResponse response = create(new MockMultipartFile("audioFile", "mono-24.wav", "audio/wav", wav));

        assertWaveform(response.waveformData());
        assertThat(response.waveformData()).contains("0.500");
    }

    @Test
    @DisplayName("WAV with an odd-sized metadata chunk remains word-aligned and readable")
    void createTrack_wavWithOddMetadataChunk_skipsPadding() throws IOException {
        byte[] wav = wav(8_000, 1, 16, 1_200, true);

        TrackResponse response = create(new MockMultipartFile("audioFile", "metadata.wav", "audio/wav", wav));

        assertWaveform(response.waveformData());
    }

    @Test
    @DisplayName("valid WAV bytes with an unsupported extension fail closed")
    void createTrack_nonWavExtension_failsClosed() throws IOException {
        byte[] wav = wav(8_000, 1, 16, 1_200, false);

        assertAudioAnalysisFailure(new MockMultipartFile(
                "audioFile", "audio.bin", "application/octet-stream", wav));
    }

    @Test
    @DisplayName("malformed MP3 never persists a size-estimated duration or null waveform")
    void createTrack_malformedMp3_failsClosed() {
        byte[] bytes = new byte[32_768];

        assertAudioAnalysisFailure(new MockMultipartFile(
                "audioFile", "sample.mp3", "audio/mpeg", bytes));
    }

    @Test
    @DisplayName("truncated and non-RIFF WAV files fail closed before storage mutation")
    void createTrack_malformedWav_failsClosed() {
        assertAudioAnalysisFailure(new MockMultipartFile(
                "audioFile", "short.wav", "audio/wav", new byte[8]));
        byte[] notRiff = new byte[44];
        System.arraycopy("NOPE".getBytes(StandardCharsets.US_ASCII), 0, notRiff, 0, 4);
        System.arraycopy("WAVE".getBytes(StandardCharsets.US_ASCII), 0, notRiff, 8, 4);
        assertAudioAnalysisFailure(new MockMultipartFile(
                "audioFile", "invalid.wav", "audio/wav", notRiff));
    }

    @Test
    @DisplayName("I/O failures use the stable audio analysis business error")
    void createTrack_ioFailure_failsClosed() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("broken.wav");
        given(file.isEmpty()).willReturn(false);
        given(file.getInputStream()).willThrow(new IOException("synthetic read failure"));

        assertAudioAnalysisFailure(file);
    }

    @Test
    @DisplayName("missing filename is rejected as unsupported audio")
    void createTrack_missingFilename_failsClosed() {
        MultipartFile file = new MockMultipartFile("audioFile", null, "application/octet-stream", new byte[] {1});

        assertAudioAnalysisFailure(file);
    }

    private TrackResponse create(MultipartFile file) {
        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Audio contract");
        request.setBpm(120);
        request.setTonality("C");
        return service.createTrack(request, file, null, userDetails);
    }

    private void assertAudioAnalysisFailure(MultipartFile file) {
        assertThatThrownBy(() -> create(file))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.AUDIO_ANALYSIS_FAILED));
        verifyNoInteractions(storageMutationCoordinator);
        verify(trackRepository, never()).save(any(Track.class));
    }

    private void assertWaveform(String waveform) {
        assertThat(waveform).isNotNull().startsWith("[").endsWith("]");
        assertThat(waveform.substring(1, waveform.length() - 1).split(",")).hasSize(200);
    }

    private byte[] wav(
            int sampleRate,
            int channels,
            int bitsPerSample,
            int frames,
            boolean includeOddMetadataChunk) throws IOException {
        int bytesPerSample = bitsPerSample / 8;
        int frameSize = channels * bytesPerSample;
        int dataSize = frames * frameSize;
        int metadataSize = includeOddMetadataChunk ? 10 : 0;
        int riffSize = 4 + (8 + 16) + metadataSize + (8 + dataSize);

        ByteArrayOutputStream out = new ByteArrayOutputStream(8 + riffSize);
        out.write("RIFF".getBytes(StandardCharsets.US_ASCII));
        writeLE(out, riffSize, 4);
        out.write("WAVE".getBytes(StandardCharsets.US_ASCII));
        out.write("fmt ".getBytes(StandardCharsets.US_ASCII));
        writeLE(out, 16, 4);
        writeLE(out, 1, 2);
        writeLE(out, channels, 2);
        writeLE(out, sampleRate, 4);
        writeLE(out, sampleRate * frameSize, 4);
        writeLE(out, frameSize, 2);
        writeLE(out, bitsPerSample, 2);

        if (includeOddMetadataChunk) {
            out.write("JUNK".getBytes(StandardCharsets.US_ASCII));
            writeLE(out, 1, 4);
            out.write(7);
            out.write(0);
        }

        out.write("data".getBytes(StandardCharsets.US_ASCII));
        writeLE(out, dataSize, 4);
        for (int frame = 0; frame < frames; frame++) {
            for (int channel = 0; channel < channels; channel++) {
                writeSample(out, bitsPerSample, channel == 0 ? 0.5 : -0.75);
            }
        }
        return out.toByteArray();
    }

    private void writeSample(ByteArrayOutputStream out, int bits, double level) {
        switch (bits) {
            case 8 -> out.write((int) Math.round(128 + (127 * level)) & 0xFF);
            case 16 -> writeLE(out, (short) Math.round(32767 * level), 2);
            case 24 -> writeLE(out, (int) Math.round(8_388_607 * level), 3);
            default -> throw new IllegalArgumentException("unsupported test bit depth");
        }
    }

    private void writeLE(ByteArrayOutputStream out, int value, int bytes) {
        byte[] encoded = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
        out.write(encoded, 0, bytes);
    }
}
