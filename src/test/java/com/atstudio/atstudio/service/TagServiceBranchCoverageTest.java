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

        assertThat(tagService.getAvailableTags(null, "   ", ", ,", null, null)).isEmpty();

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
        Tag matched = tag(7L, "Shorts", TagType.USAGE);
        given(entityManager.createNativeQuery(anyString(), eq(Tag.class))).willReturn(query);
        given(query.setParameter(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).willReturn(query);
        given(query.getResultList()).willReturn(List.of(matched));

        var result = tagService.getAvailableTags(
                " Rock, Pop ", "Energetic", "Shorts, Tutorial", 60, 140);

        assertThat(result).singleElement().satisfies(tag -> {
            assertThat(tag.id()).isEqualTo(7L);
            assertThat(tag.type()).isEqualTo(TagType.USAGE);
        });
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture(), eq(Tag.class));
        assertThat(sql.getValue())
                .contains("tg1.type = 'GENRE'", "tg2.type = 'GENRE'")
                .contains("tg3.type = 'MOOD'")
                .contains("tg4.type = 'USAGE'", "tg5.type = 'USAGE'")
                .contains("tr.bpm >= ?", "tr.bpm <= ?");
        verify(query).setParameter(1, "Rock");
        verify(query).setParameter(2, "Pop");
        verify(query).setParameter(3, "Energetic");
        verify(query).setParameter(4, "Shorts");
        verify(query).setParameter(5, "Tutorial");
        verify(query).setParameter(6, 60);
        verify(query).setParameter(7, 140);
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
