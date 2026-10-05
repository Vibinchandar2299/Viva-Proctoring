package com.airouteviva.repository;

import com.airouteviva.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    Optional<Evidence> findByEvidenceId(String evidenceId);
    List<Evidence> findBySessionIdOrderByTimestampAsc(String sessionId);
    Optional<Evidence> findByEventId(String eventId);
}
