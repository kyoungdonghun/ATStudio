package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Answer;
import com.atstudio.atstudio.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @EntityGraph(attributePaths = "user")
    List<Answer> findAllByQuestionOrderByCreatedAtAsc(Question question);

    boolean existsByQuestionAndUser_Role(Question question,
                                         com.atstudio.atstudio.entity.enums.UserRole role);
}
