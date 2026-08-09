package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.AdminTrackAudioAnalysisDryRunItemResponse;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.service.audio.AudioAnalysisException;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin track audio analysis dry-run")
class AdminTrackAudioAnalysisServiceTest {

    @Mock TrackRepository trackRepository;
    @Mock StorageService storageService;
    @Mock AudioAnalysisService audioAnalysisService;

    @InjectMocks AdminTrackAudioAnalysisService service;

    @Test
    @DisplayName("active and inactive tracks are reported in deterministic bounded pages without writes")
    void dryRunReportsAllStatesWithoutRepositoryMutation() {
        Track active = track(1L, "Active", true, "tracks/audio/active.mp3", 99, "[0.500]");
        Track inactive = track(2L, "Inactive", false, "tracks/audio/inactive.wav", 120, null);
        given(trackRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(
                List.of(active, inactive),
                PageRequest.of(0, 2),
                2));
        ByteArrayResource resource = new ByteArrayResource(new byte[] {1});
        given(storageService.loadAsResource(StorageRoot.PUBLIC, active.getAudioFile())).willReturn(resource);
        given(storageService.loadAsResource(StorageRoot.PUBLIC, inactive.getAudioFile())).willReturn(resource);
        given(audioAnalysisService.analyze(active.getAudioFile(), resource))
                .willReturn(result(100, AudioAnalysisFormat.MP3));
        given(audioAnalysisService.analyze(inactive.getAudioFile(), resource))
                .willReturn(result(90, AudioAnalysisFormat.WAV));

        ResponseDTO<AdminTrackAudioAnalysisDryRunItemResponse> response = service.dryRun(1, 2);

        assertThat(response.getDataList()).hasSize(2);
        assertThat(response.getDataList().get(0)).satisfies(item -> {
            assertThat(item.trackId()).isEqualTo(1L);
            assertThat(item.title()).isEqualTo("Active");
            assertThat(item.isActive()).isTrue();
            assertThat(item.readable()).isTrue();
            assertThat(item.storedDurationSeconds()).isEqualTo(99);
            assertThat(item.analyzedDurationSeconds()).isEqualTo(100);
            assertThat(item.durationDeltaSeconds()).isEqualTo(1);
            assertThat(item.status()).isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Status.MATCH);
            assertThat(item.recommendation())
                    .isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.NONE);
        });
        assertThat(response.getDataList().get(1)).satisfies(item -> {
            assertThat(item.isActive()).isFalse();
            assertThat(item.storedWaveformPresent()).isFalse();
            assertThat(item.durationDeltaSeconds()).isEqualTo(-30);
            assertThat(item.status())
                    .isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Status.METADATA_MISMATCH);
            assertThat(item.recommendation())
                    .isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.BACKFILL_ANALYSIS_METADATA);
        });

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(trackRepository).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(2);
        assertThat(pageable.getValue().getSort().getOrderFor("id")).isNotNull().satisfies(order ->
                assertThat(order.isAscending()).isTrue());
        verifyNoMoreInteractions(trackRepository);
    }

    @Test
    @DisplayName("unreadable and corrupt files are isolated as report rows")
    void dryRunIsolatesUnreadableAndCorruptRows() {
        Track unreadable = track(1L, "Missing", true, "tracks/audio/missing.mp3", 10, "[0.1]");
        Track corrupt = track(2L, "Corrupt", false, "tracks/audio/corrupt.wav", 20, "[0.2]");
        given(trackRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(
                List.of(unreadable, corrupt)));
        given(storageService.loadAsResource(StorageRoot.PUBLIC, unreadable.getAudioFile()))
                .willThrow(new IllegalStateException("storage unavailable"));
        ByteArrayResource resource = new ByteArrayResource(new byte[] {1});
        given(storageService.loadAsResource(StorageRoot.PUBLIC, corrupt.getAudioFile()))
                .willReturn(resource);
        given(audioAnalysisService.analyze(corrupt.getAudioFile(), resource))
                .willThrow(new AudioAnalysisException(
                        AudioAnalysisException.Reason.INVALID_AUDIO,
                        AudioAnalysisFormat.WAV));

        List<AdminTrackAudioAnalysisDryRunItemResponse> rows = service.dryRun(1, 20).getDataList();

        assertThat(rows.get(0).readable()).isFalse();
        assertThat(rows.get(0).status())
                .isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Status.UNREADABLE);
        assertThat(rows.get(1).readable()).isTrue();
        assertThat(rows.get(1).status())
                .isEqualTo(AdminTrackAudioAnalysisDryRunItemResponse.Status.ANALYSIS_FAILED);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.analyzedDurationSeconds()).isNull();
            assertThat(row.durationDeltaSeconds()).isNull();
        });
        verifyNoMoreInteractions(trackRepository);
    }

    @Test
    @DisplayName("page and size bounds fail before any repository or file access")
    void dryRunRejectsUnboundedRequests() {
        for (int[] values : List.of(
                new int[] {0, 20},
                new int[] {1, 0},
                new int[] {1, AdminTrackAudioAnalysisService.MAX_PAGE_SIZE + 1})) {
            assertThatThrownBy(() -> service.dryRun(values[0], values[1]))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        }

        verifyNoInteractions(trackRepository, storageService, audioAnalysisService);
    }

    private Track track(
            Long id,
            String title,
            boolean active,
            String audioFile,
            int duration,
            String waveform) {
        Track track = Track.builder()
                .title(title)
                .bpm(120)
                .tonality("C")
                .audioFile(audioFile)
                .duration(duration)
                .waveformData(waveform)
                .isActive(active)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private AudioAnalysisResult result(int duration, AudioAnalysisFormat format) {
        return new AudioAnalysisResult(
                duration,
                "[0.500]",
                format,
                duration * 44_100L,
                44_100,
                2);
    }
}
