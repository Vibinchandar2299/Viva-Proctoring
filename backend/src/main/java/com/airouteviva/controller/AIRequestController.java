package com.airouteviva.controller;

import com.airouteviva.dto.request.CreateAIWorkloadRequest;
import com.airouteviva.dto.response.AIOrchestrationResult;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.AIRequestRepository;
import com.airouteviva.service.AIOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/requests")
@Tag(name = "AI Request Management", description = "PS1 AI workload creation and status tracking APIs")
@CrossOrigin(origins = "*")
public class AIRequestController {

    private final AIOrchestrationService aiOrchestrationService;
    private final AIRequestRepository aiRequestRepository;

    public AIRequestController(AIOrchestrationService aiOrchestrationService, AIRequestRepository aiRequestRepository) {
        this.aiOrchestrationService = aiOrchestrationService;
        this.aiRequestRepository = aiRequestRepository;
    }

    @PostMapping
    @Operation(summary = "Submit AI workload", description = "Submits an independent AI workload to be routed by the Go router and executed via the selected adapter")
    public ResponseEntity<AIOrchestrationResult> submitAIRequest(@Valid @RequestBody CreateAIWorkloadRequest request) {
        AIOrchestrationResult result = aiOrchestrationService.orchestrateAIRequest(
                request.getSessionId(),
                request.getWorkloadType(),
                request.getComplexity(),
                request.getPriority(),
                request.getPayload()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get AI request status", description = "Retrieves AI workload status and metadata by requestId")
    public ResponseEntity<AIRequest> getAIRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(aiRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("AI request not found: " + requestId)));
    }
}
