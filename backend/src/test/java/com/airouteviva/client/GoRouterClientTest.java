package com.airouteviva.client;

import com.airouteviva.client.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GoRouterClientTest {

    private GoRouterClientImpl client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8082");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        client = new GoRouterClientImpl("http://localhost:8082", 2000, 5000);
        // Inject bound restClient using reflection
        Field field = GoRouterClientImpl.class.getDeclaredField("restClient");
        field.setAccessible(true);
        field.set(client, restClient);
    }

    @Test
    void testRouteRequest_DeserializesActualGoResponse() {
        String goJsonResponse = """
        {
          "requestId": "REQ-001",
          "selectedPath": "LIGHTWEIGHT_LOCAL",
          "reasoning": "Selected LIGHTWEIGHT_LOCAL because RAM pressure is high",
          "latencyMs": 85.0,
          "estimatedCost": 0.0,
          "resourceState": {
            "cpuUsage": 45.0,
            "ramUsage": 85.0,
            "ramTotalMb": 8192,
            "ramAvailableMb": 1020,
            "gpuAvailable": false,
            "networkStatus": "GOOD",
            "latencyMs": 24.0,
            "cpuPressure": "LOW",
            "ramPressure": "HIGH",
            "timestamp": "2026-10-05T05:12:37Z"
          },
          "scores": {
            "LIGHTWEIGHT_LOCAL": 82.4,
            "OFFLINE_FALLBACK": 61.2
          },
          "feasiblePaths": ["LIGHTWEIGHT_LOCAL", "OFFLINE_FALLBACK"],
          "cacheHit": false,
          "timestamp": "2026-10-05T05:12:37Z"
        }
        """;

        mockServer.expect(requestTo("http://localhost:8082/route"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(goJsonResponse, MediaType.APPLICATION_JSON));

        GoRouteRequest req = new GoRouteRequest("REQ-001", "ANSWER_EVALUATION", "HIGH", "HIGH", null, false);
        GoRouteResponse resp = client.routeRequest(req);

        assertNotNull(resp);
        assertEquals("REQ-001", resp.getRequestId());
        assertEquals("LIGHTWEIGHT_LOCAL", resp.getSelectedPath());
        assertEquals(85.0, resp.getLatencyMs());
        assertNotNull(resp.getResourceState());
        assertEquals(85.0, resp.getResourceState().getRamUsage());
        assertEquals("HIGH", resp.getResourceState().getRamPressure());
        assertEquals(82.4, resp.getScores().get("LIGHTWEIGHT_LOCAL"));
        assertFalse(resp.getCacheHit());
        mockServer.verify();
    }

    @Test
    void testGetCurrentResources_DeserializesGoResourceState() {
        String resourcesJson = """
        {
          "cpuUsage": 24.8,
          "ramUsage": 92.0,
          "ramTotalMb": 7518,
          "ramAvailableMb": 534,
          "gpuAvailable": true,
          "networkStatus": "GOOD",
          "latencyMs": 24.0,
          "cpuPressure": "LOW",
          "ramPressure": "HIGH",
          "timestamp": "2026-10-05T05:12:37Z"
        }
        """;

        mockServer.expect(requestTo("http://localhost:8082/resources"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesJson, MediaType.APPLICATION_JSON));

        GoResourceState state = client.getCurrentResources();

        assertNotNull(state);
        assertEquals(24.8, state.getCpuUsage());
        assertEquals(92.0, state.getRamUsage());
        assertEquals(534L, state.getRamAvailableMb());
        assertTrue(state.getGpuAvailable());
        assertEquals("GOOD", state.getNetworkStatus());
        mockServer.verify();
    }

    @Test
    void testGetMetrics_DeserializesMetrics() {
        String metricsJson = """
        {
          "totalRequests": 10,
          "localRequests": 7,
          "cacheHits": 2,
          "fallbackRequests": 0,
          "simulatedCloudRequests": 1,
          "failedRequests": 0,
          "averageLatency": 142.5,
          "peakCPU": 45.0,
          "peakRAM": 92.0,
          "resourceAdaptationEvents": 3,
          "averageEstimatedCost": 0.005,
          "totalEstimatedCost": 0.05
        }
        """;

        mockServer.expect(requestTo("http://localhost:8082/metrics"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(metricsJson, MediaType.APPLICATION_JSON));

        GoMetricsResponse metrics = client.getMetrics();

        assertNotNull(metrics);
        assertEquals(10L, metrics.getTotalRequests());
        assertEquals(2L, metrics.getCacheHits());
        assertEquals(3L, metrics.getResourceAdaptationEvents());
        mockServer.verify();
    }
}
