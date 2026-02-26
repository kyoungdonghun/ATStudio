package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.Question;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionListItemResponse(
        Long id,
        String title,
        String category,
        Boolean isPublic,
        String status,
        LocalDateTime createdAt
) {
    public static QuestionListItemResponse from(Question question) {
        return new QuestionListItemResponse(
                question.getId(),
                question.getTitle(),
                question.getCategory().name(),
                question.isPublic(),
                question.getStatus().name(),
                question.getCreatedAt()
        );
    }
}
