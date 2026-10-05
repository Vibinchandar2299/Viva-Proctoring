package com.airouteviva.service;

import com.airouteviva.dto.response.*;
import com.airouteviva.entity.*;
import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class VivaReportService {

    private final VivaSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final VivaQuestionRepository questionRepository;
    private final VivaAnswerRepository answerRepository;
    private final AnswerEvaluationRepository evaluationRepository;
    private final ProctoringEventRepository proctoringEventRepository;
    private final CommunicationMetricRepository communicationMetricRepository;
    private final AIRequestRepository aiRequestRepository;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final ResourceSnapshotRepository resourceSnapshotRepository;
    private final VivaReportRepository vivaReportRepository;

    public VivaReportService(
            VivaSessionRepository sessionRepository,
            StudentRepository studentRepository,
            VivaQuestionRepository questionRepository,
            VivaAnswerRepository answerRepository,
            AnswerEvaluationRepository evaluationRepository,
            ProctoringEventRepository proctoringEventRepository,
            CommunicationMetricRepository communicationMetricRepository,
            AIRequestRepository aiRequestRepository,
            RoutingDecisionRepository routingDecisionRepository,
            ResourceSnapshotRepository resourceSnapshotRepository,
            VivaReportRepository vivaReportRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.evaluationRepository = evaluationRepository;
        this.proctoringEventRepository = proctoringEventRepository;
        this.communicationMetricRepository = communicationMetricRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.resourceSnapshotRepository = resourceSnapshotRepository;
        this.vivaReportRepository = vivaReportRepository;
    }

    @Transactional
    public VivaReportResponse generateReport(String sessionId) {
        VivaSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        Student student = studentRepository.findByStudentId(session.getStudentId())
                .orElse(new Student(session.getStudentId(), "Student " + session.getStudentId()));

        List<VivaQuestion> questions = questionRepository.findBySessionIdOrderByOrderNumberAsc(sessionId);
        List<String> questionIds = questions.stream().map(VivaQuestion::getQuestionId).toList();
        List<VivaAnswer> answers = answerRepository.findByQuestionIdIn(questionIds);

        // 1. Build Academic / Domain Section
        VivaReportResponse.VivaDomainSection domainSection = new VivaReportResponse.VivaDomainSection();
        domainSection.setStudent(new StudentResponse(student.getStudentId(), student.getName(), student.getCreatedAt()));
        domainSection.setTopic(session.getTopic());
        domainSection.setSessionStatus(session.getStatus().name());
        domainSection.setStartedAt(session.getStartedAt());
        domainSection.setEndedAt(session.getEndedAt());
        domainSection.setQuestionCount(questions.size());
        domainSection.setAnsweredCount(answers.size());

        double totalScore = 0.0;
        int evaluatedCount = 0;
        List<VivaAnswerResponse> answerResponses = new ArrayList<>();

        for (VivaAnswer ans : answers) {
            VivaAnswerResponse resp = new VivaAnswerResponse(
                    ans.getAnswerId(),
                    ans.getQuestionId(),
                    ans.getTranscript(),
                    ans.getDurationMs(),
                    ans.getSubmittedAt()
            );
            Optional<AnswerEvaluation> evalOpt = evaluationRepository.findByAnswerId(ans.getAnswerId());
            if (evalOpt.isPresent()) {
                AnswerEvaluation eval = evalOpt.get();
                resp.setSemanticScore(eval.getSemanticScore());
                resp.setEvaluation(eval.getEvaluation());
                resp.setFeedback(eval.getFeedback());
                if (eval.getSemanticScore() != null) {
                    totalScore += eval.getSemanticScore();
                    evaluatedCount++;
                }
            }
            answerResponses.add(resp);
        }
        domainSection.setAnswers(answerResponses);

        double averageScore = evaluatedCount > 0 ? Math.round((totalScore / evaluatedCount) * 10.0) / 10.0 : 0.0;
        domainSection.setOverallScore(averageScore);
        domainSection.setEvaluatorSummary(averageScore >= 75.0
                ? "Candidate demonstrated proficiency across standard and complex topics."
                : "Candidate demonstrated partial competence; review recommendations provided.");

        // Proctoring Events
        List<ProctoringEvent> events = proctoringEventRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        List<ProctoringEventResponse> eventResponses = events.stream()
                .map(e -> new ProctoringEventResponse(
                        e.getEventId(),
                        e.getSessionId(),
                        e.getEventType(),
                        e.getSeverity(),
                        e.getDescription(),
                        e.getTimestamp(),
                        e.getEvidenceId(),
                        e.getEvidenceId() != null ? "/api/proctoring/evidence/" + e.getEvidenceId() : null
                ))
                .toList();
        domainSection.setProctoringEvents(eventResponses);

        // Communication metrics
        communicationMetricRepository.findBySessionId(sessionId).ifPresent(c -> {
            domainSection.setCommunicationMetrics(Map.of(
                    "speakingDurationMs", c.getSpeakingDurationMs(),
                    "wordCount", c.getWordCount(),
                    "wordsPerMinute", c.getWordsPerMinute(),
                    "fillerWordCount", c.getFillerWordCount(),
                    "pauseCount", c.getPauseCount()
            ));
        });

        // 2. Build AI Routing & Infrastructure Section (Calculated strictly from real stored records)
        List<AIRequest> aiRequests = aiRequestRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<String> requestIds = aiRequests.stream().map(AIRequest::getRequestId).toList();
        List<RoutingDecision> decisions = routingDecisionRepository.findByRequestIdIn(requestIds);
        List<ResourceSnapshot> snapshots = resourceSnapshotRepository.findByRequestIdInOrderByTimestampAsc(requestIds);

        Map<String, ResourceSnapshot> snapMap = new HashMap<>();
        for (ResourceSnapshot s : snapshots) {
            snapMap.put(s.getRequestId(), s);
        }

        Map<String, RoutingDecision> decMap = new HashMap<>();
        for (RoutingDecision d : decisions) {
            decMap.put(d.getRequestId(), d);
        }

        long localExec = 0;
        long cacheHitCount = 0;
        long fallbackExec = 0;
        long cloudExec = 0;
        double latencySum = 0.0;
        double costSum = 0.0;
        double peakCpu = 0.0;
        double peakRam = 0.0;

        List<VivaReportResponse.DecisionTimelineEntry> timeline = new ArrayList<>();

        for (AIRequest req : aiRequests) {
            RoutingDecision dec = decMap.get(req.getRequestId());
            ResourceSnapshot snap = snapMap.get(req.getRequestId());

            VivaReportResponse.DecisionTimelineEntry entry = new VivaReportResponse.DecisionTimelineEntry();
            entry.setRequestId(req.getRequestId());
            entry.setWorkloadType(req.getWorkloadType().name());
            entry.setComplexity(req.getComplexity().name());
            entry.setPriority(req.getPriority().name());

            if (dec != null) {
                entry.setSelectedPath(dec.getSelectedPath());
                entry.setReasoning(dec.getReasoning());
                entry.setLatencyMs(dec.getLatencyMs());
                entry.setEstimatedCost(dec.getEstimatedCost());
                entry.setCacheHit(dec.getCacheHit());
                entry.setTimestamp(dec.getTimestamp());

                latencySum += dec.getLatencyMs();
                costSum += dec.getEstimatedCost();

                if (Boolean.TRUE.equals(dec.getCacheHit()) || dec.getSelectedPath() == ExecutionPath.CACHE) {
                    cacheHitCount++;
                } else if (dec.getSelectedPath() == ExecutionPath.LIGHTWEIGHT_LOCAL || dec.getSelectedPath() == ExecutionPath.HIGHER_CAPABILITY_LOCAL) {
                    localExec++;
                } else if (dec.getSelectedPath() == ExecutionPath.SIMULATED_CLOUD) {
                    cloudExec++;
                } else if (dec.getSelectedPath() == ExecutionPath.OFFLINE_FALLBACK) {
                    fallbackExec++;
                }
            }

            if (snap != null) {
                entry.setCpuUsage(snap.getCpuUsage());
                entry.setRamUsage(snap.getRamUsage());
                entry.setNetworkStatus(snap.getNetworkStatus());
                if (snap.getCpuUsage() > peakCpu) peakCpu = snap.getCpuUsage();
                if (snap.getRamUsage() > peakRam) peakRam = snap.getRamUsage();
            }

            timeline.add(entry);
        }

        VivaReportResponse.AIRoutingSection aiSection = new VivaReportResponse.AIRoutingSection();
        aiSection.setTotalAIRequests(aiRequests.size());
        aiSection.setLocalExecutions(localExec);
        aiSection.setCacheHits(cacheHitCount);
        aiSection.setFallbackExecutions(fallbackExec);
        aiSection.setSimulatedCloudExecutions(cloudExec);
        aiSection.setAverageLatencyMs(aiRequests.isEmpty() ? 0.0 : Math.round((latencySum / aiRequests.size()) * 100.0) / 100.0);
        aiSection.setTotalEstimatedCost(Math.round(costSum * 1000.0) / 1000.0);
        aiSection.setPeakCPU(Math.round(peakCpu * 10.0) / 10.0);
        aiSection.setPeakRAM(Math.round(peakRam * 10.0) / 10.0);
        aiSection.setProctoringEventCount(events.size());
        aiSection.setTimeline(timeline);

        // Save consolidated VivaReport record
        String reportId = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        vivaReportRepository.save(new VivaReport(
                reportId,
                sessionId,
                domainSection.getEvaluatorSummary(),
                "Proctoring events recorded: " + events.size(),
                String.format("AI Requests: %d | Local: %d | Cache Hits: %d | Fallbacks: %d | Avg Latency: %.1fms",
                        aiRequests.size(), localExec, cacheHitCount, fallbackExec, aiSection.getAverageLatencyMs()),
                averageScore
        ));

        VivaReportResponse response = new VivaReportResponse();
        response.setReportId(reportId);
        response.setSessionId(sessionId);
        response.setGeneratedAt(Instant.now());
        response.setVivaResults(domainSection);
        response.setAiRoutingResults(aiSection);

        return response;
    }
}
