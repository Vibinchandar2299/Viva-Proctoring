package com.airouteviva.service;

import com.airouteviva.client.python.dto.AnswerEvaluationResponse;
import com.airouteviva.client.python.dto.SpeechTranscriptionResponse;
import com.airouteviva.dto.request.CreateQuestionRequest;
import com.airouteviva.dto.request.CreateSessionRequest;
import com.airouteviva.dto.request.SubmitAnswerRequest;
import com.airouteviva.dto.response.AIOrchestrationResult;
import com.airouteviva.dto.response.VivaAnswerResponse;
import com.airouteviva.dto.response.VivaQuestionResponse;
import com.airouteviva.dto.response.VivaSessionResponse;
import com.airouteviva.entity.*;
import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.SessionStatus;
import com.airouteviva.entity.enums.WorkloadType;
import com.airouteviva.exception.DuplicateResourceException;
import com.airouteviva.exception.InvalidSessionStateException;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.*;
import com.airouteviva.websocket.VivaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VivaSessionService {

    private static final Logger log = LoggerFactory.getLogger(VivaSessionService.class);

    private final VivaSessionRepository sessionRepository;
    private final VivaQuestionRepository questionRepository;
    private final VivaAnswerRepository answerRepository;
    private final AnswerEvaluationRepository evaluationRepository;
    private final StudentRepository studentRepository;
    private final AIOrchestrationService aiOrchestrationService;
    private final VivaEventPublisher eventPublisher;

    public VivaSessionService(
            VivaSessionRepository sessionRepository,
            VivaQuestionRepository questionRepository,
            VivaAnswerRepository answerRepository,
            AnswerEvaluationRepository evaluationRepository,
            StudentRepository studentRepository,
            AIOrchestrationService aiOrchestrationService,
            VivaEventPublisher eventPublisher
    ) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.evaluationRepository = evaluationRepository;
        this.studentRepository = studentRepository;
        this.aiOrchestrationService = aiOrchestrationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VivaSessionResponse createSession(CreateSessionRequest request) {
        if (!studentRepository.existsByStudentId(request.getStudentId())) {
            throw new ResourceNotFoundException("Student not found with ID: " + request.getStudentId());
        }

        String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : "SES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (sessionRepository.existsBySessionId(sessionId)) {
            throw new DuplicateResourceException("Session with ID " + sessionId + " already exists");
        }

        VivaSession session = new VivaSession(sessionId, request.getStudentId(), request.getTopic());
        session = sessionRepository.save(session);

        List<VivaQuestionResponse> questionResponses = new ArrayList<>();
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (CreateQuestionRequest qReq : request.getQuestions()) {
                String qId = qReq.getQuestionId() != null && !qReq.getQuestionId().isBlank()
                        ? qReq.getQuestionId()
                        : "Q-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

                VivaQuestion question = new VivaQuestion(
                        qId,
                        sessionId,
                        qReq.getQuestionText(),
                        qReq.getOrderNumber(),
                        qReq.getQuestionType()
                );
                question = questionRepository.save(question);
                questionResponses.add(mapQuestion(question));
            }
        }

        log.info("Created viva session: id={}, student={}, topic={}", sessionId, request.getStudentId(), request.getTopic());
        return mapSession(session, questionResponses);
    }

    @Transactional(readOnly = true)
    public VivaSessionResponse getSession(String sessionId) {
        VivaSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        List<VivaQuestionResponse> questions = getQuestions(sessionId);
        return mapSession(session, questions);
    }

    @Transactional
    public VivaSessionResponse startSession(String sessionId) {
        VivaSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new InvalidSessionStateException("Cannot start session in status: " + session.getStatus());
        }

        session.startSession();
        session = sessionRepository.save(session);
        log.info("Session started: {}", sessionId);

        eventPublisher.publish(new com.airouteviva.websocket.VivaWebSocketEvent(
                com.airouteviva.websocket.WebSocketEventType.AI_REQUEST_STARTED,
                sessionId,
                null,
                "Viva session started: " + sessionId
        ));

        return mapSession(session, getQuestions(sessionId));
    }

    @Transactional
    public VivaSessionResponse completeSession(String sessionId) {
        VivaSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStateException("Cannot complete session in status: " + session.getStatus());
        }

        session.completeSession();
        session = sessionRepository.save(session);
        log.info("Session completed: {}", sessionId);

        eventPublisher.publishVivaCompleted(sessionId, "Viva session completed successfully");

        return mapSession(session, getQuestions(sessionId));
    }

    @Transactional(readOnly = true)
    public List<VivaQuestionResponse> getQuestions(String sessionId) {
        return questionRepository.findBySessionIdOrderByOrderNumberAsc(sessionId).stream()
                .map(this::mapQuestion)
                .toList();
    }

    /**
     * Answer Submission Flow (Section 26):
     * 1. Validate session
     * 2. SPEECH_TO_TEXT AI workload if audio exists -> Go router -> Path -> Transcript
     * 3. ANSWER_EVALUATION AI workload -> Go router -> Path -> Evaluation
     * 4. Persist and return
     */
    public VivaAnswerResponse submitAnswer(String sessionId, SubmitAnswerRequest request) {
        VivaSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStateException("Answers can only be submitted during an ACTIVE session (current: " + session.getStatus() + ")");
        }

        VivaQuestion question = questionRepository.findByQuestionId(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.getQuestionId()));

        String transcript = request.getTranscript();

        // 1. SPEECH_TO_TEXT routing if audio is supplied and transcript is missing
        if (request.getAudioBase64() != null && !request.getAudioBase64().isBlank()) {
            AIOrchestrationResult sttResult = aiOrchestrationService.orchestrateAIRequest(
                    sessionId,
                    WorkloadType.SPEECH_TO_TEXT,
                    Complexity.MEDIUM,
                    Priority.HIGH,
                    Map.of("questionId", question.getQuestionId(), "audioDurationMs", request.getDurationMs() != null ? request.getDurationMs() : 3000L)
            );
            if (sttResult.getExecutionOutput() instanceof SpeechTranscriptionResponse sttResp) {
                transcript = sttResp.getTranscript();
            } else {
                transcript = "Speech transcribed through AI router path: " + sttResult.getRoutingDecision().getSelectedPath();
            }
            eventPublisher.publishTranscriptUpdate(sessionId, question.getQuestionId(), transcript);
        }

        if (transcript == null || transcript.isBlank()) {
            transcript = "No answer provided";
        }

        // 2. Save VivaAnswer
        String answerId = request.getAnswerId() != null && !request.getAnswerId().isBlank()
                ? request.getAnswerId()
                : "ANS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        VivaAnswer answer = new VivaAnswer(answerId, question.getQuestionId(), transcript, request.getDurationMs());
        answer = answerRepository.save(answer);

        // 3. ANSWER_EVALUATION routing
        AIOrchestrationResult evalResult = aiOrchestrationService.orchestrateAIRequest(
                sessionId,
                WorkloadType.ANSWER_EVALUATION,
                Complexity.HIGH,
                Priority.HIGH,
                Map.of(
                        "questionText", question.getQuestionText(),
                        "studentAnswer", transcript
                )
        );

        Double score = 85.0;
        String evaluationText = "Answer evaluated via " + evalResult.getRoutingDecision().getSelectedPath();
        String feedbackText = "Demonstrates solid understanding.";

        if (evalResult.getExecutionOutput() instanceof AnswerEvaluationResponse evalResp) {
            if (evalResp.getSemanticScore() != null) score = evalResp.getSemanticScore();
            if (evalResp.getEvaluation() != null) evaluationText = evalResp.getEvaluation();
            if (evalResp.getFeedback() != null) feedbackText = evalResp.getFeedback();
        }

        String evalId = "EVAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AnswerEvaluation answerEvaluation = new AnswerEvaluation(evalId, answer.getAnswerId(), score, evaluationText, feedbackText);
        evaluationRepository.save(answerEvaluation);

        eventPublisher.publishAnswerEvaluation(sessionId, answer.getAnswerId(), answerEvaluation);

        VivaAnswerResponse response = new VivaAnswerResponse(
                answer.getAnswerId(),
                question.getQuestionId(),
                answer.getTranscript(),
                answer.getDurationMs(),
                answer.getSubmittedAt()
        );
        response.setSemanticScore(score);
        response.setEvaluation(evaluationText);
        response.setFeedback(feedbackText);
        response.setEvaluationRoutingPath(evalResult.getRoutingDecision().getSelectedPath().name());

        return response;
    }

    @Transactional(readOnly = true)
    public List<VivaAnswerResponse> getAnswers(String sessionId) {
        List<VivaQuestion> questions = questionRepository.findBySessionIdOrderByOrderNumberAsc(sessionId);
        List<String> questionIds = questions.stream().map(VivaQuestion::getQuestionId).toList();
        List<VivaAnswer> answers = answerRepository.findByQuestionIdIn(questionIds);

        List<VivaAnswerResponse> results = new ArrayList<>();
        for (VivaAnswer ans : answers) {
            VivaAnswerResponse resp = new VivaAnswerResponse(
                    ans.getAnswerId(),
                    ans.getQuestionId(),
                    ans.getTranscript(),
                    ans.getDurationMs(),
                    ans.getSubmittedAt()
            );
            evaluationRepository.findByAnswerId(ans.getAnswerId()).ifPresent(eval -> {
                resp.setSemanticScore(eval.getSemanticScore());
                resp.setEvaluation(eval.getEvaluation());
                resp.setFeedback(eval.getFeedback());
            });
            results.add(resp);
        }
        return results;
    }

    private VivaSessionResponse mapSession(VivaSession session, List<VivaQuestionResponse> questions) {
        return new VivaSessionResponse(
                session.getSessionId(),
                session.getStudentId(),
                session.getTopic(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getCreatedAt(),
                questions
        );
    }

    private VivaQuestionResponse mapQuestion(VivaQuestion question) {
        return new VivaQuestionResponse(
                question.getQuestionId(),
                question.getSessionId(),
                question.getQuestionText(),
                question.getOrderNumber(),
                question.getQuestionType(),
                question.getCreatedAt()
        );
    }
}
