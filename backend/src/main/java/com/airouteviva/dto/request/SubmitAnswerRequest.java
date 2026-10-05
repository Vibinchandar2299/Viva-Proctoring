package com.airouteviva.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SubmitAnswerRequest {

    private String answerId; // optional, generated if null

    @NotBlank(message = "questionId is required")
    private String questionId;

    private String transcript; // text answer or populated via STT

    private Long durationMs;

    private String audioBase64; // optional raw audio snippet for speech-to-text pipeline

    public SubmitAnswerRequest() {
    }

    public SubmitAnswerRequest(String questionId, String transcript, Long durationMs) {
        this.questionId = questionId;
        this.transcript = transcript;
        this.durationMs = durationMs;
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

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}
