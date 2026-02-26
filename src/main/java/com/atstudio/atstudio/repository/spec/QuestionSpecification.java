package com.atstudio.atstudio.repository.spec;

import com.atstudio.atstudio.entity.Question;
import com.atstudio.atstudio.entity.enums.QuestionCategory;
import com.atstudio.atstudio.entity.enums.QuestionStatus;
import org.springframework.data.jpa.domain.Specification;

public class QuestionSpecification {

    private QuestionSpecification() {}

    public static Specification<Question> visibleTo(Long userId, boolean isAdmin) {
        if (isAdmin) return null;
        return (root, query, cb) -> cb.or(
                cb.isTrue(root.get("isPublic")),
                cb.equal(root.get("user").get("id"), userId)
        );
    }

    public static Specification<Question> hasCategory(QuestionCategory category) {
        if (category == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Question> hasStatus(QuestionStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Question> belongsToUser(Long userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
}
