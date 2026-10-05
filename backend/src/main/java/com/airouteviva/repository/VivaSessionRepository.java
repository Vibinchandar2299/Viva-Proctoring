package com.airouteviva.repository;

import com.airouteviva.entity.VivaSession;
import com.airouteviva.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VivaSessionRepository extends JpaRepository<VivaSession, Long> {
    Optional<VivaSession> findBySessionId(String sessionId);
    List<VivaSession> findByStudentId(String studentId);
    List<VivaSession> findByStatus(SessionStatus status);
    boolean existsBySessionId(String sessionId);
}
