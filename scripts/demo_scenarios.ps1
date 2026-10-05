# AI-ROUTE VIVA - 5 Primary Demonstration Scenarios
# HackWithAMYPO 2026 | PS1 Engine
$ErrorActionPreference = "Stop"

$BaseUrl = "http://localhost:8082"

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "   AI-ROUTE VIVA: Demonstration of 5 Primary Scenarios & Baseline Comparison" -ForegroundColor Cyan
Write-Host "   Problem Statement: PS1 (Integrated with PS5 Viva Proctoring)" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

function Print-Json($obj) {
    return ($obj | ConvertTo-Json -Depth 5)
}

# 0. Health & Live Resources Check
Write-Host "`n[STEP 0] Checking Service Health & Real Host Resources..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get
Write-Host "Service: $($health.service) | Status: $($health.status) | Mode: $($health.mode)" -ForegroundColor Green

$resources = Invoke-RestMethod -Uri "$BaseUrl/resources" -Method Get
Write-Host "Live Hardware Metrics:" -ForegroundColor Gray
Write-Host "  RAM: $($resources.ramUsage)% ($($resources.ramAvailableMb) MB Free / $($resources.ramTotalMb) MB Total)"
Write-Host "  CPU: $($resources.cpuUsage)% (Pressure: $($resources.cpuPressure))"
Write-Host "  GPU Available: $($resources.gpuAvailable)"
Write-Host "  Network: $($resources.networkStatus) (Latency: $($resources.latencyMs)ms)"

# Clear cache before demo
Invoke-RestMethod -Uri "$BaseUrl/cache/clear" -Method Post | Out-Null

