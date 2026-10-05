package com.airouteviva.repository;

import com.airouteviva.entity.AnswerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerEvaluationRepository extends JpaRepository<AnswerEvaluation, Long> {
    Optional<AnswerEvaluation> findByEvaluationId(String evaluationId);
    Optional<AnswerEvaluation> findByAnswerId(String answerId);
    List<AnswerEvaluation> findByAnswerIdIn(List<String> answerIds);
}
