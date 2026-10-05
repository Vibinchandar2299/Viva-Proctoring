package com.airouteviva.client.python.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpeechTranscriptionResponse {
    private String transcript;
    private double confidence;
    private Long durationMs;

    public SpeechTranscriptionResponse() {
    }

    public SpeechTranscriptionResponse(String transcript, double confidence, Long durationMs) {
        this.transcript = transcript;
        this.confidence = confidence;
        this.durationMs = durationMs;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
