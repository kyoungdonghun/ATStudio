package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.track.TrackUpdateRequest;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({JpaConfig.class, TrackService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Track audio replacement transaction integration")
class TrackAudioReplacementTransactionIntegrationTest {

    @Autowired TrackService trackService;
    @Autowired TrackRepository trackRepository;
    @Autowired UserRepository userRepository;

    @MockitoBean AudioAnalysisService audioAnalysisService;
    @MockitoBean CanonicalImageService canonicalImageService;
    @MockitoBean StorageService storageService;
    @MockitoBean StorageMutationCoordinator storageMutationCoordinator;

    @AfterEach
    void cleanUp() {
        trackRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("DB constraint failure rolls back new key, duration, and waveform together")
    void dbFailureRollsBackCompleteAudioMetadata() {
        User user = userRepository.save(User.builder()
                .email("audio-transaction@test.com")
                .nickname("audio-transaction")
                .build());
        Track original = trackRepository.save(Track.builder()
                .title("Original")
                .bpm(120)
                .tonality("C")
                .audioFile("tracks/audio/original.mp3")
                .duration(120)
                .waveformData("[0.500]")
                .user(user)
                .build());
        MultipartFile replacement = org.mockito.Mockito.mock(MultipartFile.class);
        given(replacement.isEmpty()).willReturn(false);
        given(audioAnalysisService.analyze(replacement)).willReturn(new AudioAnalysisResult(
                90,
                "[0.900]",
                AudioAnalysisFormat.MP3,
                3_969_000,
                44_100,
                2));
        given(storageMutationCoordinator.replace(
                eq(StorageDomain.TRACK),
                eq(StorageRoot.PUBLIC),
                eq(replacement),
                eq("tracks/audio"),
                eq("tracks/audio/original.mp3")))
                .willReturn("tracks/audio/replacement.mp3");
        TrackUpdateRequest request = new TrackUpdateRequest();
        request.setTitle("x".repeat(101));

        assertThatThrownBy(() -> trackService.updateTrack(original.getId(), request, replacement, null))
                .isInstanceOf(DataIntegrityViolationException.class);

        Track reloaded = trackRepository.findById(original.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Original");
        assertThat(reloaded.getAudioFile()).isEqualTo("tracks/audio/original.mp3");
        assertThat(reloaded.getDuration()).isEqualTo(120);
        assertThat(reloaded.getWaveformData()).isEqualTo("[0.500]");
        verify(storageMutationCoordinator).replace(
                StorageDomain.TRACK,
                StorageRoot.PUBLIC,
                replacement,
                "tracks/audio",
                "tracks/audio/original.mp3");
    }
}
