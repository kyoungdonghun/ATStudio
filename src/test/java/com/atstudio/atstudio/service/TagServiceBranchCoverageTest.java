package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceBranchCoverageTest {

    @Mock TagRepository tagRepository;
    @Mock TrackTagRepository trackTagRepository;
    @Mock EntityManager entityManager;
    @Mock Query query;

    @InjectMocks TagService tagService;

    @Test
    void emptySearchInputsProduceAnUnparameterizedActiveTrackQuery() {
        given(entityManager.createNativeQuery(anyString(), eq(Tag.class))).willReturn(query);
        given(query.getResultList()).willReturn(List.of());

        assertThat(tagService.getAvailableTags(
                null,
                List.of("   "),
                List.of(),
                List.of("", "  "),
                null,
                null)).isEmpty();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture(), eq(Tag.class));
        assertThat(sql.getValue())
                .contains("WHERE tr.is_active = true")
                .doesNotContain("tr.bpm >=")
                .doesNotContain("tr.bpm <=")
                .doesNotContain("tg1.type");
        verify(query, never()).setParameter(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void combinedTagAndBpmFiltersPreserveAndSemanticsAndParameterOrder() {
        Tag matched = tag(7L, "Piano, Synth", TagType.INSTRUMENT);
        given(entityManager.createNativeQuery(anyString(), eq(Tag.class))).willReturn(query);
        given(query.setParameter(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).willReturn(query);
        given(query.getResultList()).willReturn(List.of(matched));

        var result = tagService.getAvailableTags(
                List.of(" Rock ", "Pop"),
                List.of("Energetic"),
                List.of("Piano, Synth", "808 #Kit"),
                List.of("Shorts", "Tutorial"),
                60,
                140);

        assertThat(result).singleElement().satisfies(tag -> {
            assertThat(tag.id()).isEqualTo(7L);
            assertThat(tag.type()).isEqualTo(TagType.INSTRUMENT);
        });
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture(), eq(Tag.class));
        assertThat(sql.getValue())
                .contains("tg1.type = ?", "tg2.type = ?")
                .contains("tg3.type = ?")
                .contains("tg4.type = ?", "tg5.type = ?")
                .contains("tg6.type = ?", "tg7.type = ?")
                .contains("tr.bpm >= ?", "tr.bpm <= ?")
                .doesNotContain("type = '");
        verify(query).setParameter(1, "GENRE");
        verify(query).setParameter(2, "Rock");
        verify(query).setParameter(3, "GENRE");
        verify(query).setParameter(4, "Pop");
        verify(query).setParameter(5, "MOOD");
        verify(query).setParameter(6, "Energetic");
        verify(query).setParameter(7, "INSTRUMENT");
        verify(query).setParameter(8, "Piano, Synth");
        verify(query).setParameter(9, "INSTRUMENT");
        verify(query).setParameter(10, "808 #Kit");
        verify(query).setParameter(11, "USAGE");
        verify(query).setParameter(12, "Shorts");
        verify(query).setParameter(13, "USAGE");
        verify(query).setParameter(14, "Tutorial");
        verify(query).setParameter(15, 60);
        verify(query).setParameter(16, 140);
    }

    @Test
    void queryNamesCanonicalizeBeforeDistinctWithoutSplittingLegacySpecialCharacters() {
        given(entityManager.createNativeQuery(anyString(), eq(Tag.class))).willReturn(query);
        given(query.setParameter(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).willReturn(query);
        given(query.getResultList()).willReturn(List.of());

        tagService.getAvailableTags(
                List.of("  Cafe\u0301\u00A0\u00A0Beat  ", "Caf\u00E9 Beat"),
                null,
                List.of(" Piano,  Synth ", "808 #Kit"),
                null,
                null,
                null);

        verify(query).setParameter(1, "GENRE");
        verify(query).setParameter(2, "Caf\u00E9 Beat");
        verify(query).setParameter(3, "INSTRUMENT");
        verify(query).setParameter(4, "Piano, Synth");
        verify(query).setParameter(5, "INSTRUMENT");
        verify(query).setParameter(6, "808 #Kit");
        verify(query, times(6)).setParameter(
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void retainingTheSameTagNameDoesNotTriggerAFalseDuplicateRejection() {
        Tag existing = tag(7L, "Shorts", TagType.USAGE);
        given(tagRepository.findById(7L)).willReturn(Optional.of(existing));
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Shorts");
        request.setType(TagType.MOOD);

        var response = tagService.updateTag(7L, request);

        assertThat(response.name()).isEqualTo("Shorts");
        assertThat(response.type()).isEqualTo(TagType.MOOD);
        verify(tagRepository, never()).existsByName("Shorts");
    }

    private Tag tag(Long id, String name, TagType type) {
        Tag tag = Tag.builder().name(name).type(type).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }
}
