package com.airouteviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateQuestionRequest {

    private String questionId; // optional, generated if null

    @NotBlank(message = "questionText is required")
    private String questionText;

    @NotNull(message = "orderNumber is required")
    private Integer orderNumber;

    private String questionType = "STANDARD";

    public CreateQuestionRequest() {
    }

    public CreateQuestionRequest(String questionText, Integer orderNumber, String questionType) {
        this.questionText = questionText;
        this.orderNumber = orderNumber;
        this.questionType = questionType != null ? questionType : "STANDARD";
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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
}
