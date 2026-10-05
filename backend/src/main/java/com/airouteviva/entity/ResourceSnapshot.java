package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resource_snapshots", indexes = {
    @Index(name = "idx_snapshot_request_id", columnList = "request_id"),
    @Index(name = "idx_snapshot_timestamp", columnList = "timestamp")
})
public class ResourceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "cpu_usage", nullable = false)
    private Double cpuUsage;

    @Column(name = "ram_usage", nullable = false)
    private Double ramUsage;

    @Column(name = "ram_available_mb", nullable = false)
    private Long ramAvailableMb;

    @Column(name = "gpu_available", nullable = false)
    private Boolean gpuAvailable;

    @Column(name = "gpu_usage")
    private Double gpuUsage;

    @Column(name = "network_status", nullable = false, length = 32)
    private String networkStatus;

    @Column(name = "network_latency_ms", nullable = false)
    private Double networkLatencyMs;

    @Column(name = "bandwidth_mbps")
    private Double bandwidthMbps;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public ResourceSnapshot() {
    }

    public ResourceSnapshot(String requestId, Double cpuUsage, Double ramUsage, Long ramAvailableMb, Boolean gpuAvailable, Double gpuUsage, String networkStatus, Double networkLatencyMs, Double bandwidthMbps, Instant timestamp) {
        this.requestId = requestId;
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
        this.ramAvailableMb = ramAvailableMb;
        this.gpuAvailable = gpuAvailable;
        this.gpuUsage = gpuUsage;
        this.networkStatus = networkStatus;
        this.networkLatencyMs = networkLatencyMs;
        this.bandwidthMbps = bandwidthMbps;
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

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public Double getNetworkLatencyMs() {
        return networkLatencyMs;
    }

    public void setNetworkLatencyMs(Double networkLatencyMs) {
        this.networkLatencyMs = networkLatencyMs;
    }

    public Double getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(Double bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
