package com.airouteviva.client.python.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisionAnalysisRequest {
    private String workloadType;
    private String imageBase64;
    private String candidateId;

    public VisionAnalysisRequest() {
    }

    public VisionAnalysisRequest(String workloadType, String imageBase64, String candidateId) {
        this.workloadType = workloadType;
        this.imageBase64 = imageBase64;
        this.candidateId = candidateId;
    }

    public String getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(String workloadType) {
        this.workloadType = workloadType;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }
}
