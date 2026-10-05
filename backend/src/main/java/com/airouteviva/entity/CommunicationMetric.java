package com.airouteviva.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "communication_metrics", indexes = {
    @Index(name = "idx_comm_session_id", columnList = "session_id")
})
public class CommunicationMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "speaking_duration_ms", nullable = false)
    private Long speakingDurationMs;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    @Column(name = "words_per_minute", nullable = false)
    private Double wordsPerMinute;

    @Column(name = "filler_word_count", nullable = false)
    private Integer fillerWordCount;

    @Column(name = "pause_count", nullable = false)
    private Integer pauseCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public CommunicationMetric() {
    }

    public CommunicationMetric(String sessionId, Long speakingDurationMs, Integer wordCount, Double wordsPerMinute, Integer fillerWordCount, Integer pauseCount) {
        this.sessionId = sessionId;
        this.speakingDurationMs = speakingDurationMs;
        this.wordCount = wordCount;
        this.wordsPerMinute = wordsPerMinute;
        this.fillerWordCount = fillerWordCount;
        this.pauseCount = pauseCount;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSpeakingDurationMs() {
        return speakingDurationMs;
    }

    public void setSpeakingDurationMs(Long speakingDurationMs) {
        this.speakingDurationMs = speakingDurationMs;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Double getWordsPerMinute() {
        return wordsPerMinute;
    }

    public void setWordsPerMinute(Double wordsPerMinute) {
        this.wordsPerMinute = wordsPerMinute;
    }

    public Integer getFillerWordCount() {
        return fillerWordCount;
    }

    public void setFillerWordCount(Integer fillerWordCount) {
        this.fillerWordCount = fillerWordCount;
    }

    public Integer getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(Integer pauseCount) {
        this.pauseCount = pauseCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
