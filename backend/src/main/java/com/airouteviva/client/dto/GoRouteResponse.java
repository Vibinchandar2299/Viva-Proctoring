package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoRouteResponse {
    private String requestId;
    private String selectedPath;
    private String reasoning;
    private Double latencyMs;
    private Double estimatedCost;
    private GoResourceState resourceState;
    private Map<String, Double> scores;
    private Map<String, GoScoreBreakdown> scoreDetails;
    private List<String> feasiblePaths;
    private Map<String, String> infeasiblePaths;
    private Boolean cacheHit;
    private GoExecutionResult executionResult;
    private String timestamp;

    public GoRouteResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public void setSelectedPath(String selectedPath) {
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

    public GoResourceState getResourceState() {
        return resourceState;
    }

    public void setResourceState(GoResourceState resourceState) {
        this.resourceState = resourceState;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }

    public Map<String, GoScoreBreakdown> getScoreDetails() {
        return scoreDetails;
    }

    public void setScoreDetails(Map<String, GoScoreBreakdown> scoreDetails) {
        this.scoreDetails = scoreDetails;
    }

    public List<String> getFeasiblePaths() {
        return feasiblePaths;
    }

    public void setFeasiblePaths(List<String> feasiblePaths) {
        this.feasiblePaths = feasiblePaths;
    }

    public Map<String, String> getInfeasiblePaths() {
        return infeasiblePaths;
    }

    public void setInfeasiblePaths(Map<String, String> infeasiblePaths) {
        this.infeasiblePaths = infeasiblePaths;
    }

    public Boolean getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(Boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    public GoExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(GoExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
