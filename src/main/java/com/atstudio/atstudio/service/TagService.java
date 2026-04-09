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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TrackTagRepository trackTagRepository;
    private final EntityManager em;

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

    public List<TagResponse> getAllTags(TagType type) {
        List<Tag> tags = (type != null)
                ? tagRepository.findAllByType(type)
                : tagRepository.findAll();
        return tags.stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse updateTag(Long id, TagCreateRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND));

        if (!tag.getName().equals(request.getName()) && tagRepository.existsByName(request.getName())) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
        }

        tag.update(request.getName(), request.getType());
        return TagResponse.from(tag);
    }

    @SuppressWarnings("unchecked")
    public List<TagResponse> getAvailableTags(String genre, String mood, Integer bpmMin, Integer bpmMax) {
        List<String> genreNames = splitCsv(genre);
        List<String> moodNames = splitCsv(mood);

        // Build dynamic native SQL with AND logic: track must have ALL selected tags
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT t.* FROM tags t
            JOIN track_tags tt ON tt.tag_id = t.id
            JOIN tracks tr ON tt.track_id = tr.id
            WHERE tr.is_active = true
            """);

        List<Object> params = new ArrayList<>();
        int paramIdx = 1;

        // Each genre name = separate EXISTS (AND)
        for (String g : genreNames) {
            sql.append(" AND tr.id IN (SELECT tt").append(paramIdx).append(".track_id FROM track_tags tt")
               .append(paramIdx).append(" JOIN tags tg").append(paramIdx).append(" ON tg").append(paramIdx)
               .append(".id = tt").append(paramIdx).append(".tag_id WHERE tg").append(paramIdx)
               .append(".type = 'GENRE' AND tg").append(paramIdx).append(".name = ?)");
            params.add(g);
            paramIdx++;
        }

        // Each mood name = separate EXISTS (AND)
        for (String m : moodNames) {
            sql.append(" AND tr.id IN (SELECT tt").append(paramIdx).append(".track_id FROM track_tags tt")
               .append(paramIdx).append(" JOIN tags tg").append(paramIdx).append(" ON tg").append(paramIdx)
               .append(".id = tt").append(paramIdx).append(".tag_id WHERE tg").append(paramIdx)
               .append(".type = 'MOOD' AND tg").append(paramIdx).append(".name = ?)");
            params.add(m);
            paramIdx++;
        }

        if (bpmMin != null) {
            sql.append(" AND tr.bpm >= ?");
            params.add(bpmMin);
        }
        if (bpmMax != null) {
            sql.append(" AND tr.bpm <= ?");
            params.add(bpmMax);
        }

        var query = em.createNativeQuery(sql.toString(), Tag.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        List<Tag> tags = query.getResultList();
        return tags.stream().map(TagResponse::from).toList();
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND));
        trackTagRepository.deleteAllByTag(tag);
        tagRepository.delete(tag);
    }
}
