package com.airouteviva.repository;

import com.airouteviva.entity.ProctoringEvent;
import com.airouteviva.entity.enums.ProctoringEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProctoringEventRepository extends JpaRepository<ProctoringEvent, Long> {
    Optional<ProctoringEvent> findByEventId(String eventId);
    List<ProctoringEvent> findBySessionIdOrderByTimestampAsc(String sessionId);
    long countBySessionId(String sessionId);
    long countBySessionIdAndEventTypeNot(String sessionId, ProctoringEventType normal);
}
