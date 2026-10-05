package com.airouteviva.adapter;

import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionDispatcher {

    private final List<ExecutionAdapter> adapters;

    public ExecutionDispatcher(List<ExecutionAdapter> adapters) {
        this.adapters = adapters;
    }

    public Object dispatch(ExecutionPath path, WorkloadType workloadType, Object payload) {
        for (ExecutionAdapter adapter : adapters) {
            if (adapter.supports(path)) {
                return adapter.execute(workloadType, payload);
            }
        }
        throw new IllegalStateException("No execution adapter registered for path: " + path);
    }
}
