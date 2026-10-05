package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "viva_answers", indexes = {
    @Index(name = "idx_answer_answer_id", columnList = "answer_id", unique = true),
    @Index(name = "idx_answer_question_id", columnList = "question_id")
})
public class VivaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", nullable = false, unique = true, length = 64)
    private String answerId;

    @Column(name = "question_id", nullable = false, length = 64)
    private String questionId;

    @Column(name = "transcript", length = 8192)
    private String transcript;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    public VivaAnswer() {
    }

    public VivaAnswer(String answerId, String questionId, String transcript, Long durationMs) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.transcript = transcript;
        this.durationMs = durationMs;
    }

    @PrePersist
    public void onPrePersist() {
        this.submittedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
