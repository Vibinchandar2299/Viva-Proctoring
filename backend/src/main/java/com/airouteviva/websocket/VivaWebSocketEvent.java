package com.airouteviva.websocket;

import java.time.Instant;

public class VivaWebSocketEvent {
    private WebSocketEventType eventType;
    private String timestamp;
    private String sessionId;
    private String requestId;
    private Object payload;

    public VivaWebSocketEvent() {
    }

    public VivaWebSocketEvent(WebSocketEventType eventType, String sessionId, String requestId, Object payload) {
        this.eventType = eventType;
        this.timestamp = Instant.now().toString();
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.payload = payload;
    }

    public WebSocketEventType getEventType() {
        return eventType;
    }

    public void setEventType(WebSocketEventType eventType) {
        this.eventType = eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
