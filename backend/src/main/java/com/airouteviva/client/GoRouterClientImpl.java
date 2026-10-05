package com.airouteviva.client;

import com.airouteviva.client.dto.*;
import com.airouteviva.exception.GoRouterUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

@Component
public class GoRouterClientImpl implements GoRouterClient {

    private static final Logger log = LoggerFactory.getLogger(GoRouterClientImpl.class);

    private final RestClient restClient;
    private final String baseUrl;

    public GoRouterClientImpl(
            @Value("${app.go-router.url:http://localhost:8082}") String baseUrl,
            @Value("${app.go-router.connect-timeout-ms:5000}") int connectTimeout,
            @Value("${app.go-router.read-timeout-ms:15000}") int readTimeout
    ) {
        this.baseUrl = baseUrl;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public GoRouteResponse routeRequest(GoRouteRequest request) {
        try {
            log.info("Sending route request to Go router: requestId={}, workload={}", request.getRequestId(), request.getWorkloadType());
            return restClient.post()
                    .uri("/route")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GoRouteResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to communicate with Go router at {}/route: {}", baseUrl, ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GoResourceState getCurrentResources() {
        try {
            return restClient.get()
                    .uri("/resources")
                    .retrieve()
                    .body(GoResourceState.class);
        } catch (RestClientException ex) {
            log.error("Failed to fetch resources from Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Object> getHealth() {
        try {
            return restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientException ex) {
            log.error("Failed to fetch health from Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GoMetricsResponse getMetrics() {
        try {
            return restClient.get()
                    .uri("/metrics")
                    .retrieve()
                    .body(GoMetricsResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to fetch metrics from Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GoRoutingHistoryResponse getRoutingHistory(Integer limit, String workload) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/routing/history");
                        if (limit != null) uriBuilder.queryParam("limit", limit);
                        if (workload != null && !workload.isBlank()) uriBuilder.queryParam("workload", workload);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(GoRoutingHistoryResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to fetch routing history from Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Object> simulateCondition(Map<String, Object> simulationRequest) {
        try {
            return restClient.post()
                    .uri("/simulate/condition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(simulationRequest)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientException ex) {
            log.error("Failed to execute simulation on Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GoBenchmarkCompareResponse compareBenchmark(String fixedPath, GoRouteRequest request) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/benchmark/compare")
                            .queryParam("fixedPath", fixedPath != null ? fixedPath : "HIGHER_CAPABILITY_LOCAL")
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request != null ? request : new GoRouteRequest())
                    .retrieve()
                    .body(GoBenchmarkCompareResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to run benchmark compare on Go router: {}", ex.getMessage());
            throw new GoRouterUnavailableException("Go routing service is unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void clearCache() {
        try {
            restClient.post()
                    .uri("/cache/clear")
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Failed to clear Go cache: {}", ex.getMessage());
        }
    }
}
