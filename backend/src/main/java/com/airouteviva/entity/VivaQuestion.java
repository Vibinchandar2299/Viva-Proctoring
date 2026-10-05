package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "viva_questions", indexes = {
    @Index(name = "idx_question_question_id", columnList = "question_id", unique = true),
    @Index(name = "idx_question_session_id", columnList = "session_id")
})
public class VivaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false, unique = true, length = 64)
    private String questionId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "question_text", nullable = false, length = 2048)
    private String questionText;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(name = "question_type", nullable = false, length = 64)
    private String questionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VivaQuestion() {
    }

    public VivaQuestion(String questionId, String sessionId, String questionText, Integer orderNumber, String questionType) {
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.questionText = questionText;
        this.orderNumber = orderNumber;
        this.questionType = questionType != null ? questionType : "STANDARD";
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
        if (this.questionType == null) {
            this.questionType = "STANDARD";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
