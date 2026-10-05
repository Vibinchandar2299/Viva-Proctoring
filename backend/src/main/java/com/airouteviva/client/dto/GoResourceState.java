package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoResourceState {
    private Double cpuUsage;
    private Double ramUsage;
    private Long ramTotalMb;
    private Long ramAvailableMb;
    private Boolean gpuAvailable;
    private Double gpuUsage;
    private Long gpuMemoryMb;
    private String networkStatus;
    private Double latencyMs;
    private Double bandwidthKbps;
    private String cpuPressure;
    private String ramPressure;
    private Boolean isSimulated;
    private String timestamp;

    public GoResourceState() {
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

    public Long getRamTotalMb() {
        return ramTotalMb;
    }

    public void setRamTotalMb(Long ramTotalMb) {
        this.ramTotalMb = ramTotalMb;
    }

    public Long getRamAvailableMb() {
        return ramAvailableMb;
    }

    public void setRamAvailableMb(Long ramAvailableMb) {
        this.ramAvailableMb = ramAvailableMb;
    }

    public Boolean getGpuAvailable() {
        return gpuAvailable;
    }

    public void setGpuAvailable(Boolean gpuAvailable) {
        this.gpuAvailable = gpuAvailable;
    }

    public Double getGpuUsage() {
        return gpuUsage;
    }

    public void setGpuUsage(Double gpuUsage) {
        this.gpuUsage = gpuUsage;
    }

    public Long getGpuMemoryMb() {
        return gpuMemoryMb;
    }

    public void setGpuMemoryMb(Long gpuMemoryMb) {
        this.gpuMemoryMb = gpuMemoryMb;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getBandwidthKbps() {
        return bandwidthKbps;
    }

    public void setBandwidthKbps(Double bandwidthKbps) {
        this.bandwidthKbps = bandwidthKbps;
    }

    public String getCpuPressure() {
        return cpuPressure;
    }

    public void setCpuPressure(String cpuPressure) {
        this.cpuPressure = cpuPressure;
    }

    public String getRamPressure() {
        return ramPressure;
    }

    public void setRamPressure(String ramPressure) {
        this.ramPressure = ramPressure;
    }

    public Boolean getIsSimulated() {
        return isSimulated;
    }

    public void setIsSimulated(Boolean isSimulated) {
        this.isSimulated = isSimulated;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
