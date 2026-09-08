package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.StorageIntegrityProperties;
import com.atstudio.atstudio.config.StorageIntegrityStartupGuard;
import com.atstudio.atstudio.dto.track.TrackUpdateRequest;
import com.atstudio.atstudio.dto.album.AlbumUpdateRequest;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AlbumService;
import com.atstudio.atstudio.service.AlbumLikeService;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.PlaylistService;
import com.atstudio.atstudio.service.TrackService;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.BDDMockito.given;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@Import({JpaConfig.class, TrackService.class, DownloadService.class, AlbumService.class, AlbumLikeService.class, PlaylistService.class,
        StorageMutationCoordinator.class, StorageMutationJournalService.class, StorageCleanupService.class,
        StorageReferenceChecker.class, StorageIntegrityService.class, RetainedHistoryStorageIntegrationTest.Config.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RetainedHistoryStorageIntegrationTest {

    @TempDir static Path temporaryRoot;
    @Autowired TrackService tracks;
    @Autowired DownloadService downloads;
    @Autowired PlaylistService playlists;
    @Autowired AlbumService albums;
    @Autowired AlbumLikeService albumLikes;
    @Autowired TrackRepository trackRepository;
    @Autowired TrackDownloadRepository history;
    @Autowired LicenseRepository licenses;
    @Autowired PlaylistRepository playlistRepository;
    @Autowired AlbumRepository albumRepository;
    @Autowired StorageMutationRepository journal;
    @Autowired StorageMutationJournalService journalService;
    @Autowired StorageMutationCoordinator mutations;
    @Autowired StorageIntegrityService integrity;
    @Autowired StorageReferenceChecker references;
    @Autowired StorageService storage;
    @MockitoSpyBean LocalStorageService localStorage;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean AudioAnalysisService audio;
    @MockitoBean CanonicalImageService images;
    private User user;
    private CustomUserDetails principal;

    @BeforeEach
    void createH2Subscriber() {
        transaction().executeWithoutResult(status -> {
            user = User.builder().email(UUID.randomUUID() + "@example.test").nickname("retained")
                    .userType(UserType.INDIVIDUAL).role(UserRole.USER).build();
            entityManager.persist(user);
            Subscription plan = Subscription.builder().name("Daily one").userType(UserType.INDIVIDUAL)
                    .priceMonthly(BigDecimal.ONE).priceYearly(BigDecimal.TEN).downloadPerDay(1).build();
            entityManager.persist(plan);
            entityManager.persist(UserSubscription.builder().user(user).subscription(plan)
                    .billingCycle(BillingCycle.MONTHLY).startedAt(LocalDate.now())
                    .expiresAt(LocalDate.now().plusDays(1)).build());
        });
        principal = CustomUserDetails.builder().id(user.getId()).role(UserRole.USER).build();
    }

    @AfterEach
    void removeOnlyH2Rows() {
        transaction().executeWithoutResult(status -> {
            for (String entity : List.of("License", "TrackDownload", "PlaylistTrack", "AlbumTrack", "Like", "AlbumLike",
                    "TrackTag", "Track", "Playlist", "Album", "UserSubscription", "Subscription", "User", "StorageMutation")) {
                entityManager.createQuery("DELETE FROM " + entity).executeUpdate();
            }
        });
    }

    @Test
    void deactivateReactivateRetainsLicenseHistoryQuotaAndRedownloadIdentity() {
        Track track = seedTrack();
        downloads.download(track.getId(), principal);
        License license = licenses.findByUserAndTrack(user, track).orElseThrow();
        TrackDownload event = history.findAll().get(0);
        tracks.deleteTrack(track.getId());
        assertThat(trackRepository.findById(track.getId()).orElseThrow().isActive()).isFalse();
        assertHistory(track, license, event);
        assertThatThrownBy(() -> downloads.download(track.getId(), principal))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
        TrackUpdateRequest reactivate = new TrackUpdateRequest();
        reactivate.setIsActive(true);
        tracks.updateTrack(track.getId(), reactivate, null, null);
        assertThatCode(() -> downloads.download(track.getId(), principal).getContentAsByteArray())
                .doesNotThrowAnyException();
        assertHistory(track, license, event);
        Track another = seedTrack();
        assertThatThrownBy(() -> downloads.download(another.getId(), principal))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(BUSINESS_ERROR.DOWNLOAD_LIMIT_EXCEEDED));
        assertStrictStartupHealthy();
    }

    @ParameterizedTest
    @CsvSource({"ALBUM,UNIQUE", "PLAYLIST,UNIQUE", "ALBUM,SHARED", "PLAYLIST,SHARED",
            "ALBUM,NONE", "PLAYLIST,NONE", "ALBUM,LEGACY_INACTIVE", "PLAYLIST,LEGACY_INACTIVE"})
    void deletionCleanupAuditAndStrictStartupAgree(StorageDomain domain, ThumbnailCase scenario) {
        String key = scenario == ThumbnailCase.NONE ? null : storeThumbnail(domain);
        Long first = parent(domain, key, true);
        Long second = scenario == ThumbnailCase.SHARED || scenario == ThumbnailCase.LEGACY_INACTIVE
                ? parent(domain, key, scenario == ThumbnailCase.SHARED) : null;
        long mutationsBefore = journal.count();
        deleteParent(domain, first);
        assertThat(thumbnail(domain, first)).isNull();
        boolean retained = second != null;
        if (key != null) {
            assertThat(storage.exists(StorageRoot.PUBLIC, key)).isEqualTo(retained);
            assertThat(references.isReferenced(domain, key)).isEqualTo(retained);
        } else {
            assertThat(journal.count()).isEqualTo(mutationsBefore);
        }
        assertStrictStartupHealthy();
        if (scenario == ThumbnailCase.SHARED) {
            deleteParent(domain, second);
            assertThat(storage.exists(StorageRoot.PUBLIC, key)).isFalse();
            assertStrictStartupHealthy();
        }
    }

    @Test
    void sharingAcrossAlbumAndPlaylistRetainsFileUntilLastReferenceIsRemoved() {
        String key = storeThumbnail(StorageDomain.ALBUM);
        Long album = parent(StorageDomain.ALBUM, key, true);
        Long playlist = parent(StorageDomain.PLAYLIST, key, true);
        albums.deleteAlbum(album);
        assertThat(storage.exists(StorageRoot.PUBLIC, key)).isTrue();
        assertStrictStartupHealthy();
        playlists.deletePlaylist(playlist, principal);
        assertThat(storage.exists(StorageRoot.PUBLIC, key)).isFalse();
        assertStrictStartupHealthy();
    }

    @ParameterizedTest
    @EnumSource(value = StorageDomain.class, names = {"ALBUM", "PLAYLIST"})
    void deletionRollbackRetainsReferenceAndFile(StorageDomain domain) {
        String key = storeThumbnail(domain);
        Long id = parent(domain, key, true);
        transaction().executeWithoutResult(status -> {
            deleteParent(domain, id);
            status.setRollbackOnly();
        });
        assertThat(thumbnail(domain, id)).isEqualTo(key);
        assertThat(storage.exists(StorageRoot.PUBLIC, key)).isTrue();
        assertStrictStartupHealthy();
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    void albumLikeWritersWaitForThumbnailReplacementAndKeepCommittedReference(boolean remove) throws Exception {
        String oldKey = storeThumbnail(StorageDomain.ALBUM);
        Long id = parent(StorageDomain.ALBUM, oldKey, true);
        if (remove) albumLikes.addAlbumLike(id, principal);
        CountDownLatch replacementEntered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch likeStarted = new CountDownLatch(1);
        MockMultipartFile image = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", new byte[]{6});
        given(images.canonicalizeThumbnail(image)).willAnswer(invocation -> {
            replacementEntered.countDown();
            await(release);
            return image;
        });
        var executor = Executors.newFixedThreadPool(2);
        try {
            var replacement = executor.submit(() -> albums.updateAlbum(id, new AlbumUpdateRequest(), image));
            assertThat(replacementEntered.await(5, TimeUnit.SECONDS)).isTrue();
            var like = executor.submit(() -> {
                likeStarted.countDown();
                if (remove) albumLikes.removeAlbumLike(id, principal);
                else albumLikes.addAlbumLike(id, principal);
            });
            assertThat(likeStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> like.get(250, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            release.countDown();
            replacement.get(10, TimeUnit.SECONDS);
            like.get(10, TimeUnit.SECONDS);
            Album reloaded = albumRepository.findById(id).orElseThrow();
            assertThat(reloaded.getThumbnail()).isNotEqualTo(oldKey);
            assertThat(reloaded.getLikeCount()).isEqualTo(remove ? 0 : 1);
            assertThat(storage.exists(StorageRoot.PUBLIC, reloaded.getThumbnail())).isTrue();
            assertThat(storage.exists(StorageRoot.PUBLIC, oldKey)).isFalse();
            assertStrictStartupHealthy();
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void recoveryCannotClaimAgedOperationWhileStagingOrAwaitingBusinessCommit() throws Exception {
        CountDownLatch staging = new CountDownLatch(1);
        CountDownLatch releaseStage = new CountDownLatch(1);
        CountDownLatch promoted = new CountDownLatch(1);
        CountDownLatch releaseCommit = new CountDownLatch(1);
        MockMultipartFile upload = new MockMultipartFile("file", "slow.mp3", "audio/mpeg", new byte[]{4}) {
            @Override
            public InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(new byte[]{4}) {
                    @Override
                    public long transferTo(java.io.OutputStream output) throws IOException {
                        output.write(3);
                        staging.countDown();
                        await(releaseStage);
                        return 1 + super.transferTo(output);
                    }
                };
            }
        };
        StorageMutationRecoveryService recovery = recovery(storage);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var writer = executor.submit(() -> transaction().execute(status -> {
                String key = mutations.store(StorageDomain.TRACK, StorageRoot.PUBLIC, upload, "tracks/audio");
                promoted.countDown();
                await(releaseCommit);
                trackRepository.save(Track.builder().title("Slow").bpm(120).tonality("C")
                        .audioFile(key).isActive(true).user(user).build());
                return key;
            }));
            assertThat(staging.await(5, TimeUnit.SECONDS)).isTrue();
            transaction().executeWithoutResult(status -> entityManager.createQuery(
                            "UPDATE StorageMutation mutation SET mutation.updatedAt = :old")
                    .setParameter("old", LocalDateTime.now().minusSeconds(301)).executeUpdate());
            for (int phase = 0; phase < 2; phase++) {
                assertThat(recovery.recoverBatch().claimed()).isZero();
                assertThat(journal.findAll()).singleElement().satisfies(intent -> {
                    assertThat(intent.getState()).isEqualTo(StorageMutationState.PREPARED);
                    assertThat(intent.getAttemptCount()).isZero();
                    assertThat(intent.getNextAttemptAt()).isNull();
                });
                if (phase == 0) {
                    releaseStage.countDown();
                    assertThat(promoted.await(5, TimeUnit.SECONDS)).isTrue();
                }
            }
            releaseCommit.countDown();
            String key = writer.get(10, TimeUnit.SECONDS);
            assertThat(storage.loadAsResource(StorageRoot.PUBLIC, key).getContentAsByteArray())
                    .containsExactly((byte) 3, (byte) 4);
            assertThat(journal.findAll()).singleElement()
                    .satisfies(intent -> assertThat(intent.getState()).isEqualTo(StorageMutationState.DONE));
            assertStrictStartupHealthy();
        } finally {
            releaseStage.countDown();
            releaseCommit.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    void albumLikeWritersCannotResurrectDeletedThumbnail(boolean remove) throws Exception {
        String key = storeThumbnail(StorageDomain.ALBUM);
        Long id = parent(StorageDomain.ALBUM, key, true);
        if (remove) albumLikes.addAlbumLike(id, principal);
        CountDownLatch deletePending = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch likeStarted = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var deletion = executor.submit(() -> transaction().executeWithoutResult(status -> {
                albums.deleteAlbum(id);
                deletePending.countDown();
                await(release);
            }));
            assertThat(deletePending.await(5, TimeUnit.SECONDS)).isTrue();
            var like = executor.submit(() -> {
                likeStarted.countDown();
                if (remove) albumLikes.removeAlbumLike(id, principal);
                else albumLikes.addAlbumLike(id, principal);
            });
            assertThat(likeStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> like.get(250, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            release.countDown();
            deletion.get(10, TimeUnit.SECONDS);
            if (remove) {
                like.get(10, TimeUnit.SECONDS);
            } else {
                assertThatThrownBy(() -> like.get(10, TimeUnit.SECONDS)).hasCauseInstanceOf(BusinessException.class)
                        .satisfies(error -> assertThat(((BusinessException) error.getCause()).getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.ALBUM_NOT_FOUND));
            }
            Album reloaded = albumRepository.findById(id).orElseThrow();
            assertThat(reloaded.isActive()).isFalse();
            assertThat(reloaded.getThumbnail()).isNull();
            assertThat(reloaded.getLikeCount()).isZero();
            assertThat(storage.exists(StorageRoot.PUBLIC, key)).isFalse();
            assertStrictStartupHealthy();
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void retainedInactiveMissingReferenceStillFailsStrictStartup() {
        parent(StorageDomain.ALBUM, "albums/missing.jpg", false);
        assertThat(integrity.inspect().healthy()).isFalse();
        assertThatThrownBy(() -> guard().run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missingReferences=1");
    }

    @ParameterizedTest
    @CsvSource({"0,false", "1,false", "0,true", "1,true"})
    void partialStageOwnsCleanupAndSurvivesStorageServiceRestart(int failedIndex, boolean cleanupFails)
            throws IOException {
        AtomicBoolean inputClosed = new AtomicBoolean();
        MockMultipartFile partial = new MockMultipartFile("file", "partial.pdf", "application/pdf", new byte[]{7}) {
            @Override
            public InputStream getInputStream() {
                return new InputStream() {
                    private boolean written;

                    @Override
                    public int read() throws IOException {
                        if (written) throw new IOException("Synthetic partial stream failure");
                        written = true;
                        return 7;
                    }

                    @Override
                    public int read(byte[] bytes, int offset, int length) throws IOException {
                        int value = read();
                        bytes[offset] = (byte) value;
                        return 1;
                    }

                    @Override
                    public void close() {
                        inputClosed.set(true);
                    }
                };
            }
        };
        MockMultipartFile good = new MockMultipartFile("file", "complete.pdf", "application/pdf", new byte[]{1, 2});
        List<org.springframework.web.multipart.MultipartFile> files = failedIndex == 0
                ? List.of(partial, good) : List.of(good, partial);
        if (cleanupFails) {
            doReturn(StorageDeleteResult.FAILED).when(localStorage).deleteStaged(any(), anyString(), anyString());
        }
        assertThatThrownBy(() -> transaction().executeWithoutResult(status ->
                mutations.storeAll(StorageDomain.QUESTION, StorageRoot.PRIVATE, files, "questions/attachments")))
                .isInstanceOf(com.atstudio.atstudio.common.exception.TechnicException.class);
        assertThat(inputClosed).isTrue();
        List<StorageMutation> intents = journal.findAll(org.springframework.data.domain.Sort.by("id"));
        assertThat(intents).hasSize(2).allSatisfy(intent -> {
            assertThat(intent.getState()).isEqualTo(cleanupFails ? StorageMutationState.RETRY : StorageMutationState.DONE);
            assertThat(storage.exists(StorageRoot.PRIVATE, intent.getNewKey())).isFalse();
        });
        StorageMutation failed = intents.get(failedIndex);
        Path failedStage = stagePath(failed);
        if (cleanupFails) {
            assertThat(Files.readAllBytes(failedStage)).containsExactly((byte) 7);
            transaction().executeWithoutResult(status -> intents.forEach(intent ->
                    entityManager.find(StorageMutation.class, intent.getId()).scheduleRetry(
                            "SYNTHETIC_RETRY_DUE", LocalDateTime.now().minusSeconds(1), false)));
            LocalStorageService restarted = new LocalStorageService(temporaryRoot.resolve("public").toString(),
                    temporaryRoot.resolve("private").toString());
            restarted.init();
            StorageMutationRecoveryService recovery = recovery(restarted);
            assertThat(recovery.recoverBatch().completed()).isEqualTo(2);
            assertThat(recovery.recoverBatch().claimed()).isZero();
        }
        assertThat(journal.findAll()).allSatisfy(intent -> {
            assertThat(intent.getState()).isEqualTo(StorageMutationState.DONE);
            assertThat(stagePath(intent)).doesNotExist();
        });
    }

    private Path stagePath(StorageMutation intent) {
        return temporaryRoot.resolve("private/.staging").resolve(intent.getOperationId()).resolve(intent.getNewKey());
    }

    private StorageMutationRecoveryService recovery(StorageService target) {
        StorageMutationRecoveryService recovery = new StorageMutationRecoveryService(journalService,
                new StorageCleanupService(target, references), references);
        ReflectionTestUtils.setField(recovery, "batchSize", 50);
        ReflectionTestUtils.setField(recovery, "maxAttempts", 8);
        ReflectionTestUtils.setField(recovery, "staleSeconds", 300L);
        ReflectionTestUtils.setField(recovery, "claimSeconds", 120L);
        return recovery;
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Test latch timed out");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Test interrupted", exception);
        }
    }

    private void assertHistory(Track track, License license, TrackDownload event) {
        License retained = licenses.findByUserAndTrack(user, track).orElseThrow();
        assertThat(retained.getId()).isEqualTo(license.getId());
        assertThat(retained.getLicenseCode()).isEqualTo(license.getLicenseCode());
        assertThat(retained.getCreatedAt()).isEqualTo(license.getCreatedAt());
        assertThat(history.findAll()).singleElement().satisfies(row -> {
            assertThat(row.getId()).isEqualTo(event.getId());
            assertThat(row.getDownloadedAt()).isEqualTo(event.getDownloadedAt());
        });
        assertThat(history.countByUserAndDownloadedAtBetween(user, LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay())).isOne();
        assertThat(trackRepository.findById(track.getId()).orElseThrow().getDownloadCount()).isOne();
    }

    private Track seedTrack() {
        return transaction().execute(status -> {
            String key = mutations.store(StorageDomain.TRACK, StorageRoot.PUBLIC,
                    new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1, 2, 3}), "tracks/audio");
            return trackRepository.save(Track.builder().title("Retained").bpm(120).tonality("C")
                    .audioFile(key).isActive(true).user(user).build());
        });
    }

    private String storeThumbnail(StorageDomain domain) {
        return transaction().execute(status -> mutations.store(domain, StorageRoot.PUBLIC,
                new MockMultipartFile("file", "thumb.jpg", "image/jpeg", new byte[]{4, 5}), "thumbnails"));
    }

    private Long parent(StorageDomain domain, String key, boolean active) {
        if (domain == StorageDomain.ALBUM) {
            return albumRepository.save(Album.builder().title("Album").createdBy(user)
                    .thumbnail(key).isActive(active).build()).getId();
        }
        return playlistRepository.save(Playlist.builder().title("Playlist").user(user)
                .thumbnail(key).isActive(active).build()).getId();
    }

    private void deleteParent(StorageDomain domain, Long id) {
        if (domain == StorageDomain.ALBUM) {
            albums.deleteAlbum(id);
        } else {
            playlists.deletePlaylist(id, principal);
        }
    }

    private String thumbnail(StorageDomain domain, Long id) {
        return domain == StorageDomain.ALBUM ? albumRepository.findById(id).orElseThrow().getThumbnail()
                : playlistRepository.findById(id).orElseThrow().getThumbnail();
    }

    private void assertStrictStartupHealthy() {
        assertThat(integrity.inspect().healthy()).isTrue();
        assertThatCode(() -> guard().run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }

    private StorageIntegrityStartupGuard guard() {
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        properties.setAuditOnStartup(true);
        properties.setStrictOnStartup(true);
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        return new StorageIntegrityStartupGuard(properties, integrity, environment);
    }

    private TransactionTemplate transaction() {
        return new TransactionTemplate(transactionManager);
    }

    enum ThumbnailCase { UNIQUE, SHARED, NONE, LEGACY_INACTIVE }

    @TestConfiguration(proxyBeanMethods = false)
    static class Config {
        @Bean
        LocalStorageService storageService() {
            return new LocalStorageService(temporaryRoot.resolve("public").toString(),
                    temporaryRoot.resolve("private").toString());
        }
    }
}
