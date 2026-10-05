package com.airouteviva.controller;

import com.airouteviva.dto.request.CreateSessionRequest;
import com.airouteviva.dto.request.SubmitAnswerRequest;
import com.airouteviva.dto.response.VivaAnswerResponse;
import com.airouteviva.dto.response.VivaQuestionResponse;
import com.airouteviva.dto.response.VivaSessionResponse;
import com.airouteviva.service.VivaSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viva/sessions")
@Tag(name = "Viva Session Management", description = "Session lifecycle, question retrieval, and answer submission")
@CrossOrigin(origins = "*")
public class VivaSessionController {

    private final VivaSessionService sessionService;

    public VivaSessionController(VivaSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Create viva session", description = "Initializes a new viva examination session with optional question set")
    public ResponseEntity<VivaSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get viva session", description = "Retrieves session status and questions by sessionId")
    public ResponseEntity<VivaSessionResponse> getSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.getSession(sessionId));
    }

    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start viva session", description = "Transitions session from CREATED to ACTIVE")
    public ResponseEntity<VivaSessionResponse> startSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.startSession(sessionId));
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete viva session", description = "Transitions session from ACTIVE to COMPLETED")
    public ResponseEntity<VivaSessionResponse> completeSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.completeSession(sessionId));
    }

    @GetMapping("/{sessionId}/questions")
    @Operation(summary = "Get session questions", description = "Returns ordered questions for the session")
    public ResponseEntity<List<VivaQuestionResponse>> getQuestions(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.getQuestions(sessionId));
    }

    @PostMapping("/{sessionId}/answers")
    @Operation(summary = "Submit question answer", description = "Submits an answer, routes speech-to-text and answer-evaluation through Go AI router")
    public ResponseEntity<VivaAnswerResponse> submitAnswer(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitAnswerRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.submitAnswer(sessionId, request));
    }

    @GetMapping("/{sessionId}/answers")
    @Operation(summary = "Get session answers", description = "Returns all answers and evaluations for this session")
    public ResponseEntity<List<VivaAnswerResponse>> getAnswers(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.getAnswers(sessionId));
    }
}
