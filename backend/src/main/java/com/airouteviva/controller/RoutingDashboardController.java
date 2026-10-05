package com.airouteviva.controller;

import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.GoMetricsResponse;
import com.airouteviva.client.dto.GoRoutingHistoryResponse;
import com.airouteviva.dto.response.SessionRoutingRecordResponse;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.ResourceSnapshot;
import com.airouteviva.entity.RoutingDecision;
import com.airouteviva.repository.AIRequestRepository;
import com.airouteviva.repository.ResourceSnapshotRepository;
import com.airouteviva.repository.RoutingDecisionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/routing")
@Tag(name = "Routing Dashboard", description = "PS1 AI request routing audit, telemetry, and decision timeline APIs")
@CrossOrigin(origins = "*")
public class RoutingDashboardController {

    private final AIRequestRepository aiRequestRepository;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final ResourceSnapshotRepository resourceSnapshotRepository;
    private final GoRouterClient goRouterClient;

    public RoutingDashboardController(
            AIRequestRepository aiRequestRepository,
            RoutingDecisionRepository routingDecisionRepository,
            ResourceSnapshotRepository resourceSnapshotRepository,
            GoRouterClient goRouterClient
    ) {
        this.aiRequestRepository = aiRequestRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.resourceSnapshotRepository = resourceSnapshotRepository;
        this.goRouterClient = goRouterClient;
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session routing timeline", description = "Returns complete decision timeline with hardware states and reasoning for a specific session")
    public ResponseEntity<List<SessionRoutingRecordResponse>> getSessionRoutingTimeline(@PathVariable String sessionId) {
        List<AIRequest> requests = aiRequestRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<String> requestIds = requests.stream().map(AIRequest::getRequestId).toList();

        List<RoutingDecision> decisions = routingDecisionRepository.findByRequestIdIn(requestIds);
        List<ResourceSnapshot> snapshots = resourceSnapshotRepository.findByRequestIdInOrderByTimestampAsc(requestIds);

        Map<String, RoutingDecision> decMap = new HashMap<>();
        for (RoutingDecision d : decisions) {
            decMap.put(d.getRequestId(), d);
        }

        Map<String, ResourceSnapshot> snapMap = new HashMap<>();
        for (ResourceSnapshot s : snapshots) {
            snapMap.put(s.getRequestId(), s);
        }

        List<SessionRoutingRecordResponse> records = new ArrayList<>();
        for (AIRequest req : requests) {
            RoutingDecision dec = decMap.get(req.getRequestId());
            ResourceSnapshot snap = snapMap.get(req.getRequestId());

            SessionRoutingRecordResponse r = new SessionRoutingRecordResponse();
            r.setRequestId(req.getRequestId());
            r.setWorkloadType(req.getWorkloadType().name());
            r.setComplexity(req.getComplexity().name());
            r.setPriority(req.getPriority().name());

            if (dec != null) {
                r.setSelectedPath(dec.getSelectedPath());
                r.setReasoning(dec.getReasoning());
                r.setLatencyMs(dec.getLatencyMs());
                r.setEstimatedCost(dec.getEstimatedCost());
                r.setCacheHit(dec.getCacheHit());
                r.setTimestamp(dec.getTimestamp());
            }

            if (snap != null) {
                r.setCpuUsage(snap.getCpuUsage());
                r.setRamUsage(snap.getRamUsage());
                r.setNetworkStatus(snap.getNetworkStatus());
            }

            records.add(r);
        }

        return ResponseEntity.ok(records);
    }

    @GetMapping("/live/metrics")
    @Operation(summary = "Get live Go router metrics", description = "Directly retrieves real-time aggregated metrics from the Go routing engine")
    public ResponseEntity<GoMetricsResponse> getLiveMetrics() {
        return ResponseEntity.ok(goRouterClient.getMetrics());
    }

    @GetMapping("/live/history")
    @Operation(summary = "Get live Go router history", description = "Directly retrieves decision history from the Go routing engine")
    public ResponseEntity<GoRoutingHistoryResponse> getLiveHistory(
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String workload
    ) {
        return ResponseEntity.ok(goRouterClient.getRoutingHistory(limit, workload));
    }
}
