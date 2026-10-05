package com.airouteviva.dto.response;

import com.airouteviva.entity.enums.ExecutionPath;
import java.time.Instant;

public class SessionRoutingRecordResponse {
    private String requestId;
    private String workloadType;
    private String complexity;
    private String priority;
    private ExecutionPath selectedPath;
    private String reasoning;
    private Double latencyMs;
    private Double estimatedCost;
    private Boolean cacheHit;
    private Double cpuUsage;
    private Double ramUsage;
    private String networkStatus;
    private Instant timestamp;

    public SessionRoutingRecordResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(String workloadType) {
        this.workloadType = workloadType;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getRamUsage() {
        return ramUsage;
    }

    public void setRamUsage(Double ramUsage) {
        this.ramUsage = ramUsage;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
