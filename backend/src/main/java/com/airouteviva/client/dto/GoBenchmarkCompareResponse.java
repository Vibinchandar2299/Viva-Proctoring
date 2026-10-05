package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoBenchmarkCompareResponse {
    private String workloadType;
    private String complexity;
    private String priority;
    private String simulatedCondition;
    private String aiRouteSelectedPath;
    private Double aiRouteLatencyMs;
    private Double aiRouteCost;
    private Boolean aiRouteSuccess;
    private String aiRouteReasoning;
    private String baselineFixedPath;
    private Double baselineLatencyMs;
    private Double baselineCost;
    private Boolean baselineSuccess;
    private String baselineFailureReason;
    private Double latencyDeltaMs;
    private Double costDelta;
    private Boolean adaptationObserved;
    private String analysis;

    public GoBenchmarkCompareResponse() {
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

    public String getSimulatedCondition() {
        return simulatedCondition;
    }

    public void setSimulatedCondition(String simulatedCondition) {
        this.simulatedCondition = simulatedCondition;
    }

    public String getAiRouteSelectedPath() {
        return aiRouteSelectedPath;
    }

    public void setAiRouteSelectedPath(String aiRouteSelectedPath) {
        this.aiRouteSelectedPath = aiRouteSelectedPath;
    }

    public Double getAiRouteLatencyMs() {
        return aiRouteLatencyMs;
    }

    public void setAiRouteLatencyMs(Double aiRouteLatencyMs) {
        this.aiRouteLatencyMs = aiRouteLatencyMs;
    }

    public Double getAiRouteCost() {
        return aiRouteCost;
    }

    public void setAiRouteCost(Double aiRouteCost) {
        this.aiRouteCost = aiRouteCost;
    }

    public Boolean getAiRouteSuccess() {
        return aiRouteSuccess;
    }

    public void setAiRouteSuccess(Boolean aiRouteSuccess) {
        this.aiRouteSuccess = aiRouteSuccess;
    }

    public String getAiRouteReasoning() {
        return aiRouteReasoning;
    }

    public void setAiRouteReasoning(String aiRouteReasoning) {
        this.aiRouteReasoning = aiRouteReasoning;
    }

    public String getBaselineFixedPath() {
        return baselineFixedPath;
    }

    public void setBaselineFixedPath(String baselineFixedPath) {
        this.baselineFixedPath = baselineFixedPath;
    }

    public Double getBaselineLatencyMs() {
        return baselineLatencyMs;
    }

    public void setBaselineLatencyMs(Double baselineLatencyMs) {
        this.baselineLatencyMs = baselineLatencyMs;
    }

    public Double getBaselineCost() {
        return baselineCost;
    }

    public void setBaselineCost(Double baselineCost) {
        this.baselineCost = baselineCost;
    }

    public Boolean getBaselineSuccess() {
        return baselineSuccess;
    }

    public void setBaselineSuccess(Boolean baselineSuccess) {
        this.baselineSuccess = baselineSuccess;
    }

    public String getBaselineFailureReason() {
        return baselineFailureReason;
    }

    public void setBaselineFailureReason(String baselineFailureReason) {
        this.baselineFailureReason = baselineFailureReason;
    }

    public Double getLatencyDeltaMs() {
        return latencyDeltaMs;
    }

    public void setLatencyDeltaMs(Double latencyDeltaMs) {
        this.latencyDeltaMs = latencyDeltaMs;
    }

    public Double getCostDelta() {
        return costDelta;
    }

    public void setCostDelta(Double costDelta) {
        this.costDelta = costDelta;
    }

    public Boolean getAdaptationObserved() {
        return adaptationObserved;
    }

    public void setAdaptationObserved(Boolean adaptationObserved) {
        this.adaptationObserved = adaptationObserved;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
