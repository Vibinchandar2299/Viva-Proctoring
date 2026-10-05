package com.airouteviva.dto.response;

import java.time.Instant;

public class VivaAnswerResponse {
    private String answerId;
    private String questionId;
    private String transcript;
    private Long durationMs;
    private Instant submittedAt;
    private Double semanticScore;
    private String evaluation;
    private String feedback;
    private String evaluationRoutingPath;

    public VivaAnswerResponse() {
    }

    public VivaAnswerResponse(String answerId, String questionId, String transcript, Long durationMs, Instant submittedAt) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.transcript = transcript;
        this.durationMs = durationMs;
        this.submittedAt = submittedAt;
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

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
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

    public String getEvaluationRoutingPath() {
        return evaluationRoutingPath;
    }

    public void setEvaluationRoutingPath(String evaluationRoutingPath) {
        this.evaluationRoutingPath = evaluationRoutingPath;
    }
}
