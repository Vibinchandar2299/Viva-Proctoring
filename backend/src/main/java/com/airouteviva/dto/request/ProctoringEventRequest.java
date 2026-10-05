package com.airouteviva.dto.request;

import com.airouteviva.entity.enums.EventSeverity;
import com.airouteviva.entity.enums.ProctoringEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProctoringEventRequest {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotNull(message = "eventType is required")
    private ProctoringEventType eventType;

    private EventSeverity severity;

    private String description;

    private String evidenceBase64; // Base64 encoded snapshot/clip

    private String fileName;

    private String fileType; // image/jpeg, video/mp4

    public ProctoringEventRequest() {
    }

    public ProctoringEventRequest(String sessionId, ProctoringEventType eventType, EventSeverity severity, String description) {
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.severity = severity != null ? severity : EventSeverity.INFO;
        this.description = description;
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

    public String getEvidenceBase64() {
        return evidenceBase64;
    }

    public void setEvidenceBase64(String evidenceBase64) {
        this.evidenceBase64 = evidenceBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
