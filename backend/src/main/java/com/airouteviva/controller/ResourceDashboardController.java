package com.airouteviva.controller;

import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.GoResourceState;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.ResourceSnapshot;
import com.airouteviva.repository.AIRequestRepository;
import com.airouteviva.repository.ResourceSnapshotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resource Dashboard", description = "Live and historical hardware & network telemetry APIs")
@CrossOrigin(origins = "*")
public class ResourceDashboardController {

    private final GoRouterClient goRouterClient;
    private final AIRequestRepository aiRequestRepository;
    private final ResourceSnapshotRepository resourceSnapshotRepository;

    public ResourceDashboardController(
            GoRouterClient goRouterClient,
            AIRequestRepository aiRequestRepository,
            ResourceSnapshotRepository resourceSnapshotRepository
    ) {
        this.goRouterClient = goRouterClient;
        this.aiRequestRepository = aiRequestRepository;
        this.resourceSnapshotRepository = resourceSnapshotRepository;
    }

    @GetMapping("/current")
    @Operation(summary = "Get live resource metrics", description = "Retrieves current hardware and network measurements sampled by the Go router")
    public ResponseEntity<GoResourceState> getCurrentResources() {
        return ResponseEntity.ok(goRouterClient.getCurrentResources());
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get historical resource snapshots", description = "Retrieves all hardware snapshots recorded for a specific viva session")
    public ResponseEntity<List<ResourceSnapshot>> getSessionResourceSnapshots(@PathVariable String sessionId) {
        List<AIRequest> requests = aiRequestRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<String> requestIds = requests.stream().map(AIRequest::getRequestId).toList();
        return ResponseEntity.ok(resourceSnapshotRepository.findByRequestIdInOrderByTimestampAsc(requestIds));
    }
}
