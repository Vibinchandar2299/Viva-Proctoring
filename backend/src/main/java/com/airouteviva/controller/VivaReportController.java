package com.airouteviva.controller;

import com.airouteviva.dto.response.VivaReportResponse;
import com.airouteviva.service.VivaReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports & Analytics", description = "Consolidated Viva examination reports and PS1 routing benchmark summaries")
@CrossOrigin(origins = "*")
public class VivaReportController {

    private final VivaReportService vivaReportService;

    public VivaReportController(VivaReportService vivaReportService) {
        this.vivaReportService = vivaReportService;
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Generate consolidated report", description = "Generates a full viva report separating academic results from AI routing infrastructure metrics")
    public ResponseEntity<VivaReportResponse> getReport(@PathVariable String sessionId) {
        return ResponseEntity.ok(vivaReportService.generateReport(sessionId));
    }
}
