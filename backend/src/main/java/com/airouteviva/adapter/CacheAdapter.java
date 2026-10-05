package com.airouteviva.adapter;

import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheAdapter implements ExecutionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CacheAdapter.class);

    @Override
    public boolean supports(ExecutionPath path) {
        return path == ExecutionPath.CACHE;
    }

    @Override
    public Object execute(WorkloadType workloadType, Object payload) {
        log.info("Executing via CACHE adapter: workload={}", workloadType);
        return Map.of(
                "status", "COMPLETED_CACHE_HIT",
                "workload", workloadType,
                "cached", true,
                "latencyMs", 1.5,
                "summary", "Retrieved directly from Go in-process deterministic LRU cache"
        );
    }
}
