package com.airouteviva.entity;

import com.airouteviva.entity.enums.ExecutionPath;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "routing_decisions", indexes = {
    @Index(name = "idx_routing_request_id", columnList = "request_id", unique = true),
    @Index(name = "idx_routing_selected_path", columnList = "selected_path"),
    @Index(name = "idx_routing_timestamp", columnList = "timestamp")
})
public class RoutingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_path", nullable = false, length = 64)
    private ExecutionPath selectedPath;

    @Column(name = "reasoning", nullable = false, length = 2048)
    private String reasoning;

    @Column(name = "latency_ms", nullable = false)
    private Double latencyMs;

    @Column(name = "estimated_cost", nullable = false)
    private Double estimatedCost;

    @Column(name = "cache_hit")
    private Boolean cacheHit;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public RoutingDecision() {
    }

    public RoutingDecision(String requestId, ExecutionPath selectedPath, String reasoning, Double latencyMs, Double estimatedCost, Boolean cacheHit, Instant timestamp) {
        this.requestId = requestId;
        this.selectedPath = selectedPath;
        this.reasoning = reasoning;
        this.latencyMs = latencyMs;
        this.estimatedCost = estimatedCost;
        this.cacheHit = cacheHit;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
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

    public ExecutionPath getSelectedPath() {
        return selectedPath;
    }

    public void setSelectedPath(ExecutionPath selectedPath) {
        this.selectedPath = selectedPath;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Boolean getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(Boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
