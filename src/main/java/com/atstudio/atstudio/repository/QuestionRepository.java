package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
