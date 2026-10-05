package com.airouteviva.service;

import com.airouteviva.adapter.ExecutionDispatcher;
import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.*;
import com.airouteviva.dto.request.*;
import com.airouteviva.dto.response.*;
import com.airouteviva.entity.*;
import com.airouteviva.entity.enums.*;
import com.airouteviva.exception.DuplicateResourceException;
import com.airouteviva.exception.InvalidSessionStateException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class VivaLifecycleUnitTest {

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
    private StudentRepository studentRepository;

    @Autowired
    private VivaSessionRepository sessionRepository;

    @Autowired
    private VivaQuestionRepository questionRepository;

    @Autowired
    private VivaAnswerRepository answerRepository;

    @Autowired
    private AIRequestRepository aiRequestRepository;

    @Autowired
    private RoutingDecisionRepository routingDecisionRepository;

    @Autowired
    private ResourceSnapshotRepository resourceSnapshotRepository;

    @Autowired
    private ProctoringEventRepository proctoringEventRepository;

    @MockBean
    private GoRouterClient goRouterClient;

    @MockBean
    private VivaEventPublisher eventPublisher;

    @BeforeEach
    void setupMocks() {
        GoResourceState resState = new GoResourceState();
        resState.setCpuUsage(25.0);
        resState.setRamUsage(45.0);
        resState.setRamAvailableMb(4500L);
        resState.setGpuAvailable(true);
        resState.setNetworkStatus("GOOD");
        resState.setLatencyMs(20.0);

        GoRouteResponse mockResp = new GoRouteResponse();
        mockResp.setRequestId("MOCK-REQ-1");
        mockResp.setSelectedPath("HIGHER_CAPABILITY_LOCAL");
        mockResp.setReasoning("Selected HIGHER_CAPABILITY_LOCAL because resources are optimal");
        mockResp.setLatencyMs(280.0);
        mockResp.setEstimatedCost(0.0);
        mockResp.setResourceState(resState);
        mockResp.setCacheHit(false);

        when(goRouterClient.routeRequest(any(GoRouteRequest.class))).thenReturn(mockResp);
        when(goRouterClient.getCurrentResources()).thenReturn(resState);
    }

    @Test
    void test1_StudentCreation_And_DuplicateHandling() {
        CreateStudentRequest req = new CreateStudentRequest("STU-1001", "Alice Johnson");
        StudentResponse resp = studentService.createStudent(req);

        assertNotNull(resp);
        assertEquals("STU-1001", resp.getStudentId());
        assertEquals("Alice Johnson", resp.getName());

        // Duplicate rejection
        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(req));
    }

    @Test
    void test2_SessionLifecycle_ValidAndInvalidTransitions() {
        studentService.createStudent(new CreateStudentRequest("STU-1002", "Bob Smith"));

        CreateQuestionRequest q1 = new CreateQuestionRequest("Explain OOP concepts.", 1, "STANDARD");
        CreateSessionRequest sessReq = new CreateSessionRequest("STU-1002", "Object Oriented Programming", List.of(q1));

        VivaSessionResponse session = sessionService.createSession(sessReq);
        assertEquals(SessionStatus.CREATED, session.getStatus());
        assertEquals(1, session.getQuestions().size());

        // Invalid complete transition before start
        assertThrows(InvalidSessionStateException.class, () -> sessionService.completeSession(session.getSessionId()));

        // Start session: CREATED -> ACTIVE
        VivaSessionResponse started = sessionService.startSession(session.getSessionId());
        assertEquals(SessionStatus.ACTIVE, started.getStatus());

        // Invalid start transition when already ACTIVE
        assertThrows(InvalidSessionStateException.class, () -> sessionService.startSession(session.getSessionId()));

        // Complete session: ACTIVE -> COMPLETED
        VivaSessionResponse completed = sessionService.completeSession(session.getSessionId());
        assertEquals(SessionStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void test3_AnswerSubmission_And_AIRoutingPersistence() {
        studentService.createStudent(new CreateStudentRequest("STU-1003", "Charlie Davis"));

        CreateQuestionRequest q1 = new CreateQuestionRequest("Define deadlock in operating systems.", 1, "STANDARD");
        VivaSessionResponse session = sessionService.createSession(new CreateSessionRequest("STU-1003", "Operating Systems", List.of(q1)));
        sessionService.startSession(session.getSessionId());

        String questionId = session.getQuestions().get(0).getQuestionId();
        SubmitAnswerRequest ansReq = new SubmitAnswerRequest(questionId, "Deadlock occurs when multiple processes hold resources while waiting for each other.", 4500L);

        VivaAnswerResponse ansResp = sessionService.submitAnswer(session.getSessionId(), ansReq);

        assertNotNull(ansResp);
        assertEquals(questionId, ansResp.getQuestionId());
        assertNotNull(ansResp.getSemanticScore());
        assertEquals("HIGHER_CAPABILITY_LOCAL", ansResp.getEvaluationRoutingPath());

        // Verify routing decision and snapshot were persisted
        List<AIRequest> requests = aiRequestRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId());
        assertFalse(requests.isEmpty());

        AIRequest evalReq = requests.stream().filter(r -> r.getWorkloadType() == WorkloadType.ANSWER_EVALUATION).findFirst().orElse(null);
        assertNotNull(evalReq);
        assertEquals(AIRequestStatus.COMPLETED, evalReq.getStatus());

        assertTrue(routingDecisionRepository.findByRequestId(evalReq.getRequestId()).isPresent());
        assertTrue(resourceSnapshotRepository.findByRequestId(evalReq.getRequestId()).isPresent());
    }

    @Test
    void test4_ProctoringEventPersistence_And_ReportGeneration() {
        studentService.createStudent(new CreateStudentRequest("STU-1004", "Diana Prince"));

        CreateQuestionRequest q1 = new CreateQuestionRequest("What is normalization?", 1, "STANDARD");
        VivaSessionResponse session = sessionService.createSession(new CreateSessionRequest("STU-1004", "Databases", List.of(q1)));
        sessionService.startSession(session.getSessionId());

        // Record proctoring event
        ProctoringEventRequest eventReq = new ProctoringEventRequest(
                session.getSessionId(),
                ProctoringEventType.UNAUTHORIZED_OBJECT,
                EventSeverity.WARNING,
                "Unauthorized mobile phone detected near desk"
        );
        ProctoringEventResponse eventResp = proctoringService.recordEvent(eventReq);
        assertNotNull(eventResp);
        assertEquals(ProctoringEventType.UNAUTHORIZED_OBJECT, eventResp.getEventType());

        // Submit answer
        sessionService.submitAnswer(session.getSessionId(), new SubmitAnswerRequest(session.getQuestions().get(0).getQuestionId(), "Normalization minimizes redundancy.", 2000L));
        sessionService.completeSession(session.getSessionId());

        // Generate final report
        VivaReportResponse report = reportService.generateReport(session.getSessionId());
        assertNotNull(report);
        assertEquals(session.getSessionId(), report.getSessionId());
        assertNotNull(report.getVivaResults());
        assertEquals(1, report.getVivaResults().getQuestionCount());
        assertEquals(1, report.getVivaResults().getAnsweredCount());
        assertNotNull(report.getAiRoutingResults());
        assertTrue(report.getAiRoutingResults().getTotalAIRequests() > 0);
        assertTrue(report.getAiRoutingResults().getPeakRAM() > 0.0);
    }
}
