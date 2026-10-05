package router

import (
	"context"
	"fmt"
	"log/slog"
	"math"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/cache"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/feasibility"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/metrics"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/network"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/quota"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

// Engine is the central AI request routing engine for AI-ROUTE VIVA
type Engine struct {
	cfg         *config.Config
	resMonitor  *resources.ResourceMonitor
	feasFilter  *feasibility.Filter
	scorer      *scoring.Scorer
	registry    *paths.Registry
	coordinator *paths.ExecutionCoordinator
	cache       *cache.LRUCache
	networkMgr  *network.NetworkManager
	quotaMgr    *quota.QuotaManager
	tracker     *metrics.Tracker
}

// NewEngine creates and wires all components of the routing engine
func NewEngine(
	cfg *config.Config,
	resMonitor *resources.ResourceMonitor,
	feasFilter *feasibility.Filter,
	scorer *scoring.Scorer,
	registry *paths.Registry,
	coordinator *paths.ExecutionCoordinator,
	cache *cache.LRUCache,
	networkMgr *network.NetworkManager,
	quotaMgr *quota.QuotaManager,
	tracker *metrics.Tracker,
) *Engine {
	return &Engine{
		cfg:         cfg,
		resMonitor:  resMonitor,
		feasFilter:  feasFilter,
		scorer:      scorer,
		registry:    registry,
		coordinator: coordinator,
		cache:       cache,
		networkMgr:  networkMgr,
		quotaMgr:    quotaMgr,
		tracker:     tracker,
	}
}

// Route processes an incoming viva AI workload request through the entire routing pipeline
func (e *Engine) Route(ctx context.Context, req *RouteRequest) (*RouteResponse, error) {
	routeStart := time.Now()

	// 1. Request Validator
	if req.RequestID == "" {
		req.RequestID = fmt.Sprintf("REQ-%d", time.Now().UnixNano()%1000000)
	}
	if req.WorkloadType == "" {
		return nil, fmt.Errorf("workloadType is required")
	}

	// 2. Complexity & Priority resolution (Configuration-based defaults if missing)
	defaults, hasDefaults := e.cfg.WorkloadDefaultsMap[req.WorkloadType]
	if req.Complexity == "" {
		if hasDefaults {
			req.Complexity = defaults.Complexity
		} else {
			req.Complexity = config.ComplexityMedium
		}
	}
	if req.Priority == "" {
		if hasDefaults {
			req.Priority = defaults.Priority
		} else {
			req.Priority = config.PriorityMedium
		}
	}

	// 3. Resource Monitor: Sample live runtime resource state
	resState := e.resMonitor.GetCurrentState()

	// 4. Feasibility Filter: Prune invalid paths
	eval := e.feasFilter.Evaluate(req.WorkloadType, req.Payload, resState)

	// Guard: If all paths infeasible, force OFFLINE_FALLBACK to prevent system halt
	if len(eval.FeasiblePaths) == 0 {
		fallbackProfile, _ := e.registry.GetPath(config.PathOfflineFallback)
		eval.FeasiblePaths[config.PathOfflineFallback] = fallbackProfile
		slog.Warn("All standard paths infeasible, activated emergency OFFLINE_FALLBACK",
			"requestId", req.RequestID, "workload", req.WorkloadType)
	}

	// 5. Multi-Objective Scoring
	scores := e.scorer.ScorePaths(req.WorkloadType, req.Complexity, req.Priority, eval.FeasiblePaths, resState)

	// 6. Route Selector: Pick path with highest total score
	selectedPath := e.selectWinningPath(eval, scores)

	// 7. Reasoning Generator
	reasoning := GenerateReasoning(
		selectedPath,
		req.WorkloadType,
		req.Complexity,
		req.Priority,
		resState,
		scores,
		eval.InfeasiblePaths,
		eval.CacheHit,
	)

	// Determine cost
	var estCost float64
	if prof, ok := e.registry.GetPath(selectedPath); ok {
		estCost = prof.EstimatedCost
	}

	// 8. Execution Adapter: Coordinate actual execution
	var execResult *paths.ExecutionResult
	var measuredLatency float64

	if req.Execute || eval.CacheHit {
		execReq := &paths.RouteExecutionRequest{
			RequestID:    req.RequestID,
			WorkloadType: req.WorkloadType,
			Complexity:   req.Complexity,
			Priority:     req.Priority,
			Payload:      req.Payload,
		}
		var err error
		execResult, err = e.coordinator.Execute(ctx, execReq, selectedPath)
		if err != nil {
			slog.Error("Execution failed on selected path", "path", selectedPath, "error", err)
			e.tracker.RecordFailure()
			return nil, fmt.Errorf("execution on %s failed: %w", selectedPath, err)
		}
		measuredLatency = execResult.ExecutionMs
	} else {
		// Use estimated latency from scoring and routing overhead
		elapsedRoute := float64(time.Since(routeStart).Microseconds()) / 1000.0
		if prof, ok := e.registry.GetPath(selectedPath); ok {
			measuredLatency = math.Round((prof.BaseLatencyMs+elapsedRoute)*10) / 10
		} else {
			measuredLatency = elapsedRoute
		}
	}

	// Determine if this was an adaptation event (e.g. high pressure forced a shift away from higher capability)
	isAdaptation := false
	if _, heavyEliminated := eval.InfeasiblePaths[config.PathHigherCapabilityLocal]; heavyEliminated &&
		(selectedPath == config.PathLightweightLocal || selectedPath == config.PathOfflineFallback) {
		isAdaptation = true
	}

	// 9. Telemetry & Metrics recording
	timestamp := time.Now().UTC().Format(time.RFC3339)
	record := metrics.RoutingDecisionRecord{
		RequestID:       req.RequestID,
		WorkloadType:    req.WorkloadType,
		Complexity:      req.Complexity,
		Priority:        req.Priority,
		ResourceState:   resState,
		FeasiblePaths:   metrics.FeasibleKeys(eval.FeasiblePaths),
		InfeasiblePaths: eval.InfeasiblePaths,
		Scores:          scores,
		SelectedPath:    selectedPath,
		Reasoning:       reasoning,
		LatencyMs:       measuredLatency,
		EstimatedCost:   estCost,
		CacheHit:        eval.CacheHit || selectedPath == config.PathCache,
		IsAdaptation:    isAdaptation,
		Timestamp:       timestamp,
	}
	e.tracker.RecordDecision(record)

	// Structured Logging (excluding sensitive media payloads)
	slog.Info("Routing decision completed",
		"requestId", req.RequestID,
		"workload", req.WorkloadType,
		"selectedPath", selectedPath,
		"latencyMs", measuredLatency,
		"ramUsagePct", resState.RAMUsage,
		"cpuUsagePct", resState.CPUUsage,
		"cacheHit", eval.CacheHit,
	)

	return &RouteResponse{
		RequestID:       req.RequestID,
		SelectedPath:    selectedPath,
		Reasoning:       reasoning,
		LatencyMs:       measuredLatency,
		EstimatedCost:   estCost,
		ResourceState:   resState,
		Scores:          metrics.ScoreMap(scores),
		ScoreDetails:    scores,
		FeasiblePaths:   metrics.FeasibleKeys(eval.FeasiblePaths),
		InfeasiblePaths: eval.InfeasiblePaths,
		CacheHit:        eval.CacheHit || selectedPath == config.PathCache,
		ExecutionResult: execResult,
		Timestamp:       timestamp,
	}, nil
}

func (e *Engine) selectWinningPath(
	eval *feasibility.EvaluationResult,
	scores map[config.ExecutionPathType]scoring.ScoreBreakdown,
) config.ExecutionPathType {
	// If cache hit, CACHE always takes precedence
	if eval.CacheHit {
		return config.PathCache
	}

	var bestPath config.ExecutionPathType
	highestScore := -1.0

	for path, breakdown := range scores {
		if breakdown.TotalScore > highestScore {
			highestScore = breakdown.TotalScore
			bestPath = path
		}
	}

	if bestPath == "" {
		// Fallback
		return config.PathLightweightLocal
	}

	return bestPath
}

// GetTracker returns metrics tracker
func (e *Engine) GetTracker() *metrics.Tracker {
	return e.tracker
}

// GetResourceMonitor returns resource monitor
func (e *Engine) GetResourceMonitor() *resources.ResourceMonitor {
	return e.resMonitor
}

// GetNetworkManager returns network manager
func (e *Engine) GetNetworkManager() *network.NetworkManager {
	return e.networkMgr
}

// GetQuotaManager returns quota manager
func (e *Engine) GetQuotaManager() *quota.QuotaManager {
	return e.quotaMgr
}

// GetCache returns LRU cache
func (e *Engine) GetCache() *cache.LRUCache {
	return e.cache
}

// GetRegistry returns path registry
func (e *Engine) GetRegistry() *paths.Registry {
	return e.registry
}

// CompareWithBaseline benchmarks dynamic AI-ROUTE against FIXED_EXECUTION baseline under current conditions
func (e *Engine) CompareWithBaseline(ctx context.Context, req *RouteRequest, fixedPath config.ExecutionPathType) (*BaselineComparisonResult, error) {
	if fixedPath == "" {
		fixedPath = e.cfg.DefaultBaselinePath
	}

	// 1. Run Dynamic AI-ROUTE
	aiResp, err := e.Route(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("AI-Route failed: %w", err)
	}

	// 2. Evaluate Baseline Fixed Execution Path under the same conditions
	resState := e.resMonitor.GetCurrentState()
	eval := e.feasFilter.Evaluate(req.WorkloadType, req.Payload, resState)

	baselineSuccess := true
	baselineFailureReason := ""
	baselineLatency := 0.0
	baselineCost := 0.0

	if prof, ok := e.registry.GetPath(fixedPath); ok {
		baselineCost = prof.EstimatedCost
		baselineLatency = prof.BaseLatencyMs
	}

	// Check if fixed path is infeasible
	if reason, eliminated := eval.InfeasiblePaths[fixedPath]; eliminated {
		baselineSuccess = false
		baselineFailureReason = reason
		// Under baseline failure, system experiences timeout, OOM crash, or forced fallback
		baselineLatency = 5000.0 // Simulated timeout penalty
	}

	latencyDelta := math.Round((baselineLatency-aiResp.LatencyMs)*10) / 10
	costDelta := math.Round((baselineCost-aiResp.EstimatedCost)*1000) / 1000
	adaptation := aiResp.SelectedPath != fixedPath

	var analysis string
	if !baselineSuccess {
		analysis = fmt.Sprintf("Baseline fixed strategy FAILED (%s). AI-ROUTE dynamically avoided failure by adapting to %s.",
			baselineFailureReason, aiResp.SelectedPath)
	} else if aiResp.CacheHit {
		analysis = fmt.Sprintf("AI-ROUTE achieved near-zero latency (%.1fms vs %.1fms) through deterministic caching.",
			aiResp.LatencyMs, baselineLatency)
	} else if adaptation {
		analysis = fmt.Sprintf("AI-ROUTE adapted route to %s, optimizing for current resource envelope (RAM: %.1f%%, CPU: %.1f%%).",
			aiResp.SelectedPath, resState.RAMUsage, resState.CPUUsage)
	} else {
		analysis = "Both AI-ROUTE and Baseline selected the same optimal path under healthy resource conditions."
	}

	return &BaselineComparisonResult{
		WorkloadType:          req.WorkloadType,
		Complexity:            req.Complexity,
		Priority:              req.Priority,
		SimulatedCondition:    fmt.Sprintf("RAM: %.1f%%, CPU: %.1f%%, Net: %s", resState.RAMUsage, resState.CPUUsage, resState.NetworkStatus),
		AIRouteSelectedPath:   aiResp.SelectedPath,
		AIRouteLatencyMs:      aiResp.LatencyMs,
		AIRouteCost:           aiResp.EstimatedCost,
		AIRouteSuccess:        true,
		AIRouteReasoning:      aiResp.Reasoning,
		BaselineFixedPath:     fixedPath,
		BaselineLatencyMs:     baselineLatency,
		BaselineCost:          baselineCost,
		BaselineSuccess:       baselineSuccess,
		BaselineFailureReason: baselineFailureReason,
		LatencyDeltaMs:        latencyDelta,
		CostDelta:             costDelta,
		AdaptationObserved:    adaptation,
		Analysis:              analysis,
	}, nil
}
