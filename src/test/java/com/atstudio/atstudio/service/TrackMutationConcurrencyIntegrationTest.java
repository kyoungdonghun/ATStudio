package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.track.TrackUpdateRequest;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@Import({JpaConfig.class, TrackService.class, LikeService.class, DownloadService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TrackMutationConcurrencyIntegrationTest {

    @Autowired TrackService tracks;
    @Autowired LikeService likes;
    @Autowired DownloadService downloads;
    @Autowired TrackRepository trackRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean AudioAnalysisService audio;
    @MockitoBean CanonicalImageService images;
    @MockitoBean StorageService storage;
    @MockitoBean StorageMutationCoordinator mutations;

    @AfterEach
    void removeOnlyH2Fixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            for (String entity : List.of("Like", "License", "TrackDownload", "Track", "User")) {
                entityManager.createQuery("DELETE FROM " + entity).executeUpdate();
            }
        });
    }

    @Test
    void unlockedPersistenceControlReproducesStaleMediaResurrection() {
        Track original = seed();
        try (EntityManager staleContext = entityManagerFactory.createEntityManager();
             EntityManager replacementContext = entityManagerFactory.createEntityManager()) {
            staleContext.getTransaction().begin();
            try {
                Track stale = staleContext.find(Track.class, original.getId());
                replacementContext.getTransaction().begin();
                try {
                    Track replacement = replacementContext.find(
                            Track.class, original.getId(), LockModeType.PESSIMISTIC_WRITE);
                    replacement.updateAudioAnalysis("tracks/new.mp3", 2, "[0.9]");
                    replacement.updateThumbnail("tracks/new.jpg");
                    replacementContext.getTransaction().commit();
                } finally {
                    if (replacementContext.getTransaction().isActive()) {
                        replacementContext.getTransaction().rollback();
                    }
                }
                stale.incrementLikeCount();
                staleContext.getTransaction().commit();
            } finally {
                if (staleContext.getTransaction().isActive()) {
                    staleContext.getTransaction().rollback();
                }
            }
        }
        // This control deliberately bypasses services to prove the unversioned SQL hazard.
        Track reloaded = trackRepository.findById(original.getId()).orElseThrow();
        assertThat(reloaded.getAudioFile()).isEqualTo("tracks/old.mp3");
        assertThat(reloaded.getThumbnail()).isEqualTo("tracks/old.jpg");
        assertThat(reloaded.getLikeCount()).isOne();
    }

    @ParameterizedTest
    @EnumSource(Writer.class)
    void materialWritersWaitForReplacementAndKeepItsMediaAndCounters(Writer writer) throws Exception {
        Track original = seed();
        Long id = original.getId();
        CustomUserDetails user = principal(original.getUser().getId());
        if (writer == Writer.REMOVE_LIKE) {
            likes.addLike(id, user);
        }
        CountDownLatch replacementLoaded = new CountDownLatch(1);
        CountDownLatch releaseReplacement = new CountDownLatch(1);
        CountDownLatch writerStarted = new CountDownLatch(1);
        MockMultipartFile replacement = new MockMultipartFile("audio", "new.mp3", "audio/mpeg", new byte[]{1});
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", new byte[]{2});
        given(images.canonicalizeSquareTrackThumbnail(thumbnail)).willReturn(thumbnail);
        given(audio.analyze(replacement)).willAnswer(invocation -> {
            replacementLoaded.countDown();
            await(releaseReplacement);
            return new AudioAnalysisResult(2, "[0.9]", AudioAnalysisFormat.MP3, 88200, 44100, 2);
        });
        given(mutations.replace(any(), any(), any(), anyString(), anyString()))
                .willAnswer(invocation -> invocation.getArgument(2) == replacement
                        ? "tracks/new.mp3" : "tracks/new.jpg");
        if (writer == Writer.DOWNLOAD) {
            given(storage.loadAsResource(StorageRoot.PUBLIC, "tracks/new.mp3"))
                    .willReturn(new ByteArrayResource(new byte[]{3}));
        }
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> tracks.updateTrack(id, new TrackUpdateRequest(), replacement, thumbnail));
            assertThat(replacementLoaded.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                writerStarted.countDown();
                switch (writer) {
                    case METADATA -> {
                        TrackUpdateRequest request = new TrackUpdateRequest();
                        request.setTitle("Changed title");
                        tracks.updateTrack(id, request, null, null);
                    }
                    case ADD_LIKE -> likes.addLike(id, user);
                    case REMOVE_LIKE -> likes.removeLike(id, user);
                    case DOWNLOAD -> assertThat(downloads.download(id, user)).isNotNull();
                    case DELETE -> tracks.deleteTrack(id);
                    case BULK_PLAY -> new TransactionTemplate(transactionManager).executeWithoutResult(
                            status -> trackRepository.incrementPlayCount(id));
                    case BULK_DOWNLOAD -> new TransactionTemplate(transactionManager).executeWithoutResult(
                            status -> assertThat(trackRepository.incrementDownloadCountAtomically(id)).isOne());
                }
            });
            assertThat(writerStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> second.get(250, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
            releaseReplacement.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            Track reloaded = trackRepository.findById(id).orElseThrow();
            assertThat(reloaded.getAudioFile()).isEqualTo("tracks/new.mp3");
            assertThat(reloaded.getThumbnail()).isEqualTo("tracks/new.jpg");
            assertThat(reloaded.getDuration()).isEqualTo(2);
            assertThat(reloaded.getWaveformData()).isEqualTo("[0.9]");
            assertThat(reloaded.getLikeCount()).isEqualTo(writer == Writer.ADD_LIKE ? 1 : 0);
            assertThat(reloaded.getDownloadCount()).isEqualTo(
                    writer == Writer.DOWNLOAD || writer == Writer.BULK_DOWNLOAD ? 1 : 0);
            assertThat(reloaded.getPlayCount()).isEqualTo(writer == Writer.BULK_PLAY ? 1 : 0);
            assertThat(reloaded.isActive()).isEqualTo(writer != Writer.DELETE);
            if (writer == Writer.METADATA) {
                assertThat(reloaded.getTitle()).isEqualTo("Changed title");
            }
        } finally {
            releaseReplacement.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    private Track seed() {
        User user = userRepository.save(User.builder().email(UUID.randomUUID() + "@example.test")
                .nickname("writer").role(UserRole.ADMIN).build());
        return trackRepository.save(Track.builder().title("Original").bpm(120).tonality("C")
                .audioFile("tracks/old.mp3").thumbnail("tracks/old.jpg")
                .duration(1).waveformData("[0.1]").isActive(true).user(user).build());
    }

    private CustomUserDetails principal(Long id) {
        return CustomUserDetails.builder().id(id).role(UserRole.ADMIN).build();
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Test latch timed out");
        }
    }

    enum Writer { METADATA, ADD_LIKE, REMOVE_LIKE, DOWNLOAD, DELETE, BULK_PLAY, BULK_DOWNLOAD }
}
