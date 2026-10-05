package com.airouteviva.controller;

import com.airouteviva.dto.request.ProctoringEventRequest;
import com.airouteviva.dto.response.ProctoringEventResponse;
import com.airouteviva.service.ProctoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proctoring")
@Tag(name = "Proctoring & Evidence", description = "Proctoring violation events and local evidence storage APIs")
@CrossOrigin(origins = "*")
public class ProctoringController {

    private final ProctoringService proctoringService;

    public ProctoringController(ProctoringService proctoringService) {
        this.proctoringService = proctoringService;
    }

    @PostMapping("/events")
    @Operation(summary = "Record proctoring event", description = "Records a proctoring event, stores attached evidence, and triggers vision AI routing if required")
    public ResponseEntity<ProctoringEventResponse> recordEvent(@Valid @RequestBody ProctoringEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proctoringService.recordEvent(request));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get proctoring events", description = "Retrieves all proctoring events recorded for a session")
    public ResponseEntity<List<ProctoringEventResponse>> getEventsForSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(proctoringService.getEventsForSession(sessionId));
    }

    @GetMapping("/evidence/{evidenceId}")
    @Operation(summary = "Download evidence file", description = "Safely downloads an evidence snapshot or clip by evidenceId without exposing disk paths")
    public ResponseEntity<Resource> getEvidenceFile(@PathVariable String evidenceId) {
        Resource fileResource = proctoringService.getEvidenceFile(evidenceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }
}
