package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 단위 테스트")
class TagServiceTest {

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    TagService tagService;

    // ── createTag() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTag() 성공 - 새 태그 저장 후 TagResponse 반환")
    void createTag_success() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Lo-Fi");
        request.setType(TagType.GENRE);

        Tag saved = buildTag(1L, "Lo-Fi", TagType.GENRE);
        given(tagRepository.existsByName("Lo-Fi")).willReturn(false);
        given(tagRepository.save(any(Tag.class))).willReturn(saved);

        TagResponse response = tagService.createTag(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Lo-Fi");
        assertThat(response.type()).isEqualTo(TagType.GENRE);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("createTag() 실패 - 이름 중복 → TAG_NAME_DUPLICATED 예외")
    void createTag_fail_duplicateName() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Pop");
        request.setType(TagType.GENRE);

        given(tagRepository.existsByName("Pop")).willReturn(true);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED));
    }

    // ── getAllTags() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTags(null) - type 필터 없이 전체 태그 반환")
    void getAllTags_noTypeFilter() {
        Tag tag1 = buildTag(1L, "Happy", TagType.MOOD);
        Tag tag2 = buildTag(2L, "Pop", TagType.GENRE);
        given(tagRepository.findAll()).willReturn(List.of(tag1, tag2));

        List<TagResponse> result = tagService.getAllTags(null);

        assertThat(result).hasSize(2);
        verify(tagRepository).findAll();
    }

    @Test
    @DisplayName("getAllTags(GENRE) - type 필터로 해당 타입만 반환")
    void getAllTags_withTypeFilter() {
        Tag tag = buildTag(2L, "Pop", TagType.GENRE);
        given(tagRepository.findAllByType(TagType.GENRE)).willReturn(List.of(tag));

        List<TagResponse> result = tagService.getAllTags(TagType.GENRE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(TagType.GENRE);
        verify(tagRepository).findAllByType(TagType.GENRE);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private Tag buildTag(Long id, String name, TagType type) {
        Tag tag = Tag.builder().name(name).type(type).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }
}
