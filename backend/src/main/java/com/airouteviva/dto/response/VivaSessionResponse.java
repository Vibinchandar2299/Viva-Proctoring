package com.airouteviva.dto.response;

import com.airouteviva.entity.enums.SessionStatus;
import java.time.Instant;
import java.util.List;

public class VivaSessionResponse {
    private String sessionId;
    private String studentId;
    private String topic;
    private SessionStatus status;
    private Instant startedAt;
    private Instant endedAt;
    private Instant createdAt;
    private List<VivaQuestionResponse> questions;

    public VivaSessionResponse() {
    }

    public VivaSessionResponse(String sessionId, String studentId, String topic, SessionStatus status, Instant startedAt, Instant endedAt, Instant createdAt, List<VivaQuestionResponse> questions) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.topic = topic;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = createdAt;
        this.questions = questions;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<VivaQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<VivaQuestionResponse> questions) {
        this.questions = questions;
    }
}
