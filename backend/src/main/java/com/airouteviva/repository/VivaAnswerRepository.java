package com.airouteviva.repository;

import com.airouteviva.entity.VivaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VivaAnswerRepository extends JpaRepository<VivaAnswer, Long> {
    Optional<VivaAnswer> findByAnswerId(String answerId);
    Optional<VivaAnswer> findByQuestionId(String questionId);
    List<VivaAnswer> findByQuestionIdIn(List<String> questionIds);
}
