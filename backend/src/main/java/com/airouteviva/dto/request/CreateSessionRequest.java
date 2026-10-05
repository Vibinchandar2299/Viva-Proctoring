package com.airouteviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateSessionRequest {

    private String sessionId; // optional, generated if null

    @NotBlank(message = "studentId is required")
    private String studentId;

    @NotBlank(message = "topic is required")
    private String topic;

    private List<CreateQuestionRequest> questions;

    public CreateSessionRequest() {
    }

    public CreateSessionRequest(String studentId, String topic, List<CreateQuestionRequest> questions) {
        this.studentId = studentId;
        this.topic = topic;
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

    public List<CreateQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuestionRequest> questions) {
        this.questions = questions;
    }
}
