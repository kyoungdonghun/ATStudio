package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .type(request.getType())
                .build();

        return TagResponse.from(tagRepository.save(tag));
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::from)
                .toList();
    }
}
