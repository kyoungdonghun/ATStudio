package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.Question;
import com.atstudio.atstudio.entity.QuestionAttachment;
import com.atstudio.atstudio.entity.Answer;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(
        Long id,
        String title,
        String content,
        String category,
        Boolean isPublic,
        String status,
        QuestionUserInfo user,
        List<AttachmentResponse> attachments,
        List<AnswerResponse> answers,
        LocalDateTime createdAt
) {
    public record QuestionUserInfo(
            Long id,
            String nickname
    ) {}

    /**
     * Full detail (8.4 GET /api/questions/{id}) with answers and attachments.
     */
    public static QuestionResponse from(Question question,
                                         List<QuestionAttachment> attachments,
                                         List<Answer> answers) {
        return new QuestionResponse(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCategory().name(),
                question.isPublic(),
                question.getStatus().name(),
                new QuestionUserInfo(
                        question.getUser().getId(),
                        question.getUser().getNickname()
                ),
                attachments.stream().map(AttachmentResponse::from).toList(),
                answers.stream().map(AnswerResponse::from).toList(),
                question.getCreatedAt()
        );
    }

    /**
     * Create response (8.1 POST /api/questions) - no answers, no user block.
     */
    public static QuestionResponse fromCreate(Question question,
                                               List<QuestionAttachment> attachments) {
        return new QuestionResponse(
                question.getId(),
                question.getTitle(),
                null,
                question.getCategory().name(),
                question.isPublic(),
                question.getStatus().name(),
                null,
                attachments.stream().map(AttachmentResponse::from).toList(),
                null,
                question.getCreatedAt()
        );
    }

    /**
     * Status update response (8.6 PUT /api/questions/{id}/status).
     */
    public static QuestionResponse fromStatusUpdate(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getTitle(),
                null,
                question.getCategory().name(),
                question.isPublic(),
                question.getStatus().name(),
                null,
                null,
                null,
                question.getCreatedAt()
        );
    }
}
