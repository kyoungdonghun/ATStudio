package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.StorageMutationAdmissionProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.repository.StorageMutationRepository;
import com.atstudio.atstudio.service.storage.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StorageMutationAdmissionTransactionTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void admissionEnclosesRealSpringOuterAndRequiresNewJournalTransactionCleanup(boolean rollback) throws Exception {
        var filter = new StorageMutationAdmissionFilter(new StorageMutationAdmissionProperties(),
                new TrustedClientIdentityResolver(new TrustedClientIdentityProperties(), new AcceptanceProperties()),
                new StrictHttpFirewall());
        AtomicInteger connections = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        List<String> events = new ArrayList<>();
        DataSource source = mock(DataSource.class);
        when(source.getConnection()).thenAnswer(invocation -> {
            assertThat(filter.activeRequestCount()).isEqualTo(1);
            int count = connections.incrementAndGet();
            peak.accumulateAndGet(count, Math::max);
            events.add("acquire:" + count);
            Connection connection = mock(Connection.class);
            when(connection.getAutoCommit()).thenReturn(true);
            doAnswer(call -> {
                assertThat(filter.activeRequestCount()).isEqualTo(1);
                events.add("close:" + connections.decrementAndGet());
                return null;
            }).when(connection).close();
            return connection;
        });
        var manager = new DataSourceTransactionManager(source);
        StorageMutationRepository repository = mock(StorageMutationRepository.class);
        when(repository.saveAllAndFlush(any())).thenAnswer(invocation -> {
            assertThat(connections.get()).isEqualTo(2);
            List<StorageMutation> mutations = invocation.getArgument(0);
            ReflectionTestUtils.setField(mutations.get(0), "id", 1L);
            return mutations;
        });
        when(repository.findAllById(any())).thenReturn(List.of());
        var factory = new ProxyFactory(new StorageMutationJournalService(repository));
        factory.setProxyTargetClass(true);
        var interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        interceptor.afterPropertiesSet();
        factory.addAdvice(interceptor);
        var journal = (StorageMutationJournalService) factory.getProxy();
        StorageCleanupService cleanup = mock(StorageCleanupService.class);
        when(cleanup.cleanupOld(any(), any(), any())).thenAnswer(invocation -> {
            assertThat(filter.activeRequestCount()).isEqualTo(1);
            events.add("afterCommitCleanup");
            return StorageCleanupService.CleanupOutcome.DONE;
        });
        var coordinator = new StorageMutationCoordinator(mock(StorageService.class), journal, cleanup);
        var request = new MockHttpServletRequest("DELETE", "/api/notices/1");
        request.setServletPath("/api/notices/1");
        request.setRemoteAddr("192.0.2.1");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertThat(connections.get()).isZero();
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                assertThat(connections.get()).isEqualTo(1);
                coordinator.deleteAfterCommit(StorageDomain.NOTICE, StorageRoot.PRIVATE, "synthetic/key");
                assertThat(connections.get()).isEqualTo(1);
                // A competing request never reaches even the first connection acquisition.
                try {
                    var competing = new MockHttpServletRequest("PUT", "/api/albums/1");
                    competing.setServletPath("/api/albums/1");
                    competing.setRemoteAddr("192.0.2.1");
                    var rejected = new MockHttpServletResponse();
                    filter.doFilter(competing, rejected,
                            (nested, result) -> { throw new AssertionError("Rejected transaction started"); });
                    assertThat(rejected.getStatus()).isEqualTo(429);
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
                if (rollback) status.setRollbackOnly();
            });
            assertThat(connections.get()).isZero();
            assertThat(filter.activeRequestCount()).isEqualTo(1);
        });
        assertThat(peak.get()).isEqualTo(2);
        assertThat(connections.get()).isZero();
        assertThat(events.get(events.size() - 1)).isEqualTo("close:0");
        assertThat(events.contains("afterCommitCleanup")).isEqualTo(!rollback);
        assertThat(filter.activeRequestCount()).isZero();
        assertThat(filter.trackedClientCount()).isZero();
    }
}
