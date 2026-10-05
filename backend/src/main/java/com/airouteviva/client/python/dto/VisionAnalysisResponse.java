package com.airouteviva.client.python.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisionAnalysisResponse {
    private boolean verified;
    private int faceCount;
    private List<String> detectedObjects;
    private double confidence;
    private String status;

    public VisionAnalysisResponse() {
    }

    public VisionAnalysisResponse(boolean verified, int faceCount, List<String> detectedObjects, double confidence, String status) {
        this.verified = verified;
        this.faceCount = faceCount;
        this.detectedObjects = detectedObjects;
        this.confidence = confidence;
        this.status = status;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public List<String> getDetectedObjects() {
        return detectedObjects;
    }

    public void setDetectedObjects(List<String> detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
