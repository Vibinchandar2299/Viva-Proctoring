package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evidence_records", indexes = {
    @Index(name = "idx_evidence_evidence_id", columnList = "evidence_id", unique = true),
    @Index(name = "idx_evidence_session_id", columnList = "session_id"),
    @Index(name = "idx_evidence_event_id", columnList = "event_id")
})
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_id", nullable = false, unique = true, length = 64)
    private String evidenceId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "file_type", nullable = false, length = 64)
    private String fileType; // image/jpeg, audio/wav, video/mp4

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Evidence() {
    }

    public Evidence(String evidenceId, String sessionId, String eventId, String fileName, String filePath, String fileType, Instant timestamp) {
        this.evidenceId = evidenceId;
        this.sessionId = sessionId;
        this.eventId = eventId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
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

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
