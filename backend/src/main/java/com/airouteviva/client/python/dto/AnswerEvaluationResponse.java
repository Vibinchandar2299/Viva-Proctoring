package com.airouteviva.client.python.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerEvaluationResponse {
    private Double semanticScore; // 0.0 - 100.0
    private String evaluation;
    private String feedback;

    public AnswerEvaluationResponse() {
    }

    public AnswerEvaluationResponse(Double semanticScore, String evaluation, String feedback) {
        this.semanticScore = semanticScore;
        this.evaluation = evaluation;
        this.feedback = feedback;
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
}
