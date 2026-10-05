package feasibility

import (
	"fmt"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/cache"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/network"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/quota"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
)

// EvaluationResult captures the outcome of feasibility filtering
type EvaluationResult struct {
	FeasiblePaths   map[config.ExecutionPathType]paths.ExecutionPathProfile `json:"feasiblePaths"`
	InfeasiblePaths map[config.ExecutionPathType]string                    `json:"infeasiblePaths"`
	CacheHit        bool                                                   `json:"cacheHit"`
	CachedResult    interface{}                                            `json:"cachedResult,omitempty"`
}

// Filter evaluates hardware and operational constraints to prune invalid paths
type Filter struct {
	cfg        *config.Config
	registry   *paths.Registry
	cache      *cache.LRUCache
	networkMgr *network.NetworkManager
	quotaMgr   *quota.QuotaManager
}

// NewFilter constructs the feasibility filter
func NewFilter(
	cfg *config.Config,
	registry *paths.Registry,
	cache *cache.LRUCache,
	networkMgr *network.NetworkManager,
	quotaMgr *quota.QuotaManager,
) *Filter {
	return &Filter{
		cfg:        cfg,
		registry:   registry,
		cache:      cache,
		networkMgr: networkMgr,
		quotaMgr:   quotaMgr,
	}
}

// Evaluate applies hard constraints against all candidate execution paths
func (f *Filter) Evaluate(
	workloadType config.WorkloadType,
	payload interface{},
	resState resources.ResourceState,
) *EvaluationResult {
	feasible := make(map[config.ExecutionPathType]paths.ExecutionPathProfile)
	infeasible := make(map[config.ExecutionPathType]string)

	allPaths := f.registry.GetAllPaths()

	// 1. Check cache first
	cacheKey := cache.GenerateKey(string(workloadType), payload)
	cachedEntry, cacheHit := f.cache.Get(cacheKey)

	for pathType, profile := range allPaths {
		// Is path marked administrative available?
		if !profile.Available {
			infeasible[pathType] = "Path administratively marked disabled"
			continue
		}

		// Check workload support
		if !supportsWorkload(profile.SupportedWorkloads, workloadType) {
			infeasible[pathType] = fmt.Sprintf("Workload %s not supported by %s", workloadType, pathType)
			continue
		}

		// CACHE specific check
		if pathType == config.PathCache {
			if cacheHit {
				feasible[pathType] = profile
			} else {
				infeasible[pathType] = "Cache miss (no previous evaluation for this request payload)"
			}
			continue
		}

		// SIMULATED_CLOUD specific checks
		if pathType == config.PathSimulatedCloud {
			if !f.networkMgr.IsAvailable() || resState.NetworkStatus == config.NetworkOffline {
				infeasible[pathType] = "Network connectivity is offline"
				continue
			}
			if !f.quotaMgr.HasQuota() {
				infeasible[pathType] = "Cloud API request quota exhausted"
				continue
			}
			feasible[pathType] = profile
			continue
		}

		// HIGHER_CAPABILITY_LOCAL specific checks
		if pathType == config.PathHigherCapabilityLocal {
			// Memory check: requires free memory and low/medium memory pressure
			if resState.RAMPressure == resources.PressureHigh {
				infeasible[pathType] = fmt.Sprintf("Host RAM pressure is HIGH (%.1f%% used, %d MB free < %d MB required)",
					resState.RAMUsage, resState.RAMAvailableMb, profile.MinRAMMb)
				continue
			}
			if resState.RAMAvailableMb < profile.MinRAMMb {
				infeasible[pathType] = fmt.Sprintf("Available RAM (%d MB) is below threshold for higher-capability model (%d MB required)",
					resState.RAMAvailableMb, profile.MinRAMMb)
				continue
			}
			// CPU check: heavy model cannot run reliably when CPU is already saturated
			if resState.CPUUsage > profile.MaxCPUAllowedPct || resState.CPUPressure == resources.PressureHigh {
				infeasible[pathType] = fmt.Sprintf("Host CPU utilization is saturated (%.1f%% > %.1f%% allowed)",
					resState.CPUUsage, profile.MaxCPUAllowedPct)
				continue
			}
			// GPU check: if profile strictly demands GPU
			if profile.RequiresGPU && !resState.GPUAvailable {
				infeasible[pathType] = "GPU acceleration required but not present"
				continue
			}
			feasible[pathType] = profile
			continue
		}

		// LIGHTWEIGHT_LOCAL specific checks
		if pathType == config.PathLightweightLocal {
			if resState.RAMAvailableMb < profile.MinRAMMb {
				infeasible[pathType] = fmt.Sprintf("Available RAM (%d MB) is below minimum requirement (%d MB)",
					resState.RAMAvailableMb, profile.MinRAMMb)
				continue
			}
			if resState.CPUUsage > profile.MaxCPUAllowedPct {
				infeasible[pathType] = fmt.Sprintf("CPU usage (%.1f%%) exceeds maximum operational limit for lightweight inference",
					resState.CPUUsage)
				continue
			}
			feasible[pathType] = profile
			continue
		}

		// OFFLINE_FALLBACK
		if pathType == config.PathOfflineFallback {
			// Minimal requirements (runs even during severe resource squeeze)
			if resState.RAMAvailableMb < profile.MinRAMMb {
				infeasible[pathType] = "Host is completely out of memory"
				continue
			}
			feasible[pathType] = profile
			continue
		}

		// Default fallback
		feasible[pathType] = profile
	}

	var cachedResult interface{}
	if cacheHit && cachedEntry != nil {
		cachedResult = cachedEntry.Result
	}

	return &EvaluationResult{
		FeasiblePaths:   feasible,
		InfeasiblePaths: infeasible,
		CacheHit:        cacheHit,
		CachedResult:    cachedResult,
	}
}

func supportsWorkload(list []config.WorkloadType, target config.WorkloadType) bool {
	for _, w := range list {
		if w == target {
			return true
		}
	}
	return false
}
