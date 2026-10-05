package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "answer_evaluations", indexes = {
    @Index(name = "idx_eval_evaluation_id", columnList = "evaluation_id", unique = true),
    @Index(name = "idx_eval_answer_id", columnList = "answer_id")
})
public class AnswerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_id", nullable = false, unique = true, length = 64)
    private String evaluationId;

    @Column(name = "answer_id", nullable = false, length = 64)
    private String answerId;

    @Column(name = "semantic_score", nullable = false)
    private Double semanticScore; // 0.0 - 100.0 or 0.0 - 10.0

    @Column(name = "evaluation", nullable = false, length = 4096)
    private String evaluation;

    @Column(name = "feedback", length = 4096)
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AnswerEvaluation() {
    }

    public AnswerEvaluation(String evaluationId, String answerId, Double semanticScore, String evaluation, String feedback) {
        this.evaluationId = evaluationId;
        this.answerId = answerId;
        this.semanticScore = semanticScore;
        this.evaluation = evaluation;
        this.feedback = feedback;
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

    public String getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public Double getSemanticScore() {
        return semanticScore;
    }

    public void setSemanticScore(Double semanticScore) {
        this.semanticScore = semanticScore;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
