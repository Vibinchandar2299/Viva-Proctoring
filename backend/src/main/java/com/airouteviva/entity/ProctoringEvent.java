package com.airouteviva.entity;

import com.airouteviva.entity.enums.EventSeverity;
import com.airouteviva.entity.enums.ProctoringEventType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "proctoring_events", indexes = {
    @Index(name = "idx_event_event_id", columnList = "event_id", unique = true),
    @Index(name = "idx_event_session_id", columnList = "session_id"),
    @Index(name = "idx_event_timestamp", columnList = "timestamp")
})
public class ProctoringEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private ProctoringEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private EventSeverity severity;

    @Column(name = "description", nullable = false, length = 1024)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "evidence_id", length = 64)
    private String evidenceId;

    public ProctoringEvent() {
    }

    public ProctoringEvent(String eventId, String sessionId, ProctoringEventType eventType, EventSeverity severity, String description, String evidenceId) {
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.severity = severity != null ? severity : EventSeverity.INFO;
        this.description = description;
        this.evidenceId = evidenceId;
        this.timestamp = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
        if (this.severity == null) {
            this.severity = EventSeverity.INFO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ProctoringEventType getEventType() {
        return eventType;
    }

    public void setEventType(ProctoringEventType eventType) {
        this.eventType = eventType;
    }

    public EventSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }
}
