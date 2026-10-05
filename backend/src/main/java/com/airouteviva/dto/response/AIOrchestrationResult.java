package com.airouteviva.dto.response;

import com.airouteviva.client.dto.GoRouteResponse;
import com.airouteviva.entity.AIRequest;
import com.airouteviva.entity.ResourceSnapshot;
import com.airouteviva.entity.RoutingDecision;

public class AIOrchestrationResult {
    private AIRequest aiRequest;
    private RoutingDecision routingDecision;
    private ResourceSnapshot resourceSnapshot;
    private Object executionOutput;
    private GoRouteResponse rawGoResponse;

    public AIOrchestrationResult() {
    }

    public AIOrchestrationResult(AIRequest aiRequest, RoutingDecision routingDecision, ResourceSnapshot resourceSnapshot, Object executionOutput, GoRouteResponse rawGoResponse) {
        this.aiRequest = aiRequest;
        this.routingDecision = routingDecision;
        this.resourceSnapshot = resourceSnapshot;
        this.executionOutput = executionOutput;
        this.rawGoResponse = rawGoResponse;
    }

    public AIRequest getAiRequest() {
        return aiRequest;
    }

    public RoutingDecision getRoutingDecision() {
        return routingDecision;
    }

    public ResourceSnapshot getResourceSnapshot() {
        return resourceSnapshot;
    }

    public Object getExecutionOutput() {
        return executionOutput;
    }

    public GoRouteResponse getRawGoResponse() {
        return rawGoResponse;
    }
}
