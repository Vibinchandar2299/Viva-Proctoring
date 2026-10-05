package com.airouteviva.adapter;

import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SimulatedCloudAdapter implements ExecutionAdapter {

    private static final Logger log = LoggerFactory.getLogger(SimulatedCloudAdapter.class);

    @Override
    public boolean supports(ExecutionPath path) {
        return path == ExecutionPath.SIMULATED_CLOUD;
    }

    @Override
    public Object execute(WorkloadType workloadType, Object payload) {
        log.info("Executing via SIMULATED_CLOUD adapter: workload={}", workloadType);
        return Map.of(
                "status", "COMPLETED_SIMULATED_CLOUD",
                "workload", workloadType,
                "remoteWorker", "simulated-cloud-benchmark-node",
                "costDeducted", 0.05
        );
    }
}
