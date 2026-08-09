package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 단위 테스트")
class TagServiceTest {

    @Mock TagRepository tagRepository;
    @Mock TrackTagRepository trackTagRepository;
    @Mock EntityManager entityManager;

    @InjectMocks TagService tagService;

    // ── createTag() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTag() 성공 - 새 태그 저장 후 TagResponse 반환")
    void createTag_success() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("  \u1100\u1161\u2003\u2003Beat  ");
        request.setType(TagType.GENRE);

        given(tagRepository.existsByName("가 Beat")).willReturn(false);
        given(tagRepository.saveAndFlush(any(Tag.class))).willAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            ReflectionTestUtils.setField(tag, "id", 1L);
            return tag;
        });

        TagResponse response = tagService.createTag(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("가 Beat");
        assertThat(response.type()).isEqualTo(TagType.GENRE);
        ArgumentCaptor<Tag> savedTag = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).saveAndFlush(savedTag.capture());
        assertThat(savedTag.getValue().getName()).isEqualTo("가 Beat");
    }

    @Test
    @DisplayName("createTag() 실패 - 이름 중복 → TAG_NAME_DUPLICATED 예외")
    void createTag_fail_duplicateName() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("  Hip\u2003\u2003Hop  ");
        request.setType(TagType.GENRE);

        given(tagRepository.existsByName("Hip Hop")).willReturn(true);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED));
        verify(tagRepository, never()).saveAndFlush(any(Tag.class));
    }

    @Test
    @DisplayName("createTag() 실패 - raw 이름 200 code point 초과 → TAG_NAME_INVALID 예외")
    void createTag_fail_rawNameOverflow() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("가".repeat(201));
        request.setType(TagType.GENRE);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NAME_INVALID));
        verify(tagRepository, never()).existsByName(any());
        verify(tagRepository, never()).saveAndFlush(any(Tag.class));
    }

    @Test
    @DisplayName("createTag() DB 경합 - MySQL uq_tags_name → TAG_NAME_DUPLICATED 예외")
    void createTag_mysqlUniqueRaceTranslated() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Hip Hop");
        request.setType(TagType.GENRE);
        DataIntegrityViolationException race = mysqlTagNameViolation();
        given(tagRepository.existsByName("Hip Hop")).willReturn(false);
        given(tagRepository.saveAndFlush(any(Tag.class))).willThrow(race);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(BusinessException.class)
                .hasCause(race)
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

    // ── updateTag() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTag() 성공 - 태그 이름/타입 수정 후 응답 반환")
    void updateTag_success() {
        Tag tag = buildTag(1L, "OldName", TagType.GENRE);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("  \u1100\u1161\u2003\u2003Beat  ");
        request.setType(TagType.MOOD);

        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));
        given(tagRepository.existsByName("가 Beat")).willReturn(false);

        TagResponse response = tagService.updateTag(1L, request);

        assertThat(response.name()).isEqualTo("가 Beat");
        assertThat(response.type()).isEqualTo(TagType.MOOD);
        assertThat(tag.getName()).isEqualTo("가 Beat");
        verify(tagRepository).flush();
    }

    @Test
    @DisplayName("updateTag() 실패 - 존재하지 않는 ID → TAG_NOT_FOUND 예외")
    void updateTag_notFound() {
        given(tagRepository.findById(99L)).willReturn(Optional.empty());
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Any");
        request.setType(TagType.MOOD);

        assertThatThrownBy(() -> tagService.updateTag(99L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NOT_FOUND));
    }

    @Test
    @DisplayName("updateTag() 실패 - 다른 태그와 이름 중복 → TAG_NAME_DUPLICATED 예외")
    void updateTag_duplicateName() {
        Tag tag = buildTag(1L, "Original", TagType.GENRE);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("  Hip\u2003\u2003Hop  ");
        request.setType(TagType.GENRE);

        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));
        given(tagRepository.existsByName("Hip Hop")).willReturn(true);

        assertThatThrownBy(() -> tagService.updateTag(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED));
    }

    @Test
    @DisplayName("updateTag() 성공 - 정규화 후 자기 이름이 같으면 중복 조회를 생략")
    void updateTag_selfNameAfterNormalization() {
        Tag tag = buildTag(1L, "Hip Hop", TagType.GENRE);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("\u2003Hip  Hop\u2003");
        request.setType(TagType.MOOD);
        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));

        TagResponse response = tagService.updateTag(1L, request);

        assertThat(response.name()).isEqualTo("Hip Hop");
        assertThat(response.type()).isEqualTo(TagType.MOOD);
        verify(tagRepository, never()).existsByName(any());
        verify(tagRepository).flush();
    }

    @Test
    @DisplayName("updateTag() DB 경합 - H2 shaped tags(name) 제약 → TAG_NAME_DUPLICATED 예외")
    void updateTag_h2UniqueRaceTranslated() {
        Tag tag = buildTag(1L, "Original", TagType.GENRE);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Hip Hop");
        request.setType(TagType.GENRE);
        DataIntegrityViolationException race = h2TagNameViolation();
        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));
        given(tagRepository.existsByName("Hip Hop")).willReturn(false);
        org.mockito.Mockito.doThrow(race).when(tagRepository).flush();

        assertThatThrownBy(() -> tagService.updateTag(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasCause(race)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED));
    }

    // ── deleteTag() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTag() 성공 - TrackTag 먼저 삭제 후 태그 삭제")
    void deleteTag_success() {
        Tag tag = buildTag(1L, "Happy", TagType.MOOD);
        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));

        tagService.deleteTag(1L);

        verify(trackTagRepository).deleteAllByTag(tag);
        verify(tagRepository).delete(tag);
    }

    @Test
    @DisplayName("deleteTag() 실패 - 존재하지 않는 ID → TAG_NOT_FOUND 예외")
    void deleteTag_notFound() {
        given(tagRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTag(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TAG_NOT_FOUND));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private Tag buildTag(Long id, String name, TagType type) {
        Tag tag = Tag.builder().name(name).type(type).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    private DataIntegrityViolationException mysqlTagNameViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry 'Hip Hop' for key 'tags.uq_tags_name'",
                        "23000",
                        1062));
    }

    private DataIntegrityViolationException h2TagNameViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException(
                        "Unique index or primary key violation: "
                                + "\"PUBLIC.UQ_TAGS_NAME ON PUBLIC.TAGS(NAME NULLS FIRST)\"",
                        "23505",
                        23505));
    }
}
