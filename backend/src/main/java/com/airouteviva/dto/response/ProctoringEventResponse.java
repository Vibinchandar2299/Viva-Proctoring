package com.airouteviva.dto.response;

import com.airouteviva.entity.enums.EventSeverity;
import com.airouteviva.entity.enums.ProctoringEventType;
import java.time.Instant;

public class ProctoringEventResponse {
    private String eventId;
    private String sessionId;
    private ProctoringEventType eventType;
    private EventSeverity severity;
    private String description;
    private Instant timestamp;
    private String evidenceId;
    private String evidenceUrl;

    public ProctoringEventResponse() {
    }

    public ProctoringEventResponse(String eventId, String sessionId, ProctoringEventType eventType, EventSeverity severity, String description, Instant timestamp, String evidenceId, String evidenceUrl) {
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.timestamp = timestamp;
        this.evidenceId = evidenceId;
        this.evidenceUrl = evidenceUrl;
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

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }
}
