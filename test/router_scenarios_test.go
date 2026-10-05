package test

import (
	"context"
	"testing"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/cache"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/feasibility"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/metrics"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/network"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/quota"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/router"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

// setupTestEngine helper to initialize router with mocked environment
func setupTestEngine(t *testing.T) (*router.Engine, *resources.ResourceMonitor, *network.NetworkManager, *quota.QuotaManager, *cache.LRUCache) {
	cfg := config.DefaultConfig()
	cfg.MockExecution = true

	lru := cache.NewLRUCache(100, 1*time.Hour)
	qm := quota.NewQuotaManager(50, 0.05)
	nm := network.NewNetworkManager()
	reg := paths.NewPathRegistry(cfg)
	resMon := resources.NewResourceMonitor(cfg)
	feas := feasibility.NewFilter(cfg, reg, lru, nm, qm)
	sc := scoring.NewScorer(cfg)
	coord := paths.NewExecutionCoordinator(cfg, lru, qm, nm)
	tracker := metrics.NewTracker(100)

	engine := router.NewEngine(cfg, resMon, feas, sc, reg, coord, lru, nm, qm, tracker)
	return engine, resMon, nm, qm, lru
}

// -------------------------------------------------------------
// PRIMARY PS1 DEMONSTRATION SCENARIOS (1 to 5)
// -------------------------------------------------------------

// SCENARIO 1: Normal resources -> Higher-capability local execution when feasible
func TestScenario1_NormalResources_HigherCapabilitySelected(t *testing.T) {
	engine, resMon, _, _, _ := setupTestEngine(t)

	// Healthy college lab PC: 40% RAM usage, 4900 MB free, 25% CPU
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       25.0,
		RAMUsage:       40.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 4900,
		GPUAvailable:   false,
		NetworkStatus:  config.NetworkGood,
		LatencyMs:      20.0,
	})

	req := &router.RouteRequest{
		RequestID:    "TEST-SCENARIO-1",
		WorkloadType: config.WorkloadAnswerEvaluation, // Complex viva task
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityHigh,
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if resp.SelectedPath != config.PathHigherCapabilityLocal {
		t.Errorf("Expected %s under healthy resources, got %s", config.PathHigherCapabilityLocal, resp.SelectedPath)
	}

	if resp.Reasoning == "" {
		t.Error("Expected human-readable reasoning to be generated")
	}
}

// SCENARIO 2: High resource pressure -> Higher-capability becomes infeasible -> Lightweight local selected
func TestScenario2_HighResourcePressure_LightweightSelected(t *testing.T) {
	engine, resMon, _, _, _ := setupTestEngine(t)

	// Heavy RAM pressure: 87% used, only 1050 MB available (< 2048 MB required for heavy model)
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       55.0,
		RAMUsage:       87.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 1050,
		GPUAvailable:   false,
		NetworkStatus:  config.NetworkGood,
		LatencyMs:      20.0,
	})

	req := &router.RouteRequest{
		RequestID:    "TEST-SCENARIO-2",
		WorkloadType: config.WorkloadSpeechToText,
		Complexity:   config.ComplexityMedium,
		Priority:     config.PriorityHigh,
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if resp.SelectedPath != config.PathLightweightLocal {
		t.Errorf("Expected %s under high RAM pressure, got %s", config.PathLightweightLocal, resp.SelectedPath)
	}

	// Verify HIGHER_CAPABILITY_LOCAL was marked infeasible
	if _, ok := resp.InfeasiblePaths[config.PathHigherCapabilityLocal]; !ok {
		t.Errorf("Expected HIGHER_CAPABILITY_LOCAL to be infeasible under 87%% RAM")
	}
}

