package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.Answer;

import java.time.LocalDateTime;

public record AnswerResponse(
        Long id,
        String content,
        AnswerUserInfo user,
        LocalDateTime createdAt
) {
    public record AnswerUserInfo(
            Long id,
            String nickname,
            String role
    ) {}

    public static AnswerResponse from(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getContent(),
                new AnswerUserInfo(
                        answer.getUser().getId(),
                        answer.getUser().getNickname(),
                        answer.getUser().getRole().name()
                ),
                answer.getCreatedAt()
        );
    }
}
