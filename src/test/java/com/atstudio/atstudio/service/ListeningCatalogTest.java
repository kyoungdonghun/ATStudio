package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.dto.track.TrackSearchRequest;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ListeningCatalogTest {
    ListeningProperties policy;
    TrackRepository tracks;

    @BeforeEach void setup() {
        policy = new ListeningProperties(); policy.setEnabled(true); policy.setTrackIds(Set.of(1L));
        tracks = mock(TrackRepository.class);
    }

    private TrackService service() {
        var service = new TrackService(tracks, null, null, null, null, null, null, null, null, null, null, null, null);
        ReflectionTestUtils.setField(service, "listening", policy);
        return service;
    }

    @Test void nonselectedDetailAndStreamNeverTouchStorageOrDatabase() {
        var service = service();
        assertThatThrownBy(() -> service.getTrack(2L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.getStreamResource(2L)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(tracks);
    }

    @Test void batchDropsNonselectedIdsBeforeBothQueries() {
        var tags = mock(TrackTagRepository.class);
        var service = new PlayableTrackService(tracks, tags);
        ReflectionTestUtils.setField(service, "listening", policy);
        when(tracks.findAllActiveByIdIn(List.of(1L))).thenReturn(List.of());
        when(tags.findAllWithTagByTrackIdIn(List.of(1L))).thenReturn(List.of());
        assertThat(service.hydrate(List.of(2L, 1L, 2L))).isEmpty();
        verify(tracks).findAllActiveByIdIn(List.of(1L));
        verify(tags).findAllWithTagByTrackIdIn(List.of(1L));
        verifyNoMoreInteractions(tracks, tags);
    }

    @Test void emptySelectionShortCircuitsBatchAndTags() {
        policy.setTrackIds(Set.of());
        var tags = mock(TrackTagRepository.class);
        var hydrate = new PlayableTrackService(tracks, tags);
        ReflectionTestUtils.setField(hydrate, "listening", policy);
        assertThat(hydrate.hydrate(List.of(1L))).isEmpty();
        var em = mock(EntityManager.class);
        var tagService = new TagService(null, tags, em);
        ReflectionTestUtils.setField(tagService, "listening", policy);
        assertThat(tagService.getAllTags(null)).isEmpty();
        assertThat(tagService.getAvailableTags(null, null, null, null, null, null)).isEmpty();
        verifyNoInteractions(tracks, tags, em);
    }

    @Test void availableTagSqlIsBoundToSelectionAlongsideFilters() {
        var em = mock(EntityManager.class);
        var query = mock(Query.class);
        when(em.createNativeQuery(anyString(), eq(Tag.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());
        var service = new TagService(null, null, em);
        ReflectionTestUtils.setField(service, "listening", policy);
        service.getAvailableTags(List.of("pop"), null, null, null, 80, 120);
        verify(em).createNativeQuery(contains("AND tr.id IN (?)"), eq(Tag.class));
        verify(query).setParameter(3, 1L);
        verify(query).setParameter(4, 80);
        verify(query).setParameter(5, 120);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test void listAppliesSelectionInsidePaginationQuery() {
        when(tracks.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(call -> {
            Specification<Track> spec = call.getArgument(0);
            Root<Track> root = mock(Root.class);
            Path path = mock(Path.class);
            when(root.get(anyString())).thenReturn(path);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            spec.toPredicate(root, null, cb);
            verify(path).in(List.of(1L));
            return Page.empty();
        });
        var search = new TrackSearchRequest(); search.setPage(1); search.setSize(20);
        service().getTracks(search);
        verify(tracks).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test void adminThumbnailCanReadInactiveNonselectedTrack() {
        var service = service();
        var storage = mock(com.atstudio.atstudio.service.storage.StorageService.class);
        ReflectionTestUtils.setField(service, "storageService", storage);
        var track = Track.builder().title("Fixture").thumbnail("tracks/thumbnail/hidden.jpg").build();
        when(tracks.findById(2L)).thenReturn(java.util.Optional.of(track));
        var resource = new org.springframework.core.io.ByteArrayResource(new byte[]{1, 2});
        when(storage.loadAsResource(com.atstudio.atstudio.service.storage.StorageRoot.PUBLIC,
                "tracks/thumbnail/hidden.jpg")).thenReturn(resource);
        assertThat(service.getThumbnailForAdmin(2L)).isSameAs(resource);
        assertThat(track.isActive()).isFalse();
    }

    @Test void thumbnailOwnershipUsesExactPersistedKeyEvenWhenDatabaseCollationAliases() {
        var service = service();
        var storage = mock(com.atstudio.atstudio.service.storage.StorageService.class);
        ReflectionTestUtils.setField(service, "storageService", storage);
        when(tracks.findSelectedThumbnailKeys("tracks/thumbnail/UPPER.jpg", List.of(1L)))
                .thenReturn(List.of("tracks/thumbnail/upper.jpg"));
        assertThatThrownBy(() -> service.getPublicThumbnail("UPPER.jpg")).isInstanceOf(BusinessException.class);
        verifyNoInteractions(storage);
    }

    @Test void defaultDisabledPolicyKeepsSellingCatalog() {
        assertThat(new ListeningProperties().includes(999L)).isTrue();
        policy.setEnabled(false);
        var tags = mock(TrackTagRepository.class);
        var hydrate = new PlayableTrackService(tracks, tags);
        ReflectionTestUtils.setField(hydrate, "listening", policy);
        hydrate.hydrate(List.of(2L));
        verify(tracks).findAllActiveByIdIn(List.of(2L));
    }
}
