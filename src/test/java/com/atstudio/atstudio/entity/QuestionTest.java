package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.QuestionCategory;
import com.atstudio.atstudio.entity.enums.QuestionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Question 상태 전이 검증")
class QuestionTest {

    @Nested
    @DisplayName("유효한 상태 전이")
    class ValidTransitions {

        @Test
        @DisplayName("OPEN -> IN_PROGRESS 성공")
        void openToInProgress() {
            Question question = buildQuestion(QuestionStatus.OPEN);

            question.updateStatus(QuestionStatus.IN_PROGRESS);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("OPEN -> CLOSED 성공")
        void openToClosed() {
            Question question = buildQuestion(QuestionStatus.OPEN);

            question.updateStatus(QuestionStatus.CLOSED);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.CLOSED);
        }

        @Test
        @DisplayName("IN_PROGRESS -> RESOLVED 성공")
        void inProgressToResolved() {
            Question question = buildQuestion(QuestionStatus.IN_PROGRESS);

            question.updateStatus(QuestionStatus.RESOLVED);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
        }

        @Test
        @DisplayName("IN_PROGRESS -> CLOSED 성공")
        void inProgressToClosed() {
            Question question = buildQuestion(QuestionStatus.IN_PROGRESS);

            question.updateStatus(QuestionStatus.CLOSED);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.CLOSED);
        }

        @Test
        @DisplayName("RESOLVED -> CLOSED 성공")
        void resolvedToClosed() {
            Question question = buildQuestion(QuestionStatus.RESOLVED);

            question.updateStatus(QuestionStatus.CLOSED);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("유효하지 않은 상태 전이")
    class InvalidTransitions {

        @Test
        @DisplayName("CLOSED -> OPEN 실패 - INVALID_STATE_TRANSITION")
        void closedToOpen() {
            Question question = buildQuestion(QuestionStatus.CLOSED);

            assertThatThrownBy(() -> question.updateStatus(QuestionStatus.OPEN))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @Test
        @DisplayName("CLOSED -> IN_PROGRESS 실패 - INVALID_STATE_TRANSITION")
        void closedToInProgress() {
            Question question = buildQuestion(QuestionStatus.CLOSED);

            assertThatThrownBy(() -> question.updateStatus(QuestionStatus.IN_PROGRESS))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @Test
        @DisplayName("RESOLVED -> OPEN 실패 - INVALID_STATE_TRANSITION")
        void resolvedToOpen() {
            Question question = buildQuestion(QuestionStatus.RESOLVED);

            assertThatThrownBy(() -> question.updateStatus(QuestionStatus.OPEN))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @Test
        @DisplayName("OPEN -> RESOLVED 실패 - RESOLVED는 IN_PROGRESS 이후에만")
        void openToResolved() {
            Question question = buildQuestion(QuestionStatus.OPEN);

            assertThatThrownBy(() -> question.updateStatus(QuestionStatus.RESOLVED))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @Test
        @DisplayName("IN_PROGRESS -> OPEN 실패 - 역방향 전이 금지")
        void inProgressToOpen() {
            Question question = buildQuestion(QuestionStatus.IN_PROGRESS);

            assertThatThrownBy(() -> question.updateStatus(QuestionStatus.OPEN))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }
    }

    private Question buildQuestion(QuestionStatus status) {
        return Question.builder()
                .title("테스트 문의")
                .content("테스트 내용")
                .category(QuestionCategory.OTHER)
                .status(status)
                .build();
    }
}
