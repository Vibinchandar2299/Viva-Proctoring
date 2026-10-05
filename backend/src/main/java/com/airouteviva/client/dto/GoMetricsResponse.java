package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoMetricsResponse {
    private Long totalRequests;
    private Long localRequests;
    private Long cacheHits;
    private Long fallbackRequests;
    private Long simulatedCloudRequests;
    private Long failedRequests;
    private Double averageLatency;
    private Double peakCPU;
    private Double peakRAM;
    private Long resourceAdaptationEvents;
    private Double averageEstimatedCost;
    private Double totalEstimatedCost;

    public GoMetricsResponse() {
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getLocalRequests() {
        return localRequests;
    }

    public void setLocalRequests(Long localRequests) {
        this.localRequests = localRequests;
    }

    public Long getCacheHits() {
        return cacheHits;
    }

    public void setCacheHits(Long cacheHits) {
        this.cacheHits = cacheHits;
    }

    public Long getFallbackRequests() {
        return fallbackRequests;
    }

    public void setFallbackRequests(Long fallbackRequests) {
        this.fallbackRequests = fallbackRequests;
    }

    public Long getSimulatedCloudRequests() {
        return simulatedCloudRequests;
    }

    public void setSimulatedCloudRequests(Long simulatedCloudRequests) {
        this.simulatedCloudRequests = simulatedCloudRequests;
    }

    public Long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(Long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public Double getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(Double averageLatency) {
        this.averageLatency = averageLatency;
    }

    public Double getPeakCPU() {
        return peakCPU;
    }

    public void setPeakCPU(Double peakCPU) {
        this.peakCPU = peakCPU;
    }

    public Double getPeakRAM() {
        return peakRAM;
    }

    public void setPeakRAM(Double peakRAM) {
        this.peakRAM = peakRAM;
    }

    public Long getResourceAdaptationEvents() {
        return resourceAdaptationEvents;
    }

    public void setResourceAdaptationEvents(Long resourceAdaptationEvents) {
        this.resourceAdaptationEvents = resourceAdaptationEvents;
    }

    public Double getAverageEstimatedCost() {
        return averageEstimatedCost;
    }

    public void setAverageEstimatedCost(Double averageEstimatedCost) {
        this.averageEstimatedCost = averageEstimatedCost;
    }

    public Double getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(Double totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }
}
