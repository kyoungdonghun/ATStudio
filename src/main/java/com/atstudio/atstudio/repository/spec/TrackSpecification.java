package com.atstudio.atstudio.repository.spec;

import com.atstudio.atstudio.entity.Track;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import java.text.Normalizer;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class TrackSpecification {

    private TrackSpecification() {}

    public static Specification<Track> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Track> hasIsActive(Boolean isActive) {
        if (isActive == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<Track> titleContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String normalized = Normalizer.normalize(keyword.toLowerCase(), Normalizer.Form.NFC);
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + normalized + "%");
    }

    public static Specification<Track> hasBpmMin(Integer bpmMin) {
        if (bpmMin == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("bpm"), bpmMin);
    }

    public static Specification<Track> hasBpmMax(Integer bpmMax) {
        if (bpmMax == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("bpm"), bpmMax);
    }

    public static Specification<Track> hasTonality(String tonality) {
        if (tonality == null || tonality.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("tonality"), tonality);
    }

    public static Specification<Track> hasTagWithNameAndType(String tagName, String tagType) {
        if (tagName == null || tagName.isBlank()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> trackTags = root.join("trackTags", JoinType.INNER);
            Join<Object, Object> tag = trackTags.join("tag", JoinType.INNER);
            return cb.and(
                    cb.equal(tag.get("name"), tagName),
                    cb.equal(tag.get("type").as(String.class), tagType)
            );
        };
    }

    /**
     * AND logic: track must have ALL of the given tag names (not just any one).
     * Each name becomes a separate EXISTS subquery.
     */
    public static Specification<Track> hasAllTagsWithType(List<String> names, String tagType) {
        if (names == null || names.isEmpty()) return null;
        // Chain: for each name, add a spec that requires that tag
        Specification<Track> combined = null;
        for (String name : names) {
            Specification<Track> single = hasTagWithNameAndType(name, tagType);
            combined = (combined == null) ? single : combined.and(single);
        }
        return combined;
    }
}
