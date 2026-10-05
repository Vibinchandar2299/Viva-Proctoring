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
public class HigherCapabilityLocalAdapter implements ExecutionAdapter {

    private static final Logger log = LoggerFactory.getLogger(HigherCapabilityLocalAdapter.class);

    private final PythonAIClient pythonClient;
    private final String evalUrl;
    private final String followupUrl;
    private final String speechUrl;

    public HigherCapabilityLocalAdapter(
            PythonAIClient pythonClient,
            @Value("${app.python-ai.evaluation-url:http://localhost:5003}") String evalUrl,
            @Value("${app.python-ai.followup-url:http://localhost:5005}") String followupUrl,
            @Value("${app.python-ai.speech-url:http://localhost:5002}") String speechUrl
    ) {
        this.pythonClient = pythonClient;
        this.evalUrl = evalUrl;
        this.followupUrl = followupUrl;
        this.speechUrl = speechUrl;
    }

    @Override
    public boolean supports(ExecutionPath path) {
        return path == ExecutionPath.HIGHER_CAPABILITY_LOCAL;
    }

    @Override
    public Object execute(WorkloadType workloadType, Object payload) {
        log.info("Executing via HIGHER_CAPABILITY_LOCAL adapter: workload={}", workloadType);
        switch (workloadType) {
            case ANSWER_EVALUATION:
                return pythonClient.evaluateAnswer(evalUrl, new AnswerEvaluationRequest("Complex Question", "In-depth response", "HIGH"));
            case FOLLOW_UP_GENERATION:
                return pythonClient.generateFollowUp(followupUrl, Map.of("workload", workloadType.name(), "depth", "DEEP"));
            case SPEECH_TO_TEXT:
                return pythonClient.transcribeSpeech(speechUrl, new SpeechTranscriptionRequest(null, 5000L, "en"));
            default:
                return Map.of("status", "COMPLETED_HIGHER_CAPABILITY_LOCAL", "workload", workloadType, "fidelity", "FULL_PRECISION");
        }
    }
}
