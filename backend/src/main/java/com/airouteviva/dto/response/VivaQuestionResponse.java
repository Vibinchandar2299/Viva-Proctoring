package com.airouteviva.dto.response;

import java.time.Instant;

public class VivaQuestionResponse {
    private String questionId;
    private String sessionId;
    private String questionText;
    private Integer orderNumber;
    private String questionType;
    private Instant createdAt;

    public VivaQuestionResponse() {
    }

    public VivaQuestionResponse(String questionId, String sessionId, String questionText, Integer orderNumber, String questionType, Instant createdAt) {
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.questionText = questionText;
        this.orderNumber = orderNumber;
        this.questionType = questionType;
        this.createdAt = createdAt;
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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