// SCENARIO 3: Network failure -> Cloud path becomes infeasible -> Local/offline selected
func TestScenario3_NetworkFailure_CloudInfeasible(t *testing.T) {
	engine, _, netMgr, _, _ := setupTestEngine(t)

	// Set network to OFFLINE
	netMgr.SetStatus(config.NetworkOffline)

	req := &router.RouteRequest{
		RequestID:    "TEST-SCENARIO-3",
		WorkloadType: config.WorkloadObjectDetection,
		Complexity:   config.ComplexityMedium,
		Priority:     config.PriorityMedium,
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if resp.SelectedPath == config.PathSimulatedCloud {
		t.Errorf("Router must NEVER select SIMULATED_CLOUD when network is offline")
	}

	// Verify SIMULATED_CLOUD is in infeasible paths with proper reason
	reason, ok := resp.InfeasiblePaths[config.PathSimulatedCloud]
	if !ok {
		t.Errorf("Expected SIMULATED_CLOUD to be flagged as infeasible")
	}
	if reason == "" {
		t.Errorf("Expected descriptive rejection reason for offline network")
	}
}

// SCENARIO 4: Repeated request -> Deterministic cache hit with immediate response
func TestScenario4_RepeatedRequest_CacheHit(t *testing.T) {
	engine, _, _, _, _ := setupTestEngine(t)

	reqPayload := map[string]interface{}{
		"studentId": "STU-9921",
		"answer":    "Polymorphism allows objects to take multiple forms through method overriding.",
	}

	req1 := &router.RouteRequest{
		RequestID:    "REQ-VIVA-01",
		WorkloadType: config.WorkloadAnswerEvaluation,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityHigh,
		Payload:      reqPayload,
		Execute:      true,
	}

	// 1st request -> executes and caches result
	resp1, err := engine.Route(context.Background(), req1)
	if err != nil {
		t.Fatalf("First request failed: %v", err)
	}
	if resp1.CacheHit {
		t.Errorf("First request should be cache miss")
	}

	// 2nd identical request -> cache hit
	req2 := &router.RouteRequest{
		RequestID:    "REQ-VIVA-02",
		WorkloadType: config.WorkloadAnswerEvaluation,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityHigh,
		Payload:      reqPayload,
		Execute:      true,
	}

	resp2, err := engine.Route(context.Background(), req2)
	if err != nil {
		t.Fatalf("Second request failed: %v", err)
	}

	if !resp2.CacheHit || resp2.SelectedPath != config.PathCache {
		t.Errorf("Expected CACHE path on repeated request, got %s (cacheHit: %v)", resp2.SelectedPath, resp2.CacheHit)
	}

	// Cache latency should be drastically lower
	if resp2.LatencyMs > resp1.LatencyMs {
		t.Errorf("Cache latency (%.2fms) should be lower than first run (%.2fms)", resp2.LatencyMs, resp1.LatencyMs)
	}
}

// SCENARIO 5: Resource recovery -> Router detects improved resources -> Higher-capability feasible again
func TestScenario5_ResourceRecovery_HigherCapabilityRestored(t *testing.T) {
	engine, resMon, _, _, _ := setupTestEngine(t)

	// Step 1: Stress state -> Lightweight selected
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       85.0,
		RAMUsage:       86.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 1100,
	})

	reqStress := &router.RouteRequest{
		RequestID:    "TEST-STRESS",
		WorkloadType: config.WorkloadFollowUpGeneration,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityMedium,
	}
	respStress, err := engine.Route(context.Background(), reqStress)
	if err != nil {
		t.Fatalf("Stress route failed: %v", err)
	}
	if respStress.SelectedPath == config.PathHigherCapabilityLocal {
		t.Errorf("Heavy path should not be selected under stress")
	}

	// Step 2: System recovers -> Clear overrides or set healthy state
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       20.0,
		RAMUsage:       42.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 4750,
	})

	reqRecovered := &router.RouteRequest{
		RequestID:    "TEST-RECOVERED",
		WorkloadType: config.WorkloadFollowUpGeneration,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityMedium,
	}
	respRecovered, err := engine.Route(context.Background(), reqRecovered)
	if err != nil {
		t.Fatalf("Recovered route failed: %v", err)
	}

	if respRecovered.SelectedPath != config.PathHigherCapabilityLocal {
		t.Errorf("Expected %s after resource recovery, got %s", config.PathHigherCapabilityLocal, respRecovered.SelectedPath)
	}
}

// -------------------------------------------------------------
// COMPREHENSIVE EDGE-CASE & UNIT TESTS
// -------------------------------------------------------------

