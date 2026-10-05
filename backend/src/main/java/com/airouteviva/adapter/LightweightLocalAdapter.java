package com.airouteviva.adapter;

import com.airouteviva.client.python.PythonAIClient;
import com.airouteviva.client.python.dto.*;
import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LightweightLocalAdapter implements ExecutionAdapter {

    private static final Logger log = LoggerFactory.getLogger(LightweightLocalAdapter.class);

    private final PythonAIClient pythonClient;
    private final String visionUrl;
    private final String speechUrl;
    private final String evalUrl;
    private final String commUrl;
    private final String followupUrl;

    public LightweightLocalAdapter(
            PythonAIClient pythonClient,
            @Value("${app.python-ai.vision-url:http://localhost:5001}") String visionUrl,
            @Value("${app.python-ai.speech-url:http://localhost:5002}") String speechUrl,
            @Value("${app.python-ai.evaluation-url:http://localhost:5003}") String evalUrl,
            @Value("${app.python-ai.communication-url:http://localhost:5004}") String commUrl,
            @Value("${app.python-ai.followup-url:http://localhost:5005}") String followupUrl
    ) {
        this.pythonClient = pythonClient;
        this.visionUrl = visionUrl;
        this.speechUrl = speechUrl;
        this.evalUrl = evalUrl;
        this.commUrl = commUrl;
        this.followupUrl = followupUrl;
    }

    @Override
    public boolean supports(ExecutionPath path) {
        return path == ExecutionPath.LIGHTWEIGHT_LOCAL;
    }

    @Override
    public Object execute(WorkloadType workloadType, Object payload) {
        log.info("Executing via LIGHTWEIGHT_LOCAL adapter: workload={}", workloadType);
        switch (workloadType) {
            case IDENTITY_VERIFICATION, FACE_DETECTION, MULTIPLE_FACE_DETECTION, OBJECT_DETECTION:
                return pythonClient.analyzeVision(visionUrl, new VisionAnalysisRequest(workloadType.name(), null, "candidate"));
            case SPEECH_TO_TEXT:
                return pythonClient.transcribeSpeech(speechUrl, new SpeechTranscriptionRequest(null, 3500L, "en"));
            case ANSWER_EVALUATION:
                return pythonClient.evaluateAnswer(evalUrl, new AnswerEvaluationRequest("Standard Question", "Student response", "LOW"));
            case COMMUNICATION_ANALYSIS:
                return pythonClient.analyzeCommunication(commUrl, Map.of("workload", workloadType.name()));
            case FOLLOW_UP_GENERATION:
                return pythonClient.generateFollowUp(followupUrl, Map.of("workload", workloadType.name()));
            default:
                return Map.of("status", "COMPLETED_LIGHTWEIGHT_LOCAL", "workload", workloadType);
        }
    }
}
