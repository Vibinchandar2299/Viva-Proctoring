package com.airouteviva.repository;

import com.airouteviva.entity.VivaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VivaQuestionRepository extends JpaRepository<VivaQuestion, Long> {
    Optional<VivaQuestion> findByQuestionId(String questionId);
    List<VivaQuestion> findBySessionIdOrderByOrderNumberAsc(String sessionId);
    long countBySessionId(String sessionId);
}
