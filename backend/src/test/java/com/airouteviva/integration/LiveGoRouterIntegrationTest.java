package com.airouteviva.integration;

import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.GoResourceState;
import com.airouteviva.dto.request.CreateAIWorkloadRequest;
import com.airouteviva.dto.request.CreateQuestionRequest;
import com.airouteviva.dto.request.CreateSessionRequest;
import com.airouteviva.dto.request.CreateStudentRequest;
import com.airouteviva.dto.response.AIOrchestrationResult;
import com.airouteviva.dto.response.VivaSessionResponse;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.ResourceSnapshot;
import com.airouteviva.entity.RoutingDecision;
import com.airouteviva.entity.enums.AIRequestStatus;
import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.WorkloadType;
import com.airouteviva.repository.AIRequestRepository;
import com.airouteviva.repository.ResourceSnapshotRepository;
import com.airouteviva.repository.RoutingDecisionRepository;
import com.airouteviva.service.AIOrchestrationService;
import com.airouteviva.service.StudentService;
import com.airouteviva.service.VivaSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Section 45 Integration Test:
 * Tests the real end-to-end integration between Spring Boot and the live Go Router.
 * No mocks are used for the Go Router.
 *
 * Flow:
 * Client (REST) -> Spring Boot -> Live Go Router (port 8082) -> Real Routing Decision -> Spring Persistence -> API Response
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LiveGoRouterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentService studentService;

    @Autowired
    private VivaSessionService sessionService;

    @Autowired
    private AIRequestRepository aiRequestRepository;

    @Autowired
    private RoutingDecisionRepository routingDecisionRepository;

    @Autowired
    private ResourceSnapshotRepository resourceSnapshotRepository;

    @Autowired
    private GoRouterClient goRouterClient;

    @Test
    void testEndToEnd_RealGoRouter_RoutingAndPersistence() {
        // 1. Verify Go Router is alive and responding
        GoResourceState liveResources = goRouterClient.getCurrentResources();
        assertNotNull(liveResources, "Go Router must be running and returning live resources");
        assertTrue(liveResources.getCpuUsage() >= 0.0);
        assertTrue(liveResources.getRamUsage() >= 0.0);

        // 2. Setup Student and Viva Session
        studentService.createStudent(new CreateStudentRequest("STU-E2E-001", "Live Integration Student"));
        VivaSessionResponse session = sessionService.createSession(new CreateSessionRequest(
                "STU-E2E-001",
                "Distributed AI Systems",
                List.of(new CreateQuestionRequest("Explain CAP theorem and its impact on routing", 1, "THEORY"))
        ));
        sessionService.startSession(session.getSessionId());

        // 3. Send AI workload request via Spring Boot REST API: POST /api/ai/requests
        CreateAIWorkloadRequest workloadReq = new CreateAIWorkloadRequest(
                session.getSessionId(),
                WorkloadType.ANSWER_EVALUATION,
                Complexity.HIGH,
                Priority.HIGH,
                Map.of(
                        "question", "Explain CAP theorem and its impact on routing",
                        "answer", "In distributed data stores it is impossible to simultaneously provide Consistency, Availability, and Partition tolerance."
                )
        );

        String baseUrl = "http://localhost:" + port + "/api/ai/requests";
        ResponseEntity<AIOrchestrationResult> response = restTemplate.postForEntity(
                baseUrl,
                workloadReq,
                AIOrchestrationResult.class
        );

        // 4. Verify API Response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AIOrchestrationResult result = response.getBody();
        assertNotNull(result);
        assertNotNull(result.getAiRequest());
        String requestId = result.getAiRequest().getRequestId();
        assertNotNull(requestId);

        RoutingDecision decision = result.getRoutingDecision();
        assertNotNull(decision);
        assertNotNull(decision.getSelectedPath(), "Selected path must be populated by live Go router");
        assertNotNull(decision.getReasoning(), "Reasoning must originate from live Go router");
        assertTrue(decision.getLatencyMs() >= 0.0);

        // 5. Verify Spring Boot Database Persistence
        AIRequest persistedRequest = aiRequestRepository.findByRequestId(requestId).orElse(null);
        assertNotNull(persistedRequest);
        assertEquals(AIRequestStatus.COMPLETED, persistedRequest.getStatus());
        assertEquals(WorkloadType.ANSWER_EVALUATION, persistedRequest.getWorkloadType());

        RoutingDecision persistedDecision = routingDecisionRepository.findByRequestId(requestId).orElse(null);
        assertNotNull(persistedDecision);
        assertEquals(decision.getSelectedPath(), persistedDecision.getSelectedPath());
        assertEquals(decision.getReasoning(), persistedDecision.getReasoning());

        ResourceSnapshot persistedSnapshot = resourceSnapshotRepository.findByRequestId(requestId).orElse(null);
        assertNotNull(persistedSnapshot);
        assertTrue(persistedSnapshot.getCpuUsage() >= 0.0);
        assertTrue(persistedSnapshot.getRamUsage() >= 0.0);
        assertNotNull(persistedSnapshot.getNetworkStatus());
    }
}
