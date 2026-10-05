package com.airouteviva.client.python.dto;

public class SpeechTranscriptionRequest {
    private String audioBase64;
    private Long durationMs;
    private String language;

    public SpeechTranscriptionRequest() {
    }

    public SpeechTranscriptionRequest(String audioBase64, Long durationMs, String language) {
        this.audioBase64 = audioBase64;
        this.durationMs = durationMs;
        this.language = language != null ? language : "en";
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
