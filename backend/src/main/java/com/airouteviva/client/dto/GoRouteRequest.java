package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoRouteRequest {
    private String requestId;
    private String workloadType;
    private String complexity;
    private String priority;
    private Object payload;
    private Boolean execute;

    public GoRouteRequest() {
    }

    public GoRouteRequest(String requestId, String workloadType, String complexity, String priority, Object payload, Boolean execute) {
        this.requestId = requestId;
        this.workloadType = workloadType;
        this.complexity = complexity;
        this.priority = priority;
        this.payload = payload;
        this.execute = execute;
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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }
}
