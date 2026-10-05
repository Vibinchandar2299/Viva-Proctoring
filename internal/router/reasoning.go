package router

import (
	"fmt"
	"strings"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

// GenerateReasoning produces human-readable explanation for a routing decision
func GenerateReasoning(
	selectedPath config.ExecutionPathType,
	workloadType config.WorkloadType,
	complexity config.ComplexityLevel,
	priority config.PriorityLevel,
	resState resources.ResourceState,
	scores map[config.ExecutionPathType]scoring.ScoreBreakdown,
	infeasiblePaths map[config.ExecutionPathType]string,
	cacheHit bool,
) string {
	// Scenario: Cache Hit
	if selectedPath == config.PathCache || cacheHit {
		return fmt.Sprintf(
			"Selected CACHE because an identical valid evaluation for workload %s is already available in memory, eliminating redundant compute and delivering immediate response.",
			workloadType,
		)
	}

	var parts []string

	// Main selection justification
	switch selectedPath {
	case config.PathHigherCapabilityLocal:
		parts = append(parts, fmt.Sprintf(
			"Selected HIGHER_CAPABILITY_LOCAL because host resources are healthy (RAM: %.1f%% used, %d MB available; CPU: %.1f%%), satisfying the %s complexity demand of %s with high-fidelity local inference",
			resState.RAMUsage, resState.RAMAvailableMb, resState.CPUUsage, complexity, workloadType,
		))

	case config.PathLightweightLocal:
		// Check why lightweight was preferred
		if reason, eliminated := infeasiblePaths[config.PathHigherCapabilityLocal]; eliminated {
			parts = append(parts, fmt.Sprintf(
				"Selected LIGHTWEIGHT_LOCAL because HIGHER_CAPABILITY_LOCAL was disqualified (%s) and %s priority demands deterministic local execution under available memory (%d MB)",
				reason, priority, resState.RAMAvailableMb,
			))
		} else {
			parts = append(parts, fmt.Sprintf(
				"Selected LIGHTWEIGHT_LOCAL to maximize throughput and minimize latency (%.1fms base) for %s workload under current system load (CPU: %.1f%%)",
				scores[selectedPath].LatencyScore, workloadType, resState.CPUUsage,
			))
		}

	case config.PathSimulatedCloud:
		parts = append(parts, fmt.Sprintf(
			"Selected SIMULATED_CLOUD because network connectivity is %s (latency: %.1fms), remaining quota is valid, offloading compute from local host",
			resState.NetworkStatus, resState.LatencyMs,
		))

	case config.PathOfflineFallback:
		parts = append(parts, fmt.Sprintf(
			"Selected OFFLINE_FALLBACK because local hardware resources are severely constrained and external network is unavailable, ensuring viva exam continuity without crash",
		))

	default:
		parts = append(parts, fmt.Sprintf("Selected %s based on multi-objective scoring (score: %.1f)", selectedPath, scores[selectedPath].TotalScore))
	}

	// Add constraint context for eliminated paths
	var eliminatedNotices []string
	if reason, ok := infeasiblePaths[config.PathSimulatedCloud]; ok && selectedPath != config.PathSimulatedCloud {
		eliminatedNotices = append(eliminatedNotices, fmt.Sprintf("Cloud path infeasible (%s)", reason))
	}
	if reason, ok := infeasiblePaths[config.PathHigherCapabilityLocal]; ok && selectedPath != config.PathHigherCapabilityLocal {
		eliminatedNotices = append(eliminatedNotices, fmt.Sprintf("Heavy local path infeasible (%s)", reason))
	}

	if len(eliminatedNotices) > 0 {
		parts = append(parts, fmt.Sprintf("[%s]", strings.Join(eliminatedNotices, "; ")))
	}

	return strings.Join(parts, ". ")
}
