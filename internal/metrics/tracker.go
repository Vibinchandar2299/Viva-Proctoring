package metrics

import (
	"math"
	"sync"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

// RoutingDecisionRecord represents a persisted routing decision for telemetry and history
type RoutingDecisionRecord struct {
	RequestID       string                                                  `json:"requestId"`
	WorkloadType    config.WorkloadType                                     `json:"workloadType"`
	Complexity      config.ComplexityLevel                                  `json:"complexity"`
	Priority        config.PriorityLevel                                    `json:"priority"`
	ResourceState   resources.ResourceState                                 `json:"resourceState"`
	FeasiblePaths   []config.ExecutionPathType                              `json:"feasiblePaths"`
	InfeasiblePaths map[config.ExecutionPathType]string                    `json:"infeasiblePaths,omitempty"`
	Scores          map[config.ExecutionPathType]scoring.ScoreBreakdown     `json:"scores"`
	SelectedPath    config.ExecutionPathType                                `json:"selectedPath"`
	Reasoning       string                                                  `json:"reasoning"`
	LatencyMs       float64                                                 `json:"latencyMs"`
	EstimatedCost   float64                                                 `json:"estimatedCost"`
	CacheHit        bool                                                    `json:"cacheHit"`
	IsAdaptation    bool                                                    `json:"isAdaptation"`
	Timestamp       string                                                  `json:"timestamp"`
}

// RouterMetrics stores aggregated runtime routing statistics
type RouterMetrics struct {
	TotalRequests            uint64  `json:"totalRequests"`
	LocalRequests            uint64  `json:"localRequests"`
	CacheHits                uint64  `json:"cacheHits"`
	FallbackRequests         uint64  `json:"fallbackRequests"`
	SimulatedCloudRequests   uint64  `json:"simulatedCloudRequests"`
	FailedRequests           uint64  `json:"failedRequests"`
	AverageLatency           float64 `json:"averageLatency"`
	PeakCPU                  float64 `json:"peakCPU"`
	PeakRAM                  float64 `json:"peakRAM"`
	ResourceAdaptationEvents uint64  `json:"resourceAdaptationEvents"`
	AverageEstimatedCost     float64 `json:"averageEstimatedCost"`
	TotalEstimatedCost       float64 `json:"totalEstimatedCost"`
}

// Tracker maintains atomic metrics and in-memory history
type Tracker struct {
	mu           sync.RWMutex
	metrics      RouterMetrics
	totalLatency float64
	history      []RoutingDecisionRecord
	maxHistory   int
}

// NewTracker creates a new metrics and history tracker
func NewTracker(maxHistory int) *Tracker {
	if maxHistory <= 0 {
		maxHistory = 500
	}
	return &Tracker{
		history:    make([]RoutingDecisionRecord, 0, maxHistory),
		maxHistory: maxHistory,
	}
}

// RecordDecision logs a completed routing decision into metrics and history
func (t *Tracker) RecordDecision(record RoutingDecisionRecord) {
	t.mu.Lock()
	defer t.mu.Unlock()

	t.metrics.TotalRequests++
	t.totalLatency += record.LatencyMs
	t.metrics.AverageLatency = math.Round((t.totalLatency/float64(t.metrics.TotalRequests))*100) / 100

	t.metrics.TotalEstimatedCost += record.EstimatedCost
	t.metrics.AverageEstimatedCost = math.Round((t.metrics.TotalEstimatedCost/float64(t.metrics.TotalRequests))*1000) / 1000

	if record.ResourceState.CPUUsage > t.metrics.PeakCPU {
		t.metrics.PeakCPU = math.Round(record.ResourceState.CPUUsage*10) / 10
	}
	if record.ResourceState.RAMUsage > t.metrics.PeakRAM {
		t.metrics.PeakRAM = math.Round(record.ResourceState.RAMUsage*10) / 10
	}

	if record.CacheHit || record.SelectedPath == config.PathCache {
		t.metrics.CacheHits++
	} else if record.SelectedPath == config.PathLightweightLocal || record.SelectedPath == config.PathHigherCapabilityLocal {
		t.metrics.LocalRequests++
	} else if record.SelectedPath == config.PathSimulatedCloud {
		t.metrics.SimulatedCloudRequests++
	} else if record.SelectedPath == config.PathOfflineFallback {
		t.metrics.FallbackRequests++
	}

	if record.IsAdaptation {
		t.metrics.ResourceAdaptationEvents++
	}

	// Add to bounded history slice
	if len(t.history) >= t.maxHistory {
		t.history = t.history[1:]
	}
	t.history = append(t.history, record)
}

// RecordFailure increments failed requests
func (t *Tracker) RecordFailure() {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.metrics.FailedRequests++
}

// GetMetrics returns snapshot of routing metrics
func (t *Tracker) GetMetrics() RouterMetrics {
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.metrics
}

// GetHistory returns filtered history records (newest first)
func (t *Tracker) GetHistory(limit int, workloadFilter string) []RoutingDecisionRecord {
	t.mu.RLock()
	defer t.mu.RUnlock()

	if limit <= 0 || limit > len(t.history) {
		limit = len(t.history)
	}

	result := make([]RoutingDecisionRecord, 0, limit)
	// Iterate in reverse for newest first
	for i := len(t.history) - 1; i >= 0 && len(result) < limit; i-- {
		item := t.history[i]
		if workloadFilter != "" && string(item.WorkloadType) != workloadFilter {
			continue
		}
		result = append(result, item)
	}

	return result
}

// Reset clears all counters and history
func (t *Tracker) Reset() {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.metrics = RouterMetrics{}
	t.totalLatency = 0
	t.history = make([]RoutingDecisionRecord, 0, t.maxHistory)
}

// FeasibleKeys extracts path slice from map for serialization
func FeasibleKeys(m map[config.ExecutionPathType]paths.ExecutionPathProfile) []config.ExecutionPathType {
	keys := make([]config.ExecutionPathType, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}
	return keys
}

// ScoreMap extracts score floats for summary response
func ScoreMap(scores map[config.ExecutionPathType]scoring.ScoreBreakdown) map[string]float64 {
	res := make(map[string]float64, len(scores))
	for k, v := range scores {
		res[string(k)] = v.TotalScore
	}
	return res
}
