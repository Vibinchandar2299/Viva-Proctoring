package com.airouteviva.adapter;

import com.airouteviva.entity.enums.ExecutionPath;
import com.airouteviva.entity.enums.WorkloadType;

public interface ExecutionAdapter {

    boolean supports(ExecutionPath path);

    Object execute(WorkloadType workloadType, Object payload);
}
