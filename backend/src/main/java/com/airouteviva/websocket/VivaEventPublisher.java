package com.airouteviva.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class VivaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(VivaEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public VivaEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(VivaWebSocketEvent event) {
        try {
            // General broadcast
            messagingTemplate.convertAndSend("/topic/events", event);

            // Session-specific broadcast if session is present
            if (event.getSessionId() != null && !event.getSessionId().isBlank()) {
                messagingTemplate.convertAndSend("/topic/session/" + event.getSessionId(), event);
            }
            log.debug("Published WebSocket event: type={}, session={}", event.getEventType(), event.getSessionId());
        } catch (Exception ex) {
            log.warn("Failed to publish WebSocket event {}: {}", event.getEventType(), ex.getMessage());
        }
    }

    public void publishResourceUpdate(String sessionId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.RESOURCE_UPDATE, sessionId, null, payload));
    }

    public void publishAIRequestCreated(String sessionId, String requestId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.AI_REQUEST_CREATED, sessionId, requestId, payload));
    }

    public void publishRoutingDecision(String sessionId, String requestId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.ROUTING_DECISION, sessionId, requestId, payload));
    }

    public void publishAIRequestStarted(String sessionId, String requestId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.AI_REQUEST_STARTED, sessionId, requestId, payload));
    }

    public void publishAIRequestCompleted(String sessionId, String requestId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.AI_REQUEST_COMPLETED, sessionId, requestId, payload));
    }

    public void publishProctoringEvent(String sessionId, String eventId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.PROCTORING_EVENT, sessionId, eventId, payload));
    }

    public void publishTranscriptUpdate(String sessionId, String questionId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.TRANSCRIPT_UPDATE, sessionId, questionId, payload));
    }

    public void publishAnswerEvaluation(String sessionId, String answerId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.ANSWER_EVALUATION, sessionId, answerId, payload));
    }

    public void publishCacheHit(String sessionId, String requestId) {
        publish(new VivaWebSocketEvent(WebSocketEventType.CACHE_HIT, sessionId, requestId, "Deterministic LRU cache hit"));
    }

    public void publishFallbackActivated(String sessionId, String requestId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.FALLBACK_ACTIVATED, sessionId, requestId, payload));
    }

    public void publishVivaCompleted(String sessionId, Object payload) {
        publish(new VivaWebSocketEvent(WebSocketEventType.VIVA_COMPLETED, sessionId, null, payload));
    }
}
