package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.*;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.storage.*;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Isolated H2/JPA only. No Boot config, network, mail, provider or production settings. */
@SpringJUnitConfig(ListeningDatabaseCatalogTest.Config.class)
class ListeningDatabaseCatalogTest {
    @Autowired ListeningCatalogService catalog;
    @Autowired SiteSettingRepository settings;
    @Autowired TrackRepository tracks;
    @Autowired UserRepository users;
    @Autowired AdminOperationAuditLogRepository audits;
    @Autowired PlatformTransactionManager manager;
    @Autowired Writer writer;
    @Autowired SiteSettingService generic;
    @Autowired TrackService trackService;
    @Autowired com.atstudio.atstudio.service.audio.TrackAudioProcessingTransactions audio;
    @Autowired StorageContinuityGuard guard;
    long trackId;
    TransactionTemplate tx;

    @BeforeEach void seed() {
        tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> {
            audits.deleteAllInBatch(); tracks.deleteAllInBatch(); users.deleteAllInBatch(); settings.deleteAllInBatch();
            var user = users.saveAndFlush(User.builder().email("fixture@example.invalid").nickname("Fixture").build());
            var track = tracks.saveAndFlush(Track.builder().title("Approved original").tonality("C")
                    .bpm(100).audioFile("tracks/audio/old.mp3").thumbnail("tracks/thumbnail/old.jpg")
                    .isActive(true).user(user).build());
            trackId = track.getId();
            settings.saveAndFlush(new SiteSetting(ReservedSettingPolicy.CATALOG_KEY,
                    "{\"revision\":0,\"trackIds\":[]}"));
        });
    }

    ListeningCatalogService.Update update(long revision, List<Long> ids) {
        return new ListeningCatalogService.Update(revision, ids, "Synthetic local approval", true);
    }

    @Test void revisionAuditAndRestartReadUseDatabase() {
        var result = catalog.replace(update(0, List.of(trackId)), 99L);
        assertThat(result.revision()).isEqualTo(1);
        assertThat(catalog.get()).isEqualTo(result);
        assertThat(audits.count()).isEqualTo(1);
        assertThat(audits.findAll().get(0).getAfterState()).contains(result.digest());
        assertThatThrownBy(() -> catalog.replace(update(0, List.of()), 99L)).isInstanceOf(ResponseStatusException.class);
        assertThat(catalog.get().trackIds()).containsExactly(trackId);
    }

    @Test void missingCorruptAndEmptyNeverFallback() {
        tx.executeWithoutResult(s -> settings.deleteAll());
        assertThatThrownBy(catalog::get).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> catalog.replace(update(0, List.of(trackId)), 1L)).isInstanceOf(ResponseStatusException.class);
        tx.executeWithoutResult(s -> settings.save(new SiteSetting(ReservedSettingPolicy.CATALOG_KEY, "{\"trackIds\":[]}")));
        assertThatThrownBy(catalog::get).isInstanceOf(ResponseStatusException.class);
        tx.executeWithoutResult(s -> settings.findBySettingKey(ReservedSettingPolicy.CATALOG_KEY).orElseThrow()
                .updateValue("{\"revision\":4,\"trackIds\":[]}"));
        assertThat(catalog.get().trackIds()).isEmpty();
    }

    @Test void invalidPendingAndCreditInputsDoNotPublish() {
        assertThatThrownBy(() -> catalog.replace(new ListeningCatalogService.Update(0, List.of(trackId), "x", false), 1L))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> catalog.replace(update(0, List.of(trackId, trackId)), 1L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> catalog.replace(update(0, List.of(-1L)), 1L)).isInstanceOf(ResponseStatusException.class);
        tx.executeWithoutResult(s -> tracks.findById(trackId).orElseThrow().enqueueAudio("tracks/audio/pending.wav", false));
        assertThatThrownBy(() -> catalog.replace(update(0, List.of(trackId)), 1L)).isInstanceOf(ResponseStatusException.class);
        assertThat(catalog.get().revision()).isZero();
    }

    @Test void auditFailureRollsBackCatalog() {
        // A post-audit exception proves both catalog and already-flushed audit roll back together.
        tx.executeWithoutResult(s -> settings.findBySettingKey(ReservedSettingPolicy.CATALOG_KEY).orElseThrow()
                .updateValue("{\"revision\":0,\"trackIds\":[]}"));
        assertThatThrownBy(() -> writer.replaceThenFail(update(0, List.of(trackId)))).isInstanceOf(IllegalStateException.class);
        assertThat(catalog.get().revision()).isZero();
        assertThat(audits.count()).isZero();
    }

    @Test void publishVsEditIsSerializedBySharedDatabaseLock() throws Exception {
        CountDownLatch inside = new CountDownLatch(1), release = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> edit = pool.submit(() -> writer.edit(trackId, inside, release));
            assertThat(inside.await(10, TimeUnit.SECONDS)).isTrue();
            Future<?> publish = pool.submit(() -> catalog.replace(update(0, List.of(trackId)), 1L));
            Thread.sleep(150);
            assertThat(publish.isDone()).isFalse();
            release.countDown(); edit.get(10, TimeUnit.SECONDS); publish.get(10, TimeUnit.SECONDS);
            assertThatThrownBy(() -> writer.edit(trackId, new CountDownLatch(0), new CountDownLatch(0)))
                    .isInstanceOf(ResponseStatusException.class);
            assertThat(tracks.findById(trackId).orElseThrow().getTitle()).isEqualTo("Edited before approval");
        } finally { release.countDown(); pool.shutdownNow(); }
    }

    @Test void repeatableSnapshotPinsOldContentAndKeyAcrossUnpublishEdit() throws Exception {
        catalog.replace(update(0, List.of(trackId)), 1L);
        CountDownLatch snapshotRead = new CountDownLatch(1), changed = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> read = pool.submit(() -> {
                var readTx = new TransactionTemplate(manager); readTx.setReadOnly(true);
                readTx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
                return readTx.execute(status -> {
                    var before = catalog.snapshot(); snapshotRead.countDown(); await(changed);
                    Track track = tracks.findById(trackId).orElseThrow();
                    assertThat(before.trackIds()).contains(trackId);
                    assertThat(catalog.snapshot().revision()).isEqualTo(before.revision());
                    return track.getTitle() + ":" + track.listeningAudioFile();
                });
            });
            assertThat(snapshotRead.await(10, TimeUnit.SECONDS)).isTrue();
            Future<?> mutation = pool.submit(() -> {
                catalog.replace(update(1, List.of()), 1L);
                writer.edit(trackId, new CountDownLatch(0), new CountDownLatch(0));
            });
            Thread.sleep(150);
            assertThat(mutation.isDone()).isFalse();
            changed.countDown();
            assertThat(read.get(10, TimeUnit.SECONDS)).isEqualTo("Approved original:tracks/audio/old.mp3");
            mutation.get(10, TimeUnit.SECONDS);
            assertThat(catalog.get().trackIds()).isEmpty();
        } finally { changed.countDown(); pool.shutdownNow(); }
    }

    @Test void concurrentExpectedRevisionHasOneWinner() throws Exception {
        var pool = Executors.newFixedThreadPool(2); var start = new CountDownLatch(1);
        try {
            Callable<Boolean> call = () -> { await(start); try { catalog.replace(update(0, List.of(trackId)), 1L); return true; }
                catch (ResponseStatusException conflict) { assertThat(conflict.getStatusCode().value()).isEqualTo(409); return false; } };
            var first = pool.submit(call); var second = pool.submit(call); start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(audits.count()).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    @Test void genericAliasesAndPersistedReservedIdentityAreDenied() {
        for (String key : List.of("listening.catalog.v1", "LISTENING.CATALOG.V1", "lístening.catalog.v1",
                "listening.catalog.v1 ", "%6cistening.catalog.v1", "listening.catalog.v1\u00ad")) {
            assertThatThrownBy(() -> generic.getValue(key)).isInstanceOf(ResponseStatusException.class);
            assertThatThrownBy(() -> generic.getValue(key, "default")).isInstanceOf(ResponseStatusException.class);
            assertThatThrownBy(() -> generic.setValue(key, "hidden")).isInstanceOf(ResponseStatusException.class);
        }
        generic.setValue("COMPANY_CERT_GUIDE", "compatible");
        assertThat(generic.getValue("COMPANY_CERT_GUIDE")).isEqualTo("compatible");
    }

    @Test void realPublicServicesApplySelectedCountDetailAndThumbnailSnapshot() {
        var search = new com.atstudio.atstudio.dto.track.TrackSearchRequest(); search.setPage(1); search.setSize(20);
        assertThat(trackService.getTracks(search).getPageInfo().getTotal()).isZero();
        catalog.replace(update(0, List.of(trackId)), 1L);
        assertThat(trackService.getTracks(search).getPageInfo().getTotal()).isEqualTo(1);
        assertThat(trackService.getTrack(trackId).title()).isEqualTo("Approved original");
        assertThat(trackService.getPublicThumbnail("old.jpg")).isNotNull();
        assertThatThrownBy(() -> trackService.updateTrack(trackId,
                new com.atstudio.atstudio.dto.track.TrackUpdateRequest(), null, null)).isInstanceOf(ResponseStatusException.class);
        catalog.replace(update(1, List.of()), 1L);
        assertThatThrownBy(() -> trackService.getTrack(trackId)).isInstanceOf(com.atstudio.atstudio.common.exception.BusinessException.class);
        assertThatThrownBy(() -> trackService.getPublicThumbnail("old.jpg")).isInstanceOf(com.atstudio.atstudio.common.exception.BusinessException.class);
    }

    @Test void actualWorkerPendingGenerationCannotBeApprovedBeforeCompletion() {
        tx.executeWithoutResult(status -> tracks.findById(trackId).orElseThrow().enqueueAudio("tracks/audio/pending.wav", false));
        var claim = audio.claimNext(); assertThat(claim).isNotNull();
        assertThatThrownBy(() -> catalog.replace(update(0, List.of(trackId)), 1L)).isInstanceOf(ResponseStatusException.class);
        var generated = org.mockito.Mockito.mock(StorageMutationCoordinator.GeneratedWrite.class);
        org.mockito.Mockito.when(generated.key()).thenReturn("tracks/audio/generated.mp3");
        var analysis = new com.atstudio.atstudio.service.audio.AudioAnalysisResult(10, "[0]",
                com.atstudio.atstudio.service.audio.AudioAnalysisFormat.WAV, 44100, 44100, 1);
        assertThat(audio.complete(claim, generated, analysis)).isTrue();
        catalog.replace(update(0, List.of(trackId)), 1L);
        assertThatThrownBy(() -> audio.complete(claim, generated, analysis)).isInstanceOf(ResponseStatusException.class);
    }

    @Test void freezeDrainsCatalogWriterAndRejectsNewCommitButNotReads() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2); String capture = null;
        try {
            var edit = pool.submit(() -> writer.edit(trackId, entered, release));
            assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
            var freeze = pool.submit(() -> guard.freeze(java.time.Duration.ofSeconds(5)));
            Thread.sleep(150); assertThat(freeze.isDone()).isFalse();
            release.countDown(); edit.get(10, TimeUnit.SECONDS); capture = freeze.get(10, TimeUnit.SECONDS);
            assertThatThrownBy(() -> catalog.replace(update(0, List.of(trackId)), 1L))
                    .isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            assertThat(catalog.get().revision()).isZero();
        } finally {
            release.countDown(); pool.shutdownNow();
            if (capture != null) guard.releaseReconciled(capture);
        }
    }

    static void await(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Fixture timeout"); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IllegalStateException(interrupted); }
    }

    static class Writer {
        final ListeningCatalogService catalog; final TrackRepository tracks;
        Writer(ListeningCatalogService catalog, TrackRepository tracks) { this.catalog = catalog; this.tracks = tracks; }
        @Transactional @StorageReferenceMutation
        public void edit(Long id, CountDownLatch entered, CountDownLatch release) {
            catalog.requireUnselected(id);
            Track track = tracks.findByIdForUpdate(id).orElseThrow();
            entered.countDown(); await(release);
            track.update("Edited before approval", null, null, null);
            track.useDirectAudio("tracks/audio/new.mp3", 10, "[0]");
        }
        @Transactional @StorageReferenceMutation
        public void replaceThenFail(ListeningCatalogService.Update update) {
            catalog.replace(update, 1L); throw new IllegalStateException("Synthetic post-audit transaction failure");
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement @EnableJpaAuditing
    @EnableJpaRepositories(basePackages = "com.atstudio.atstudio.repository")
    @Import({ListeningCatalogService.class, AdminOperationAuditService.class, SiteSettingService.class,
            ListeningCatalogWriteConfiguration.class, StorageReferenceAdmissionConfiguration.class})
    static class Config {
        @Bean DataSource dataSource() {
            var data = new DriverManagerDataSource(); data.setDriverClassName("org.h2.Driver");
            data.setUrl("jdbc:h2:mem:listening-catalog;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
            data.setUsername("sa"); return data;
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource data) {
            var factory = new LocalContainerEntityManagerFactoryBean(); factory.setDataSource(data);
            factory.setPackagesToScan("com.atstudio.atstudio.entity"); factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop")); return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) { return new JpaTransactionManager(factory); }
        @Bean ListeningProperties listeningProperties() {
            var p = new ListeningProperties(); p.setEnabled(true);
            p.setCatalogSource(ListeningProperties.CatalogSource.DATABASE); p.setTrackIds(Set.of(9999L)); return p;
        }
        @Bean StorageContinuityGuard guard() throws java.io.IOException {
            return new StorageContinuityGuard(java.nio.file.Files.createTempDirectory("ats-catalog-fixture-").toString());
        }
        @Bean StorageMutationCoordinator mutations() { return org.mockito.Mockito.mock(StorageMutationCoordinator.class); }
        @Bean com.atstudio.atstudio.service.audio.TrackAudioProcessingTransactions audio(
                TrackRepository tracks, StorageMutationCoordinator mutations) {
            return new com.atstudio.atstudio.service.audio.TrackAudioProcessingTransactions(tracks, mutations);
        }
        @Bean TrackService trackService(TrackRepository tracks, TrackTagRepository trackTags, TagRepository tags,
                UserRepository users, StorageMutationCoordinator mutations) {
            StorageService storage = org.mockito.Mockito.mock(StorageService.class);
            org.mockito.Mockito.when(storage.loadAsResource(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                    .thenReturn(new org.springframework.core.io.ByteArrayResource(new byte[]{1}));
            return new TrackService(tracks, trackTags, tags, users,
                    org.mockito.Mockito.mock(com.atstudio.atstudio.service.audio.AudioAnalysisService.class),
                    org.mockito.Mockito.mock(com.atstudio.atstudio.service.image.CanonicalImageService.class), storage, mutations,
                    org.mockito.Mockito.mock(LikeRepository.class), org.mockito.Mockito.mock(TrackDownloadRepository.class),
                    org.mockito.Mockito.mock(LicenseRepository.class), org.mockito.Mockito.mock(PlaylistTrackRepository.class),
                    org.mockito.Mockito.mock(AlbumTrackRepository.class));
        }
        @Bean Writer writer(ListeningCatalogService catalog, TrackRepository tracks) { return new Writer(catalog, tracks); }
    }
}
