package com.airouteviva.entity;

import com.airouteviva.entity.enums.SessionStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "viva_sessions", indexes = {
    @Index(name = "idx_session_session_id", columnList = "session_id", unique = true),
    @Index(name = "idx_session_student_id", columnList = "student_id"),
    @Index(name = "idx_session_status", columnList = "status")
})
public class VivaSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "student_id", nullable = false, length = 64)
    private String studentId;

    @Column(name = "topic", nullable = false, length = 256)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SessionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public VivaSession() {
    }

    public VivaSession(String sessionId, String studentId, String topic) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.topic = topic;
        this.status = SessionStatus.CREATED;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = SessionStatus.CREATED;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public void startSession() {
        if (this.status != SessionStatus.CREATED) {
            throw new IllegalStateException("Cannot start session in status: " + this.status);
        }
        this.status = SessionStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    public void completeSession() {
        if (this.status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot complete session in status: " + this.status);
        }
        this.status = SessionStatus.COMPLETED;
        this.endedAt = Instant.now();
    }

    public void cancelSession() {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel an already completed session");
        }
        this.status = SessionStatus.CANCELLED;
        this.endedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
