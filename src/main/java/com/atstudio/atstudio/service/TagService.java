package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.TagNamePolicy;
import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.dto.tag.TagDeletionImpactResponse;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {
    @org.springframework.beans.factory.annotation.Autowired
    private ListeningCatalogService catalog;

    private java.util.List<Long> selectedIds() {
        if (catalog != null) return catalog.snapshot().trackIds();
        if (listening.getCatalogSource() == com.atstudio.atstudio.config.ListeningProperties.CatalogSource.DATABASE) {
            throw new IllegalStateException("Catalog service required in database mode");
        }
        return listening.getTrackIds().stream().sorted().toList();
    }


    // Initialized for standalone service fixtures; Spring always injects the bound policy.
    @org.springframework.beans.factory.annotation.Autowired
    private com.atstudio.atstudio.config.ListeningProperties listening =
            new com.atstudio.atstudio.config.ListeningProperties();

    private final TagRepository tagRepository;
    private final TrackTagRepository trackTagRepository;
    private final EntityManager em;

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        String canonicalName = canonicalizeAndValidate(request.getName());
        if (tagRepository.existsByName(canonicalName)) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
        }

        Tag tag = Tag.builder()
                .name(canonicalName)
                .type(request.getType())
                .build();

        try {
            return TagResponse.from(tagRepository.saveAndFlush(tag));
        } catch (DataIntegrityViolationException exception) {
            throw TagNameConstraintTranslator.translate(exception);
        }
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public List<TagResponse> getAllTags(TagType type) {
        if (listening.isEnabled()) {
            return getAvailableTags(null, null, null, null, null, null).stream()
                    .filter(tag -> type == null || tag.type() == type).toList();
        }
        return getAllTagsForAdmin(type);
    }

    public List<TagResponse> getAllTagsForAdmin(TagType type) {
        List<Tag> tags = (type != null)
                ? tagRepository.findAllByType(type)
                : tagRepository.findAll();
        return tags.stream().map(TagResponse::from).toList();
    }

    public TagDeletionImpactResponse getDeletionImpact(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND));
        long trackAssociationCount = trackTagRepository.countByTag(tag);
        return TagDeletionImpactResponse.from(tag, trackAssociationCount);
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public TagResponse updateTag(Long id, TagCreateRequest request) {
        guardTagTracks(id);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND));
        String canonicalName = canonicalizeAndValidate(request.getName());

        if (!tag.getName().equals(canonicalName) && tagRepository.existsByName(canonicalName)) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
        }

        tag.update(canonicalName, request.getType());
        try {
            tagRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw TagNameConstraintTranslator.translate(exception);
        }
        return TagResponse.from(tag);
    }

    private void guardTagTracks(Long tagId) {
        if (catalog == null || !catalog.databaseMode()) return;
        var ids = em.createQuery("select distinct tt.track.id from TrackTag tt where tt.tag.id = :id order by tt.track.id", Long.class)
                .setParameter("id", tagId).getResultList();
        for (Long id : ids) {
            catalog.requireUnselected(id);
            em.find(com.atstudio.atstudio.entity.Track.class, id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        }
    }

    private String canonicalizeAndValidate(String rawName) {
        if (!TagNamePolicy.isWithinRawLimit(rawName)) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_INVALID);
        }

        String canonicalName = TagNamePolicy.canonicalize(rawName);
        if (!TagNamePolicy.isValid(canonicalName)) {
            throw new BusinessException(BUSINESS_ERROR.TAG_NAME_INVALID);
        }
        return canonicalName;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public List<TagResponse> getAvailableTags(
            List<String> genre,
            List<String> mood,
            List<String> instrument,
            List<String> usage,
            Integer bpmMin,
            Integer bpmMax) {
        var selected = listening.isEnabled() ? selectedIds() : java.util.List.<Long>of();
        if (listening.isEnabled() && selected.isEmpty()) {
            return List.of();
        }
        List<String> genreNames = normalizeNames(genre);
        List<String> moodNames = normalizeNames(mood);
        List<String> instrumentNames = normalizeNames(instrument);
        List<String> usageNames = normalizeNames(usage);

        // Build dynamic native SQL with AND logic: track must have ALL selected tags
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT t.* FROM tags t
            JOIN track_tags tt ON tt.tag_id = t.id
            JOIN tracks tr ON tt.track_id = tr.id
            WHERE tr.is_active = true
            """);

        List<Object> params = new ArrayList<>();
        int paramIdx = 1;

        paramIdx = appendTagFilters(sql, params, paramIdx, genreNames, TagType.GENRE);
        paramIdx = appendTagFilters(sql, params, paramIdx, moodNames, TagType.MOOD);
        paramIdx = appendTagFilters(sql, params, paramIdx, instrumentNames, TagType.INSTRUMENT);
        appendTagFilters(sql, params, paramIdx, usageNames, TagType.USAGE);

        if (listening.isEnabled()) {
            sql.append(" AND tr.id IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(selected.size(), "?")));
            sql.append(")");
            params.addAll(selected);
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

    private List<String> normalizeNames(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(TagNamePolicy::canonicalize)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    private int appendTagFilters(
            StringBuilder sql,
            List<Object> params,
            int paramIdx,
            List<String> names,
            TagType tagType) {
        for (String name : names) {
            sql.append(" AND tr.id IN (SELECT tt").append(paramIdx).append(".track_id FROM track_tags tt")
                    .append(paramIdx).append(" JOIN tags tg").append(paramIdx).append(" ON tg").append(paramIdx)
                    .append(".id = tt").append(paramIdx).append(".tag_id WHERE tg").append(paramIdx)
                    .append(".type = ? AND tg").append(paramIdx).append(".name = ?)");
            params.add(tagType.name());
            params.add(name);
            paramIdx++;
        }
        return paramIdx;
    }

    @Transactional
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public void deleteTag(Long id) {
        guardTagTracks(id);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TAG_NOT_FOUND));
        trackTagRepository.deleteAllByTag(tag);
        tagRepository.delete(tag);
    }
}
