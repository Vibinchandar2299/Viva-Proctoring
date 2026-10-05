package com.airouteviva.service;

import com.airouteviva.adapter.ExecutionDispatcher;
import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.GoResourceState;
import com.airouteviva.client.dto.GoRouteRequest;
import com.airouteviva.client.dto.GoRouteResponse;
import com.airouteviva.dto.response.AIOrchestrationResult;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.ResourceSnapshot;
import com.airouteviva.entity.RoutingDecision;
import com.airouteviva.entity.enums.AIRequestStatus;
import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.WorkloadType;
import com.airouteviva.repository.AIRequestRepository;
import com.airouteviva.repository.ResourceSnapshotRepository;
import com.airouteviva.repository.RoutingDecisionRepository;
import com.airouteviva.websocket.VivaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AIOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AIOrchestrationService.class);

    private final GoRouterClient goRouterClient;
    private final ExecutionDispatcher executionDispatcher;
    private final AIRequestRepository aiRequestRepository;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final ResourceSnapshotRepository resourceSnapshotRepository;
    private final VivaEventPublisher eventPublisher;

    public AIOrchestrationService(
            GoRouterClient goRouterClient,
            ExecutionDispatcher executionDispatcher,
            AIRequestRepository aiRequestRepository,
            RoutingDecisionRepository routingDecisionRepository,
            ResourceSnapshotRepository resourceSnapshotRepository,
            VivaEventPublisher eventPublisher
    ) {
        this.goRouterClient = goRouterClient;
        this.executionDispatcher = executionDispatcher;
        this.aiRequestRepository = aiRequestRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.resourceSnapshotRepository = resourceSnapshotRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Coordinates the complete AI request lifecycle:
     * Spring Boot (creation) -> Go Router (decision) -> Persistence -> Adapter (execution) -> WebSocket
     */
    public AIOrchestrationResult orchestrateAIRequest(
            String sessionId,
            WorkloadType workloadType,
            Complexity complexity,
            Priority priority,
            Object payload
    ) {
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Starting AI request orchestration: reqId={}, session={}, workload={}", requestId, sessionId, workloadType);

        // 1. Create and persist AIRequest
        AIRequest aiRequest = new AIRequest(
                requestId,
                sessionId,
                workloadType,
                complexity != null ? complexity : Complexity.MEDIUM,
                priority != null ? priority : Priority.MEDIUM
        );
        aiRequest = aiRequestRepository.save(aiRequest);
        eventPublisher.publishAIRequestCreated(sessionId, requestId, aiRequest);

        // 2. Query authoritative Go Router
        GoRouteRequest goReq = new GoRouteRequest(
                requestId,
                workloadType.name(),
                complexity != null ? complexity.name() : null,
                priority != null ? priority.name() : null,
                payload,
                false
        );

        GoRouteResponse goResp;
        try {
            updateStatus(aiRequest, AIRequestStatus.ROUTING);
            goResp = goRouterClient.routeRequest(goReq);
        } catch (Exception ex) {
            log.error("Routing failed for requestId={}: {}", requestId, ex.getMessage());
            updateStatus(aiRequest, AIRequestStatus.FAILED);
            throw ex;
        }

        // 3. Persist Go Routing Decision & Resource Snapshot
        ExecutionPath selectedPath = parseExecutionPath(goResp.getSelectedPath());
        RoutingDecision routingDecision = new RoutingDecision(
                requestId,
                selectedPath,
                goResp.getReasoning(),
                goResp.getLatencyMs(),
                goResp.getEstimatedCost() != null ? goResp.getEstimatedCost() : 0.0,
                goResp.getCacheHit(),
                Instant.now()
        );
        routingDecisionRepository.save(routingDecision);

        ResourceSnapshot resourceSnapshot = null;
        if (goResp.getResourceState() != null) {
            GoResourceState rs = goResp.getResourceState();
            resourceSnapshot = new ResourceSnapshot(
                    requestId,
                    rs.getCpuUsage() != null ? rs.getCpuUsage() : 0.0,
                    rs.getRamUsage() != null ? rs.getRamUsage() : 0.0,
                    rs.getRamAvailableMb() != null ? rs.getRamAvailableMb() : 0L,
                    rs.getGpuAvailable() != null ? rs.getGpuAvailable() : false,
                    rs.getGpuUsage(),
                    rs.getNetworkStatus() != null ? rs.getNetworkStatus() : "GOOD",
                    rs.getLatencyMs() != null ? rs.getLatencyMs() : 0.0,
                    rs.getBandwidthKbps() != null ? rs.getBandwidthKbps() / 1000.0 : 50.0,
                    Instant.now()
            );
            resourceSnapshotRepository.save(resourceSnapshot);
        }

        // 4. Broadcast routing events
        eventPublisher.publishRoutingDecision(sessionId, requestId, routingDecision);
        if (Boolean.TRUE.equals(goResp.getCacheHit())) {
            eventPublisher.publishCacheHit(sessionId, requestId);
        }
        if (selectedPath == ExecutionPath.OFFLINE_FALLBACK) {
            eventPublisher.publishFallbackActivated(sessionId, requestId, goResp.getReasoning());
        }

        // 5. Execute through adapter selected by Go
        updateStatus(aiRequest, AIRequestStatus.EXECUTING);
        eventPublisher.publishAIRequestStarted(sessionId, requestId, selectedPath);

        Object executionOutput;
        try {
            executionOutput = executionDispatcher.dispatch(selectedPath, workloadType, payload);
            updateStatus(aiRequest, AIRequestStatus.COMPLETED);
            eventPublisher.publishAIRequestCompleted(sessionId, requestId, executionOutput);
        } catch (Exception ex) {
            log.error("Execution adapter failed for requestId={}, path={}: {}", requestId, selectedPath, ex.getMessage());
            updateStatus(aiRequest, AIRequestStatus.FAILED);
            throw ex;
        }

        return new AIOrchestrationResult(aiRequest, routingDecision, resourceSnapshot, executionOutput, goResp);
    }

    @Transactional
    protected void updateStatus(AIRequest aiRequest, AIRequestStatus status) {
        aiRequest.setStatus(status);
        if (status == AIRequestStatus.COMPLETED || status == AIRequestStatus.FAILED) {
            aiRequest.setCompletedAt(Instant.now());
        }
        aiRequestRepository.save(aiRequest);
    }

    private ExecutionPath parseExecutionPath(String pathStr) {
        if (pathStr == null) return ExecutionPath.LIGHTWEIGHT_LOCAL;
        try {
            return ExecutionPath.valueOf(pathStr);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown execution path string '{}', defaulting to LIGHTWEIGHT_LOCAL", pathStr);
            return ExecutionPath.LIGHTWEIGHT_LOCAL;
        }
    }
}
