package com.airouteviva.client.python.dto;

public class AnswerEvaluationRequest {
    private String questionText;
    private String studentAnswer;
    private String complexity;

    public AnswerEvaluationRequest() {
    }

    public AnswerEvaluationRequest(String questionText, String studentAnswer, String complexity) {
        this.questionText = questionText;
        this.studentAnswer = studentAnswer;
        this.complexity = complexity;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }
}
