package com.airouteviva.service;

import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.*;
import com.airouteviva.dto.request.*;
import com.airouteviva.dto.response.*;
import com.airouteviva.entity.enums.*;
import com.airouteviva.exception.DuplicateResourceException;
import com.airouteviva.exception.GoRouterUnavailableException;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.*;
import com.airouteviva.websocket.VivaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class VivaValidationAndErrorUnitTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private VivaSessionService sessionService;

    @Autowired
    private AIOrchestrationService aiOrchestrationService;

    @Autowired
    private ProctoringService proctoringService;

    @Autowired
    private VivaReportService reportService;

    @Autowired
    private AIRequestRepository aiRequestRepository;

    @MockBean
    private GoRouterClient goRouterClient;

    @MockBean
    private VivaEventPublisher eventPublisher;

    @BeforeEach
    void setupMocks() {
        GoResourceState resState = new GoResourceState();
        resState.setCpuUsage(30.0);
        resState.setRamUsage(50.0);
        resState.setRamAvailableMb(4000L);
        resState.setGpuAvailable(true);
        resState.setNetworkStatus("GOOD");
        resState.setLatencyMs(15.0);

        GoRouteResponse mockResp = new GoRouteResponse();
        mockResp.setRequestId("MOCK-REQ-100");
        mockResp.setSelectedPath("LIGHTWEIGHT_LOCAL");
        mockResp.setReasoning("Lightweight local selected for testing");
        mockResp.setLatencyMs(120.0);
        mockResp.setEstimatedCost(0.0);
        mockResp.setResourceState(resState);
        mockResp.setCacheHit(false);

        when(goRouterClient.routeRequest(any(GoRouteRequest.class))).thenReturn(mockResp);
        when(goRouterClient.getCurrentResources()).thenReturn(resState);
    }

    @Test
    void testQuestionRetrieval() {
        studentService.createStudent(new CreateStudentRequest("STU-RETRIEVE", "Eve Adams"));

        CreateQuestionRequest q1 = new CreateQuestionRequest("Question 1", 1, "CODE");
        CreateQuestionRequest q2 = new CreateQuestionRequest("Question 2", 2, "THEORY");

        VivaSessionResponse session = sessionService.createSession(
                new CreateSessionRequest("STU-RETRIEVE", "Algorithms", List.of(q1, q2))
        );

        List<VivaQuestionResponse> questions = sessionService.getQuestions(session.getSessionId());
        assertEquals(2, questions.size());
        assertEquals(1, questions.get(0).getOrderNumber());
        assertEquals("Question 1", questions.get(0).getQuestionText());
        assertEquals(2, questions.get(1).getOrderNumber());
    }

    @Test
    void testAIRequestCreation_DirectOrchestration() {
        studentService.createStudent(new CreateStudentRequest("STU-ORCH", "Frank Miller"));
        VivaSessionResponse session = sessionService.createSession(
                new CreateSessionRequest("STU-ORCH", "Computer Networks", List.of(new CreateQuestionRequest("Explain TCP handshakes", 1, "THEORY")))
        );
        sessionService.startSession(session.getSessionId());

        AIOrchestrationResult result = aiOrchestrationService.orchestrateAIRequest(
                session.getSessionId(),
                WorkloadType.IDENTITY_VERIFICATION,
                Complexity.LOW,
                Priority.HIGH,
                Map.of("studentId", "STU-ORCH")
        );

        assertNotNull(result);
        assertNotNull(result.getRequestId());
        assertEquals("LIGHTWEIGHT_LOCAL", result.getRoutingDecision().getSelectedPath());

        // Verify WebSocket event was published
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void testGoServiceUnavailable_ThrowsGoRouterUnavailableException() {
        studentService.createStudent(new CreateStudentRequest("STU-GO-FAIL", "Hank Pym"));
        VivaSessionResponse session = sessionService.createSession(
                new CreateSessionRequest("STU-GO-FAIL", "Security", List.of())
        );

        // Force GoRouterClient to throw GoRouterUnavailableException
        when(goRouterClient.routeRequest(any(GoRouteRequest.class)))
                .thenThrow(new GoRouterUnavailableException("Go router service is currently unavailable"));

        assertThrows(GoRouterUnavailableException.class, () -> aiOrchestrationService.orchestrateAIRequest(
                session.getSessionId(),
                WorkloadType.ANSWER_EVALUATION,
                Complexity.HIGH,
                Priority.HIGH,
                Map.of("question", "What is an injection attack?")
        ));
    }

    @Test
    void testResourceNotFound_ForInvalidSession() {
        assertThrows(ResourceNotFoundException.class, () -> sessionService.getSession("SES-NON-EXISTENT"));
        assertThrows(ResourceNotFoundException.class, () -> proctoringService.getEventsForSession("SES-NON-EXISTENT"));
        assertThrows(ResourceNotFoundException.class, () -> reportService.generateReport("SES-NON-EXISTENT"));
    }
}
