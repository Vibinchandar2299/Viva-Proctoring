package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "viva_reports", indexes = {
    @Index(name = "idx_report_report_id", columnList = "report_id", unique = true),
    @Index(name = "idx_report_session_id", columnList = "session_id", unique = true)
})
public class VivaReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false, unique = true, length = 64)
    private String reportId;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "summary", length = 4096)
    private String summary;

    @Column(name = "proctoring_summary", length = 4096)
    private String proctoringSummary;

    @Column(name = "routing_summary", length = 4096)
    private String routingSummary;

    @Column(name = "score_average")
    private Double scoreAverage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VivaReport() {
    }

    public VivaReport(String reportId, String sessionId, String summary, String proctoringSummary, String routingSummary, Double scoreAverage) {
        this.reportId = reportId;
        this.sessionId = sessionId;
        this.summary = summary;
        this.proctoringSummary = proctoringSummary;
        this.routingSummary = routingSummary;
        this.scoreAverage = scoreAverage;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getProctoringSummary() {
        return proctoringSummary;
    }

    public void setProctoringSummary(String proctoringSummary) {
        this.proctoringSummary = proctoringSummary;
    }

    public String getRoutingSummary() {
        return routingSummary;
    }

    public void setRoutingSummary(String routingSummary) {
        this.routingSummary = routingSummary;
    }

    public Double getScoreAverage() {
        return scoreAverage;
    }

    public void setScoreAverage(Double scoreAverage) {
        this.scoreAverage = scoreAverage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