# SCENARIO 1: Normal Resources
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "SCENARIO 1: Normal resources -> Higher-capability local execution when feasible" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
# Set healthy simulated state (RAM 40%, CPU 25%)
$simBody = @{
    mode = "CUSTOM"
    ramUsage = 40.0
    ramAvailableMb = 4900
    cpuUsage = 25.0
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body $simBody -ContentType "application/json" | Out-Null

$req1 = @{
    requestId = "DEMO-SCENARIO-1"
    workloadType = "ANSWER_EVALUATION"
    complexity = "HIGH"
    priority = "HIGH"
    payload = @{
        question = "Explain ACID properties in relational database systems."
        studentAnswer = "Atomicity ensures all-or-nothing transactions. Consistency preserves constraints."
    }
} | ConvertTo-Json

$resp1 = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $req1 -ContentType "application/json"
Write-Host "Selected Path : $($resp1.selectedPath)" -ForegroundColor Green
Write-Host "Reasoning     : $($resp1.reasoning)" -ForegroundColor Gray
Write-Host "Latency (Est) : $($resp1.latencyMs) ms"
Write-Host "Scores        : $(Print-Json $resp1.scores)"

# SCENARIO 2: High Resource Pressure
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "SCENARIO 2: High resource pressure -> Higher-capability path becomes infeasible -> Lightweight local selected" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
# Simulate High RAM pressure (88% used, only 980 MB free < 2048 MB required)
$simBody2 = @{
    mode = "HIGH_RAM"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body $simBody2 -ContentType "application/json" | Out-Null

$req2 = @{
    requestId = "DEMO-SCENARIO-2"
    workloadType = "SPEECH_TO_TEXT"
    complexity = "MEDIUM"
    priority = "HIGH"
    payload = @{
        audioDurationSec = 4.2
    }
} | ConvertTo-Json

$resp2 = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $req2 -ContentType "application/json"
Write-Host "Selected Path : $($resp2.selectedPath)" -ForegroundColor Green
Write-Host "Reasoning     : $($resp2.reasoning)" -ForegroundColor Gray
Write-Host "Feasible      : $(Print-Json $resp2.feasiblePaths)"
Write-Host "Infeasible    : $(Print-Json $resp2.infeasiblePaths)"

# SCENARIO 3: Network Failure
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "SCENARIO 3: Network failure -> Simulated cloud path becomes infeasible -> Local path selected" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
$simBody3 = @{
    mode = "OFFLINE_NET"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body $simBody3 -ContentType "application/json" | Out-Null

$req3 = @{
    requestId = "DEMO-SCENARIO-3"
    workloadType = "OBJECT_DETECTION"
    complexity = "MEDIUM"
    priority = "MEDIUM"
    payload = @{
        frameId = 1042
    }
} | ConvertTo-Json

$resp3 = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $req3 -ContentType "application/json"
Write-Host "Selected Path : $($resp3.selectedPath)" -ForegroundColor Green
Write-Host "Reasoning     : $($resp3.reasoning)" -ForegroundColor Gray
Write-Host "Infeasible Cloud Reason: $($resp3.infeasiblePaths.SIMULATED_CLOUD)" -ForegroundColor Red

# SCENARIO 4: Repeated Request (Cache Demo)
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "SCENARIO 4: Repeated request -> Deterministic cache hit with near-zero latency" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
# Reset to normal network & resources
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body '{"mode":"RESET"}' -ContentType "application/json" | Out-Null

$cachePayload = @{
    examId = "EXAM-2026-CS101"
    question = "Define virtual memory and paging."
    answer = "Virtual memory maps virtual addresses used by programs to physical addresses in RAM."
}

$req4A = @{
    requestId = "REQ-CACHE-RUN1"
    workloadType = "ANSWER_EVALUATION"
    complexity = "HIGH"
    priority = "HIGH"
    payload = $cachePayload
    execute = $true
} | ConvertTo-Json

Write-Host "Sending First Request (Cold miss -> Model execution)..." -ForegroundColor Yellow
$resp4A = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $req4A -ContentType "application/json"
Write-Host "  Path: $($resp4A.selectedPath) | CacheHit: $($resp4A.cacheHit) | Measured Latency: $($resp4A.latencyMs) ms" -ForegroundColor White

Write-Host "Sending Second Identical Request (Warm -> In-memory cache)..." -ForegroundColor Yellow
$req4B = @{
    requestId = "REQ-CACHE-RUN2"
    workloadType = "ANSWER_EVALUATION"
    complexity = "HIGH"
    priority = "HIGH"
    payload = $cachePayload
    execute = $true
} | ConvertTo-Json

$resp4B = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $req4B -ContentType "application/json"
Write-Host "  Path: $($resp4B.selectedPath) | CacheHit: $($resp4B.cacheHit) | Measured Latency: $($resp4B.latencyMs) ms" -ForegroundColor Green
Write-Host "  Reasoning: $($resp4B.reasoning)" -ForegroundColor Gray

# SCENARIO 5: Resource Recovery
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "SCENARIO 5: Resource recovery -> Router detects improved resources -> Higher-capability feasible again" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

Write-Host "Applying Resource Stress..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body '{"mode":"HIGH_RAM"}' -ContentType "application/json" | Out-Null
$reqStress = @{
    requestId = "DEMO-STRESS"
    workloadType = "FOLLOW_UP_GENERATION"
    complexity = "HIGH"
    priority = "MEDIUM"
} | ConvertTo-Json
$respStress = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $reqStress -ContentType "application/json"
Write-Host "  Under Stress -> Selected: $($respStress.selectedPath)" -ForegroundColor Red

Write-Host "Simulating Resource Recovery (Releasing RAM)..." -ForegroundColor Yellow
$simRecover = @{
    mode = "CUSTOM"
    ramUsage = 38.0
    ramAvailableMb = 5100
    cpuUsage = 22.0
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body $simRecover -ContentType "application/json" | Out-Null

$reqRecover = @{
    requestId = "DEMO-RECOVER"
    workloadType = "FOLLOW_UP_GENERATION"
    complexity = "HIGH"
    priority = "MEDIUM"
} | ConvertTo-Json
$respRecover = Invoke-RestMethod -Uri "$BaseUrl/route" -Method Post -Body $reqRecover -ContentType "application/json"
Write-Host "  After Recovery -> Selected: $($respRecover.selectedPath)" -ForegroundColor Green
Write-Host "  Reasoning: $($respRecover.reasoning)" -ForegroundColor Gray

# BASELINE COMPARISON
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "BASELINE BENCHMARK: AI-ROUTE Dynamic vs FIXED_EXECUTION Strategy" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
# Put system under stress to show contrast
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body '{"mode":"HIGH_RAM"}' -ContentType "application/json" | Out-Null

$benchReq = @{
    requestId = "BENCH-001"
    workloadType = "ANSWER_EVALUATION"
    complexity = "HIGH"
    priority = "HIGH"
} | ConvertTo-Json

$benchResp = Invoke-RestMethod -Uri "$BaseUrl/benchmark/compare?fixedPath=HIGHER_CAPABILITY_LOCAL" -Method Post -Body $benchReq -ContentType "application/json"
Write-Host "Condition           : $($benchResp.simulatedCondition)"
Write-Host "AI-ROUTE Path       : $($benchResp.aiRouteSelectedPath) (Success: $($benchResp.aiRouteSuccess), Latency: $($benchResp.aiRouteLatencyMs)ms)" -ForegroundColor Green
Write-Host "Baseline Fixed Path : $($benchResp.baselineFixedPath) (Success: $($benchResp.baselineSuccess), Latency: $($benchResp.baselineLatencyMs)ms)" -ForegroundColor Red
if ($benchResp.baselineFailureReason) {
    Write-Host "Baseline Failure    : $($benchResp.baselineFailureReason)" -ForegroundColor Red
}
Write-Host "Analysis            : $($benchResp.analysis)" -ForegroundColor Yellow

# METRICS SUMMARY
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "ROUTING METRICS SUMMARY" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
$metrics = Invoke-RestMethod -Uri "$BaseUrl/metrics" -Method Get
Write-Host (Print-Json $metrics)

# Reset simulation state
Invoke-RestMethod -Uri "$BaseUrl/simulate/condition" -Method Post -Body '{"mode":"RESET"}' -ContentType "application/json" | Out-Null
Write-Host "`nAll 5 Scenarios Completed Successfully!" -ForegroundColor Green
