package com.airouteviva.adapter;

import com.airouteviva.client.python.PythonAIClient;
import com.airouteviva.client.python.dto.VisionAnalysisResponse;
import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ExecutionAdapterUnitTest {

    @Autowired
    private ExecutionDispatcher dispatcher;

    @Autowired
    private LightweightLocalAdapter lightweightAdapter;

    @Autowired
    private HigherCapabilityLocalAdapter higherAdapter;

    @Autowired
    private CacheAdapter cacheAdapter;

    @Autowired
    private SimulatedCloudAdapter cloudAdapter;

    @Autowired
    private OfflineFallbackAdapter fallbackAdapter;

    @MockBean
    private PythonAIClient pythonAIClient;

    @Test
    void testExecutionDispatcher_PathMapping() {
        assertTrue(lightweightAdapter.supports(ExecutionPath.LIGHTWEIGHT_LOCAL));
        assertTrue(higherAdapter.supports(ExecutionPath.HIGHER_CAPABILITY_LOCAL));
        assertTrue(cacheAdapter.supports(ExecutionPath.CACHE));
        assertTrue(cloudAdapter.supports(ExecutionPath.SIMULATED_CLOUD));
        assertTrue(fallbackAdapter.supports(ExecutionPath.OFFLINE_FALLBACK));
    }

    @Test
    void testLightweightLocalAdapter_VisionExecution() {
        VisionAnalysisResponse mockVision = new VisionAnalysisResponse();
        mockVision.setFaceCount(1);
        mockVision.setVerified(true);
        mockVision.setConfidence(0.95);
        mockVision.setStatus("SUCCESS");

        when(pythonAIClient.analyzeVision(any(), any())).thenReturn(mockVision);

        Object result = dispatcher.dispatch(ExecutionPath.LIGHTWEIGHT_LOCAL, WorkloadType.FACE_DETECTION, Map.of());
        assertNotNull(result);
        assertTrue(result instanceof VisionAnalysisResponse);
        assertEquals(1, ((VisionAnalysisResponse) result).getFaceCount());
    }

    @Test
    void testCacheAdapter_Execution() {
        Object result = dispatcher.dispatch(ExecutionPath.CACHE, WorkloadType.ANSWER_EVALUATION, Map.of());
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(true, map.get("cached"));
        assertEquals("COMPLETED_CACHE_HIT", map.get("status"));
    }

    @Test
    void testSimulatedCloudAdapter_Execution() {
        Object result = dispatcher.dispatch(ExecutionPath.SIMULATED_CLOUD, WorkloadType.SPEECH_TO_TEXT, Map.of());
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("COMPLETED_SIMULATED_CLOUD", map.get("status"));
    }

    @Test
    void testOfflineFallbackAdapter_Execution() {
        Object result = dispatcher.dispatch(ExecutionPath.OFFLINE_FALLBACK, WorkloadType.OBJECT_DETECTION, Map.of());
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("COMPLETED_OFFLINE_FALLBACK", map.get("status"));
    }
}