func TestHighCPUPressure(t *testing.T) {
	engine, resMon, _, _, _ := setupTestEngine(t)

	// CPU saturated at 94%
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       94.0,
		RAMUsage:       50.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 4096,
	})

	req := &router.RouteRequest{
		RequestID:    "TEST-HIGH-CPU",
		WorkloadType: config.WorkloadAnswerEvaluation,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityHigh,
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Route failed: %v", err)
	}

	if resp.SelectedPath == config.PathHigherCapabilityLocal {
		t.Errorf("Higher capability path should be disqualified under 94%% CPU")
	}
}

func TestCloudQuotaExhaustion(t *testing.T) {
	engine, _, _, quotaMgr, _ := setupTestEngine(t)

	// Exhaust quota
	quotaMgr.SetRemaining(0)

	req := &router.RouteRequest{
		RequestID:    "TEST-QUOTA",
		WorkloadType: config.WorkloadAnswerEvaluation,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityLow,
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Route failed: %v", err)
	}

	if resp.SelectedPath == config.PathSimulatedCloud {
		t.Errorf("Router must not select cloud when quota is 0")
	}
	if _, ok := resp.InfeasiblePaths[config.PathSimulatedCloud]; !ok {
		t.Errorf("SIMULATED_CLOUD should be listed in InfeasiblePaths when quota is 0")
	}
}

func TestNoComplexitySupplied(t *testing.T) {
	engine, _, _, _, _ := setupTestEngine(t)

	// Request without complexity
	req := &router.RouteRequest{
		RequestID:    "TEST-NO-COMPLEXITY",
		WorkloadType: config.WorkloadAnswerEvaluation,
		// Complexity left empty
	}

	resp, err := engine.Route(context.Background(), req)
	if err != nil {
		t.Fatalf("Expected router to handle missing complexity gracefully: %v", err)
	}

	if resp.SelectedPath == "" {
		t.Error("Selected path should not be empty")
	}
}

func TestPriorityRules_HighVsLow(t *testing.T) {
	engine, _, _, _, _ := setupTestEngine(t)

	reqHigh := &router.RouteRequest{
		RequestID:    "TEST-PRIO-HIGH",
		WorkloadType: config.WorkloadSpeechToText,
		Priority:     config.PriorityHigh,
	}
	respHigh, err := engine.Route(context.Background(), reqHigh)
	if err != nil {
		t.Fatalf("High priority route failed: %v", err)
	}

	reqLow := &router.RouteRequest{
		RequestID:    "TEST-PRIO-LOW",
		WorkloadType: config.WorkloadSpeechToText,
		Priority:     config.PriorityLow,
	}
	respLow, err := engine.Route(context.Background(), reqLow)
	if err != nil {
		t.Fatalf("Low priority route failed: %v", err)
	}

	// Verify scoring weight differences applied
	highScores := respHigh.ScoreDetails[respHigh.SelectedPath]
	lowScores := respLow.ScoreDetails[respLow.SelectedPath]

	if highScores.AppliedWeights.WeightLatency <= lowScores.AppliedWeights.WeightLatency {
		t.Errorf("High priority should assign higher weight to latency (got %v vs %v)",
			highScores.AppliedWeights.WeightLatency, lowScores.AppliedWeights.WeightLatency)
	}
}

func TestBaselineComparison(t *testing.T) {
	engine, resMon, _, _, _ := setupTestEngine(t)

	// Under high pressure, fixed HIGHER_CAPABILITY_LOCAL should fail, while AI-ROUTE adapts
	resMon.SetSimulationOverride(&resources.ResourceState{
		CPUUsage:       85.0,
		RAMUsage:       91.0,
		RAMTotalMb:     8192,
		RAMAvailableMb: 720,
	})

	req := &router.RouteRequest{
		RequestID:    "BENCHMARK-01",
		WorkloadType: config.WorkloadAnswerEvaluation,
		Complexity:   config.ComplexityHigh,
		Priority:     config.PriorityHigh,
	}

	comp, err := engine.CompareWithBaseline(context.Background(), req, config.PathHigherCapabilityLocal)
	if err != nil {
		t.Fatalf("Baseline comparison failed: %v", err)
	}

	if comp.BaselineSuccess {
		t.Errorf("Baseline fixed execution should fail under 91%% RAM pressure")
	}
	if !comp.AIRouteSuccess {
		t.Errorf("AI-ROUTE should succeed by adapting path")
	}
	if !comp.AdaptationObserved {
		t.Errorf("Expected adaptation to be recorded")
	}
}
