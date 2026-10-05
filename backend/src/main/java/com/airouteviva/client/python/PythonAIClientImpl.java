package com.airouteviva.client.python;

import com.airouteviva.client.python.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class PythonAIClientImpl implements PythonAIClient {

    private static final Logger log = LoggerFactory.getLogger(PythonAIClientImpl.class);

    private final RestClient restClient;
    private final boolean stubEnabled;

    public PythonAIClientImpl(@Value("${app.python-ai.stub-enabled:true}") boolean stubEnabled) {
        this.restClient = RestClient.builder().build();
        this.stubEnabled = stubEnabled;
    }

    @Override
    public VisionAnalysisResponse analyzeVision(String endpointUrl, VisionAnalysisRequest request) {
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            try {
                return restClient.post()
                        .uri(endpointUrl + "/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(VisionAnalysisResponse.class);
            } catch (RestClientException ex) {
                log.warn("Python Vision service unreachable at {}: {}", endpointUrl, ex.getMessage());
                if (!stubEnabled) throw ex;
            }
        }

        // Development stub: realistic output based on workload
        if ("MULTIPLE_FACE_DETECTION".equals(request.getWorkloadType())) {
            return new VisionAnalysisResponse(true, 1, List.of(), 0.98, "NORMAL");
        } else if ("OBJECT_DETECTION".equals(request.getWorkloadType())) {
            return new VisionAnalysisResponse(true, 1, List.of(), 0.95, "CLEAR");
        } else if ("IDENTITY_VERIFICATION".equals(request.getWorkloadType())) {
            return new VisionAnalysisResponse(true, 1, List.of(), 0.99, "VERIFIED");
        }
        return new VisionAnalysisResponse(true, 1, List.of(), 0.96, "FACE_DETECTED");
    }

    @Override
    public SpeechTranscriptionResponse transcribeSpeech(String endpointUrl, SpeechTranscriptionRequest request) {
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            try {
                return restClient.post()
                        .uri(endpointUrl + "/transcribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(SpeechTranscriptionResponse.class);
            } catch (RestClientException ex) {
                log.warn("Python Speech service unreachable at {}: {}", endpointUrl, ex.getMessage());
                if (!stubEnabled) throw ex;
            }
        }

        return new SpeechTranscriptionResponse(
                "An interface in Java defines a contract of abstract methods that implementing classes must satisfy.",
                0.97,
                request.getDurationMs() != null ? request.getDurationMs() : 3500L
        );
    }

    @Override
    public AnswerEvaluationResponse evaluateAnswer(String endpointUrl, AnswerEvaluationRequest request) {
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            try {
                return restClient.post()
                        .uri(endpointUrl + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(AnswerEvaluationResponse.class);
            } catch (RestClientException ex) {
                log.warn("Python Evaluation service unreachable at {}: {}", endpointUrl, ex.getMessage());
                if (!stubEnabled) throw ex;
            }
        }

        return new AnswerEvaluationResponse(
                88.5,
                "The student correctly identified the core principle and explained practical implications.",
                "Good clarity on definitions; could elaborate further on real-world trade-offs."
        );
    }

    @Override
    public Map<String, Object> analyzeCommunication(String endpointUrl, Map<String, Object> request) {
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            try {
                return restClient.post()
                        .uri(endpointUrl + "/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            } catch (RestClientException ex) {
                log.warn("Python Communication service unreachable at {}: {}", endpointUrl, ex.getMessage());
                if (!stubEnabled) throw ex;
            }
        }

        return Map.of(
                "speakingDurationMs", 18500L,
                "wordCount", 48,
                "wordsPerMinute", 155.6,
                "fillerWordCount", 2,
                "pauseCount", 1
        );
    }

    @Override
    public Map<String, Object> generateFollowUp(String endpointUrl, Map<String, Object> request) {
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            try {
                return restClient.post()
                        .uri(endpointUrl + "/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            } catch (RestClientException ex) {
                log.warn("Python Followup service unreachable at {}: {}", endpointUrl, ex.getMessage());
                if (!stubEnabled) throw ex;
            }
        }

        return Map.of(
                "question", "Can an abstract class have constructors, and if so, how are they invoked?",
                "orderNumber", 2,
                "questionType", "FOLLOW_UP"
        );
    }
}
