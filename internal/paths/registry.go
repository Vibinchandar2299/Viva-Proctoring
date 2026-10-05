package paths

import (
	"sync"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
)

// ExecutionPathProfile details the operational and hardware envelope of an execution path
type ExecutionPathProfile struct {
	Name               config.ExecutionPathType `json:"name"`
	DisplayName        string                   `json:"displayName"`
	Description        string                   `json:"description"`
	Available          bool                     `json:"available"`
	MinRAMMb           uint64                   `json:"minRAMMb"`
	MaxCPUAllowedPct   float64                  `json:"maxCPUAllowedPct"`
	RequiresGPU        bool                     `json:"requiresGPU"`
	RequiresNetwork    bool                     `json:"requiresNetwork"`
	RequiresQuota      bool                     `json:"requiresQuota"`
	BaseLatencyMs      float64                  `json:"baseLatencyMs"`
	EstimatedCost      float64                  `json:"estimatedCost"`
	CapabilityScore    float64                  `json:"capabilityScore"` // 0 to 100
	SupportedWorkloads []config.WorkloadType    `json:"supportedWorkloads"`
}

// Registry maintains all registered execution paths
type Registry struct {
	mu    sync.RWMutex
	paths map[config.ExecutionPathType]ExecutionPathProfile
}

// NewPathRegistry creates and seeds the path registry with structured profiles
func NewPathRegistry(cfg *config.Config) *Registry {
	allWorkloads := []config.WorkloadType{
		config.WorkloadIdentityVerification,
		config.WorkloadFaceDetection,
		config.WorkloadMultipleFaceDetection,
		config.WorkloadObjectDetection,
		config.WorkloadSpeechToText,
		config.WorkloadAnswerEvaluation,
		config.WorkloadCommunicationAnalysis,
		config.WorkloadFollowUpGeneration,
	}

	profiles := map[config.ExecutionPathType]ExecutionPathProfile{
		config.PathCache: {
			Name:             config.PathCache,
			DisplayName:      "In-Process Deterministic LRU Cache",
			Description:      "Immediate response from memory for previously evaluated viva requests",
			Available:        true,
			MinRAMMb:         0,
			MaxCPUAllowedPct: 100.0,
			RequiresGPU:      false,
			RequiresNetwork:  false,
			RequiresQuota:    false,
			BaseLatencyMs:    2.0,
			EstimatedCost:    0.0,
			CapabilityScore:  100.0, // Cached answer retains original fidelity
			SupportedWorkloads: allWorkloads,
		},
		config.PathLightweightLocal: {
			Name:             config.PathLightweightLocal,
			DisplayName:      "Lightweight Local Inference (CPU-Optimized / Quantized)",
			Description:      "Low-footprint local models (e.g. MobileNet, TinyWhisper, 4-bit quant) suitable for constrained 8GB college lab PCs",
			Available:        true,
			MinRAMMb:         cfg.Thresholds.LightweightMinRAMMb, // 512MB
			MaxCPUAllowedPct: 90.0,
			RequiresGPU:      false,
			RequiresNetwork:  false,
			RequiresQuota:    false,
			BaseLatencyMs:    85.0,
			EstimatedCost:    0.0,
			CapabilityScore:  75.0,
			SupportedWorkloads: allWorkloads,
		},
		config.PathHigherCapabilityLocal: {
			Name:             config.PathHigherCapabilityLocal,
			DisplayName:      "Higher-Capability Local Inference (Full Precision / Large Model)",
			Description:      "Higher quality local models (e.g. Whisper-medium, deep face embedding, LLM) requiring higher RAM / GPU",
			Available:        true,
			MinRAMMb:         cfg.Thresholds.HigherCapMinRAMMb, // 2048MB
			MaxCPUAllowedPct: 80.0,
			RequiresGPU:      false, // GPU optional, but preferred
			RequiresNetwork:  false,
			RequiresQuota:    false,
			BaseLatencyMs:    320.0,
			EstimatedCost:    0.0,
			CapabilityScore:  95.0,
			SupportedWorkloads: allWorkloads,
		},
		config.PathSimulatedCloud: {
			Name:             config.PathSimulatedCloud,
			DisplayName:      "Simulated Cloud / API Quota Path",
			Description:      "Simulated remote cloud endpoint used for benchmarking under varying network latency and quota limits",
			Available:        true,
			MinRAMMb:         64,
			MaxCPUAllowedPct: 95.0,
			RequiresGPU:      false,
			RequiresNetwork:  true,
			RequiresQuota:    true,
			BaseLatencyMs:    180.0, // Base network roundtrip
			EstimatedCost:    cfg.SimulatedCostPerRequest,
			CapabilityScore:  98.0,
			SupportedWorkloads: allWorkloads,
		},
		config.PathOfflineFallback: {
			Name:             config.PathOfflineFallback,
			DisplayName:      "Offline Fallback Heuristic Engine",
			Description:      "Emergency offline rule-based and cached heuristic fallback when local resources are critically depleted or crashed",
			Available:        true,
			MinRAMMb:         32,
			MaxCPUAllowedPct: 100.0,
			RequiresGPU:      false,
			RequiresNetwork:  false,
			RequiresQuota:    false,
			BaseLatencyMs:    15.0,
			EstimatedCost:    0.0,
			CapabilityScore:  50.0,
			SupportedWorkloads: allWorkloads,
		},
	}

	return &Registry{
		paths: profiles,
	}
}

// GetPath returns the profile for a given path
func (r *Registry) GetPath(name config.ExecutionPathType) (ExecutionPathProfile, bool) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	p, exists := r.paths[name]
	return p, exists
}

// GetAllPaths returns all registered paths
func (r *Registry) GetAllPaths() map[config.ExecutionPathType]ExecutionPathProfile {
	r.mu.RLock()
	defer r.mu.RUnlock()
	res := make(map[config.ExecutionPathType]ExecutionPathProfile, len(r.paths))
	for k, v := range r.paths {
		res[k] = v
	}
	return res
}

// SetAvailability toggles whether a path is marked online/offline
func (r *Registry) SetAvailability(name config.ExecutionPathType, available bool) {
	r.mu.Lock()
	defer r.mu.Unlock()
	if p, exists := r.paths[name]; exists {
		p.Available = available
		r.paths[name] = p
	}
}
