package com.airouteviva.client;

import com.airouteviva.client.dto.*;

import java.util.Map;

public interface GoRouterClient {

    /**
     * Sends an AI workload request to the Go Router for feasibility, scoring, and path selection.
     */
    GoRouteResponse routeRequest(GoRouteRequest request);

    /**
     * Retrieves live hardware and network resource metrics sampled by the Go Router.
     */
    GoResourceState getCurrentResources();

    /**
     * Fetches router operational health and path readiness.
     */
    Map<String, Object> getHealth();

    /**
     * Retrieves runtime aggregated metrics from the Go router.
     */
    GoMetricsResponse getMetrics();

    /**
     * Retrieves the in-memory decision audit history from the Go router.
     */
    GoRoutingHistoryResponse getRoutingHistory(Integer limit, String workload);

    /**
     * Triggers dynamic simulation condition on the Go router (e.g. HIGH_RAM, OFFLINE_NET).
     */
    Map<String, Object> simulateCondition(Map<String, Object> simulationRequest);

    /**
     * Performs side-by-side benchmark comparison between AI-ROUTE and FIXED_EXECUTION baseline.
     */
    GoBenchmarkCompareResponse compareBenchmark(String fixedPath, GoRouteRequest request);

    /**
     * Flushes the Go router in-process LRU cache.
     */
    void clearCache();
}
