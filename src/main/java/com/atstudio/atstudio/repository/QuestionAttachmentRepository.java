package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.QuestionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionAttachmentRepository extends JpaRepository<QuestionAttachment, Long> {

    List<QuestionAttachment> findAllByQuestionId(Long questionId);

    Optional<QuestionAttachment> findByIdAndQuestionId(Long id, Long questionId);
}
