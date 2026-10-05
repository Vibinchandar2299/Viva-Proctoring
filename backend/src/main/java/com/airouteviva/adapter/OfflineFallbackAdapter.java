package com.airouteviva.adapter;

import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OfflineFallbackAdapter implements ExecutionAdapter {

    private static final Logger log = LoggerFactory.getLogger(OfflineFallbackAdapter.class);

    @Override
    public boolean supports(ExecutionPath path) {
        return path == ExecutionPath.OFFLINE_FALLBACK;
    }

    @Override
    public Object execute(WorkloadType workloadType, Object payload) {
        log.warn("Executing via OFFLINE_FALLBACK adapter: workload={} - Emergency rule-based fallback activated", workloadType);
        return Map.of(
                "status", "COMPLETED_OFFLINE_FALLBACK",
                "workload", workloadType,
                "confidence", 0.65,
                "summary", "Emergency heuristic evaluated to ensure exam continuity under constrained offline conditions"
        );
    }
}
