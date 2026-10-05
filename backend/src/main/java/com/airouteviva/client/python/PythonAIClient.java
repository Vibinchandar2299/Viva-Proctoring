package com.airouteviva.client.python;

import com.airouteviva.client.python.dto.*;
import java.util.Map;

public interface PythonAIClient {
    VisionAnalysisResponse analyzeVision(String endpointUrl, VisionAnalysisRequest request);
    SpeechTranscriptionResponse transcribeSpeech(String endpointUrl, SpeechTranscriptionRequest request);
    AnswerEvaluationResponse evaluateAnswer(String endpointUrl, AnswerEvaluationRequest request);
    Map<String, Object> analyzeCommunication(String endpointUrl, Map<String, Object> request);
    Map<String, Object> generateFollowUp(String endpointUrl, Map<String, Object> request);
}
