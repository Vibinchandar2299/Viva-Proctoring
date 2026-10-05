package com.airouteviva.repository;

import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.enums.AIRequestStatus;
import com.airouteviva.entity.enums.WorkloadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIRequestRepository extends JpaRepository<AIRequest, Long> {
    Optional<AIRequest> findByRequestId(String requestId);
    List<AIRequest> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<AIRequest> findBySessionIdAndWorkloadType(String sessionId, WorkloadType workloadType);
    long countBySessionId(String sessionId);
    long countBySessionIdAndStatus(String sessionId, AIRequestStatus status);
}
