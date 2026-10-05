package com.airouteviva.entity;

import com.airouteviva.entity.enums.AIRequestStatus;
import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.WorkloadType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_requests", indexes = {
    @Index(name = "idx_ai_req_request_id", columnList = "request_id", unique = true),
    @Index(name = "idx_ai_req_session_id", columnList = "session_id"),
    @Index(name = "idx_ai_req_status", columnList = "status"),
    @Index(name = "idx_ai_req_workload", columnList = "workload_type")
})
public class AIRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "workload_type", nullable = false, length = 64)
    private WorkloadType workloadType;

    @Enumerated(EnumType.STRING)
    @Column(name = "complexity", nullable = false, length = 32)
    private Complexity complexity;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AIRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public AIRequest() {
    }

    public AIRequest(String requestId, String sessionId, WorkloadType workloadType, Complexity complexity, Priority priority) {
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.workloadType = workloadType;
        this.complexity = complexity;
        this.priority = priority;
        this.status = AIRequestStatus.CREATED;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = AIRequestStatus.CREATED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public WorkloadType getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(WorkloadType workloadType) {
        this.workloadType = workloadType;
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public AIRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AIRequestStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
