package com.airouteviva.controller;

import com.airouteviva.client.GoRouterClient;
import com.airouteviva.client.dto.GoBenchmarkCompareResponse;
import com.airouteviva.client.dto.GoRouteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "Benchmark & Baseline", description = "PS1 baseline comparison and simulation delegation to Go router")
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private final GoRouterClient goRouterClient;

    public BenchmarkController(GoRouterClient goRouterClient) {
        this.goRouterClient = goRouterClient;
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare AI-ROUTE against Baseline", description = "Delegates to Go router to execute side-by-side benchmark comparison against FIXED_EXECUTION baseline")
    public ResponseEntity<GoBenchmarkCompareResponse> compareBenchmark(
            @RequestParam(required = false, defaultValue = "HIGHER_CAPABILITY_LOCAL") String fixedPath,
            @RequestBody(required = false) GoRouteRequest request
    ) {
        return ResponseEntity.ok(goRouterClient.compareBenchmark(fixedPath, request));
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate hardware / network condition", description = "Directly sets simulation condition on Go router (e.g. HIGH_RAM, OFFLINE_NET)")
    public ResponseEntity<Map<String, Object>> simulateCondition(@RequestBody Map<String, Object> simulationRequest) {
        return ResponseEntity.ok(goRouterClient.simulateCondition(simulationRequest));
    }
}
