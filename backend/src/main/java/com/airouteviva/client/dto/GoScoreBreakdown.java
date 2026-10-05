package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoScoreBreakdown {
    private Double totalScore;
    private Double resourceScore;
    private Double latencyScore;
    private Double costScore;
    private Double capabilityScore;
    private Double priorityFitScore;

    public GoScoreBreakdown() {
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getResourceScore() {
        return resourceScore;
    }

    public void setResourceScore(Double resourceScore) {
        this.resourceScore = resourceScore;
    }

    public Double getLatencyScore() {
        return latencyScore;
    }

    public void setLatencyScore(Double latencyScore) {
        this.latencyScore = latencyScore;
    }

    public Double getCostScore() {
        return costScore;
    }

    public void setCostScore(Double costScore) {
        this.costScore = costScore;
    }

    public Double getCapabilityScore() {
        return capabilityScore;
    }

    public void setCapabilityScore(Double capabilityScore) {
        this.capabilityScore = capabilityScore;
    }

    public Double getPriorityFitScore() {
        return priorityFitScore;
    }

    public void setPriorityFitScore(Double priorityFitScore) {
        this.priorityFitScore = priorityFitScore;
    }
}
