package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.QuestionCategory;
import com.atstudio.atstudio.entity.enums.QuestionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionCategory category;

    @Builder.Default
    @Column(nullable = false)
    private boolean isPublic = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private QuestionStatus status = QuestionStatus.OPEN;

    public void updateStatus(QuestionStatus newStatus) {
        validateTransition(this.status, newStatus);
        this.status = newStatus;
    }

    private void validateTransition(QuestionStatus from, QuestionStatus to) {
        boolean valid = switch (from) {
            case OPEN -> to == QuestionStatus.IN_PROGRESS || to == QuestionStatus.CLOSED;
            case IN_PROGRESS -> to == QuestionStatus.RESOLVED || to == QuestionStatus.CLOSED;
            case RESOLVED -> to == QuestionStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!valid) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }
}
