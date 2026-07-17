package com.atstudio.atstudio.service.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StorageReferenceCheckerBranchCoverageTest {

    @Mock EntityManager entityManager;
    @Mock TypedQuery<Long> query;

    @InjectMocks StorageReferenceChecker checker;

    @Test
    void nullAndBlankKeysAreNeverQueried() {
        assertThat(checker.isReferenced(StorageDomain.TRACK, null)).isFalse();
        assertThat(checker.isReferenced(StorageDomain.TRACK, "  ")).isFalse();
        verifyNoInteractions(entityManager);
    }

    @ParameterizedTest
    @MethodSource("domainMappings")
    void eachStorageDomainUsesItsOwnLiveReferenceBoundary(StorageDomain domain, String entityFragment) {
        given(entityManager.createQuery(anyString(), eq(Long.class))).willReturn(query);
        given(query.setParameter("key", "shared/key.bin")).willReturn(query);
        given(query.getSingleResult()).willReturn(1L);

        assertThat(checker.isReferenced(domain, "shared/key.bin")).isTrue();

        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpql.capture(), eq(Long.class));
        assertThat(jpql.getValue()).contains(entityFragment);
        verify(query).setParameter("key", "shared/key.bin");
    }

    @Test
    void zeroReferencesAllowStorageCleanup() {
        given(entityManager.createQuery(anyString(), eq(Long.class))).willReturn(query);
        given(query.setParameter("key", "orphan/key.bin")).willReturn(query);
        given(query.getSingleResult()).willReturn(0L);

        assertThat(checker.isReferenced(StorageDomain.NOTICE, "orphan/key.bin")).isFalse();
    }

    private static Stream<Arguments> domainMappings() {
        return Stream.of(
                Arguments.of(StorageDomain.TRACK, "FROM Track track"),
                Arguments.of(StorageDomain.PLAYLIST, "FROM Playlist playlist"),
                Arguments.of(StorageDomain.ALBUM, "FROM Album album"),
                Arguments.of(StorageDomain.COMPANY_CERTIFICATION, "FROM CompanyCertificationDocument document"),
                Arguments.of(StorageDomain.NOTICE, "FROM NoticeAttachment attachment"),
                Arguments.of(StorageDomain.QUESTION, "FROM QuestionAttachment attachment"));
    }
}
