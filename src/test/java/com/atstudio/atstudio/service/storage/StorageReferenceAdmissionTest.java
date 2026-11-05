package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.config.StorageReferenceAdmissionConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageReferenceAdmissionTest {
    @TempDir Path directory;

    @Test
    void defaultDisabledAdvisorPreservesExistingOuterTransactionCalls() {
        try(var guard=StorageContinuityGuard.disabled()) {
            var factory=new ProxyFactory(new DisabledWriter());
            factory.setProxyTargetClass(true);
            factory.addAdvisor(new StorageReferenceAdmissionConfiguration().storageReferenceAdmissionAdvisor(guard));
            var writer=(DisabledWriter)factory.getProxy();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try { assertThat(writer.changeReference()).isEqualTo("unchanged"); }
            finally { TransactionSynchronizationManager.setActualTransactionActive(false); }
        }
    }

    public static class DisabledWriter {
        @StorageReferenceMutation public String changeReference() { return "unchanged"; }
    }

    @Test
    void auditedReferenceMutationEntryPointsCarryAdmissionAnnotation() throws Exception {
        java.util.Map<String,java.util.Set<String>> audited=java.util.Map.ofEntries(
                java.util.Map.entry("TrackService",java.util.Set.of("createTrack","updateTrack","deleteTrack","retryAudioProcessing")),
                java.util.Map.entry("AlbumService",java.util.Set.of("createAlbum","updateAlbum","deleteAlbum")),
                java.util.Map.entry("PlaylistService",java.util.Set.of("createPlaylist","updatePlaylist","deletePlaylist")),
                java.util.Map.entry("NoticeService",java.util.Set.of("createNotice","updateNotice","deleteNotice")),
                java.util.Map.entry("QuestionService",java.util.Set.of("createQuestion","deleteQuestion")),
                java.util.Map.entry("CompanyCertificationService",java.util.Set.of("apply","resubmit")),
                java.util.Map.entry("UserService",java.util.Set.of("withdraw")),
                java.util.Map.entry("audio.TrackAudioProcessingTransactions",java.util.Set.of("claimNext","complete","fail","recoverInterruptedBatch")),
                java.util.Map.entry("storage.StorageMutationJournalService",java.util.Set.of("prepare","transition","deferPreserved","recordCleanupFailure","claimBatch","retryClaimed")));
        for(var entry:audited.entrySet()) {
            Class<?> type=Class.forName("com.atstudio.atstudio.service."+entry.getKey());
            for(String method:entry.getValue()) {
                var matches=java.util.Arrays.stream(type.getDeclaredMethods()).filter(value -> value.getName().equals(method)).toList();
                assertThat(matches).as(entry.getKey()+"."+method).isNotEmpty()
                        .allMatch(value -> value.isAnnotationPresent(StorageReferenceMutation.class));
            }
        }
        for(String normal:java.util.List.of("auth.AuthService","DownloadService")) {
            assertThat(Class.forName("com.atstudio.atstudio.service."+normal).getDeclaredMethods())
                    .noneMatch(method -> method.isAnnotationPresent(StorageReferenceMutation.class));
        }
    }

    @Test
    void actualSpringInfrastructureAdvisorIsAppliedBeforeTransactionCreation() throws Exception {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(StorageContinuityGuard.class, () -> new StorageContinuityGuard(directory.resolve("state").toString()));
            context.register(StorageReferenceAdmissionConfiguration.class, ContextConfiguration.class);
            context.refresh();
            StorageContinuityGuard guard = context.getBean(StorageContinuityGuard.class);
            Writer writer = context.getBean(Writer.class);
            assertThat(org.springframework.aop.support.AopUtils.isAopProxy(writer)).isTrue();
            writer.changeReference();
            String capture = guard.freeze(Duration.ZERO);
            assertThatThrownBy(writer::changeReference).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            writer.ordinaryAuthOrDownloadWrite();
            guard.releaseReconciled(capture);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    static class ContextConfiguration {
        @Bean Writer writer(StorageContinuityGuard guard) { return new Writer(guard, new AtomicInteger()); }
        @Bean DataSourceTransactionManager transactionManager() throws Exception {
            DataSource source = mock(DataSource.class);
            when(source.getConnection()).thenAnswer(call -> {
                Connection connection = mock(Connection.class);
                when(connection.getAutoCommit()).thenReturn(true);
                return connection;
            });
            return new DataSourceTransactionManager(source);
        }
    }

    @Test
    void admissionSurroundsTransactionAndCallbacksButOrdinaryWritesContinue() throws Exception {
        try (var guard = new StorageContinuityGuard(directory.resolve("state").toString())) {
            AtomicInteger connections = new AtomicInteger(), callbacks = new AtomicInteger();
            DataSource data = mock(DataSource.class);
            when(data.getConnection()).thenAnswer(call -> {
                connections.incrementAndGet();
                Connection connection = mock(Connection.class);
                when(connection.getAutoCommit()).thenReturn(true);
                return connection;
            });
            var manager = new DataSourceTransactionManager(data);
            var transaction = new TransactionInterceptor();
            transaction.setTransactionManager(manager);
            transaction.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
            transaction.afterPropertiesSet();
            var factory = new ProxyFactory(new Writer(guard, callbacks));
            factory.setProxyTargetClass(true);
            factory.addAdvisor(new StorageReferenceAdmissionConfiguration().storageReferenceAdmissionAdvisor(guard));
            factory.addAdvice(transaction);
            Writer writer = (Writer) factory.getProxy();
            writer.changeReference();
            assertThat(callbacks.get()).isEqualTo(2);
            String capture = guard.freeze(Duration.ZERO);
            int before = connections.get();
            assertThatThrownBy(writer::changeReference).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            assertThat(connections.get()).isEqualTo(before);
            writer.ordinaryAuthOrDownloadWrite();
            assertThat(connections.get()).isEqualTo(before + 1);
            guard.releaseReconciled(capture);
            assertThatThrownBy(() -> new TransactionTemplate(manager).executeWithoutResult(status -> writer.changeReference()))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("precede transaction");
        }
    }

    public static class Writer {
        private final StorageContinuityGuard guard;
        private final AtomicInteger callbacks;
        Writer(StorageContinuityGuard guard, AtomicInteger callbacks) { this.guard=guard; this.callbacks=callbacks; }
        @StorageReferenceMutation @Transactional
        public void changeReference() {
            assertThat(guard.currentThreadAdmitted()).isTrue();
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { assertThat(guard.currentThreadAdmitted()).isTrue(); callbacks.incrementAndGet(); }
                @Override public void afterCompletion(int status) { assertThat(guard.currentThreadAdmitted()).isTrue(); callbacks.incrementAndGet(); }
            });
        }
        @Transactional public void ordinaryAuthOrDownloadWrite() { assertThat(guard.currentThreadAdmitted()).isFalse(); }
    }
}
