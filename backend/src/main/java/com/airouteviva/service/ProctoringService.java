package com.airouteviva.service;

import com.airouteviva.dto.request.ProctoringEventRequest;
import com.airouteviva.dto.response.ProctoringEventResponse;
import com.airouteviva.entity.Evidence;
import com.airouteviva.entity.ProctoringEvent;
import com.airouteviva.entity.enums.Complexity;
import com.airouteviva.entity.enums.EventSeverity;
import com.airouteviva.entity.enums.Priority;
import com.airouteviva.entity.enums.WorkloadType;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.EvidenceRepository;
import com.airouteviva.repository.ProctoringEventRepository;
import com.airouteviva.repository.VivaSessionRepository;
import com.airouteviva.websocket.VivaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ProctoringService {

    private static final Logger log = LoggerFactory.getLogger(ProctoringService.class);

    private final ProctoringEventRepository eventRepository;
    private final EvidenceRepository evidenceRepository;
    private final VivaSessionRepository sessionRepository;
    private final AIOrchestrationService aiOrchestrationService;
    private final VivaEventPublisher eventPublisher;
    private final String evidenceStoragePath;

    public ProctoringService(
            ProctoringEventRepository eventRepository,
            EvidenceRepository evidenceRepository,
            VivaSessionRepository sessionRepository,
            AIOrchestrationService aiOrchestrationService,
            VivaEventPublisher eventPublisher,
            @Value("${app.evidence.storage-path:./data/evidence}") String evidenceStoragePath
    ) {
        this.eventRepository = eventRepository;
        this.evidenceRepository = evidenceRepository;
        this.sessionRepository = sessionRepository;
        this.aiOrchestrationService = aiOrchestrationService;
        this.eventPublisher = eventPublisher;
        this.evidenceStoragePath = evidenceStoragePath;
        initStorageDirectory();
    }

    private void initStorageDirectory() {
        try {
            Files.createDirectories(Paths.get(evidenceStoragePath));
        } catch (IOException e) {
            log.error("Could not initialize evidence directory: {}", e.getMessage());
        }
    }

    @Transactional
    public ProctoringEventResponse recordEvent(ProctoringEventRequest request) {
        String eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String evidenceId = null;

        // 1. Process evidence file if attached
        if (request.getEvidenceBase64() != null && !request.getEvidenceBase64().isBlank()) {
            evidenceId = "EVD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String fileName = request.getFileName() != null ? request.getFileName() : (evidenceId + ".jpg");
            String fileType = request.getFileType() != null ? request.getFileType() : "image/jpeg";

            Path sessionDir = Paths.get(evidenceStoragePath, request.getSessionId());
            try {
                Files.createDirectories(sessionDir);
                Path filePath = sessionDir.resolve(fileName);
                byte[] data = Base64.getDecoder().decode(request.getEvidenceBase64());
                try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                    fos.write(data);
                }

                Evidence evidence = new Evidence(
                        evidenceId,
                        request.getSessionId(),
                        eventId,
                        fileName,
                        filePath.toString(),
                        fileType,
                        Instant.now()
                );
                evidenceRepository.save(evidence);
                log.info("Saved evidence file for event {}: {}", eventId, filePath);
            } catch (Exception ex) {
                log.error("Failed to store evidence file: {}", ex.getMessage());
            }
        }

        // 2. Trigger AI verification workload through Go Router if proctoring vision workload applies
        WorkloadType aiWorkload = mapEventToWorkload(request);
        if (aiWorkload != null) {
            aiOrchestrationService.orchestrateAIRequest(
                    request.getSessionId(),
                    aiWorkload,
                    Complexity.LOW,
                    Priority.HIGH,
                    request.getDescription()
            );
        }

        // 3. Save proctoring event
        ProctoringEvent event = new ProctoringEvent(
                eventId,
                request.getSessionId(),
                request.getEventType(),
                request.getSeverity() != null ? request.getSeverity() : EventSeverity.INFO,
                request.getDescription() != null ? request.getDescription() : request.getEventType().name(),
                evidenceId
        );
        event = eventRepository.save(event);

        ProctoringEventResponse response = mapToResponse(event);
        eventPublisher.publishProctoringEvent(request.getSessionId(), eventId, response);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ProctoringEventResponse> getEventsForSession(String sessionId) {
        if (!sessionRepository.existsBySessionId(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }
        return eventRepository.findBySessionIdOrderByTimestampAsc(sessionId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource getEvidenceFile(String evidenceId) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found: " + evidenceId));

        File file = new File(evidence.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Evidence file on disk not found for ID: " + evidenceId);
        }

        return new FileSystemResource(file);
    }

    private WorkloadType mapEventToWorkload(ProctoringEventRequest req) {
        return switch (req.getEventType()) {
            case IDENTITY_VERIFIED, IDENTITY_FAILED -> WorkloadType.IDENTITY_VERIFICATION;
            case FACE_ABSENT -> WorkloadType.FACE_DETECTION;
            case MULTIPLE_FACE -> WorkloadType.MULTIPLE_FACE_DETECTION;
            case UNAUTHORIZED_OBJECT -> WorkloadType.OBJECT_DETECTION;
            default -> null;
        };
    }

    private ProctoringEventResponse mapToResponse(ProctoringEvent event) {
        String evidenceUrl = event.getEvidenceId() != null
                ? "/api/proctoring/evidence/" + event.getEvidenceId()
                : null;

        return new ProctoringEventResponse(
                event.getEventId(),
                event.getSessionId(),
                event.getEventType(),
                event.getSeverity(),
                event.getDescription(),
                event.getTimestamp(),
                event.getEvidenceId(),
                evidenceUrl
        );
    }
}
