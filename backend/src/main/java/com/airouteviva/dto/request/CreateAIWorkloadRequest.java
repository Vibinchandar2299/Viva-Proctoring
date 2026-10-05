package com.airouteviva.dto.request;

import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.WorkloadType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAIWorkloadRequest {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotNull(message = "workloadType is required")
    private WorkloadType workloadType;

    private Complexity complexity;

    private Priority priority;

    private Object payload;

    public CreateAIWorkloadRequest() {
    }

    public CreateAIWorkloadRequest(String sessionId, WorkloadType workloadType, Complexity complexity, Priority priority, Object payload) {
        this.sessionId = sessionId;
        this.workloadType = workloadType;
        this.complexity = complexity;
        this.priority = priority;
        this.payload = payload;
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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
