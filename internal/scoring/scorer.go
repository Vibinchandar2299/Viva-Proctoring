package scoring

import (
	"math"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
)

// ScoreBreakdown details the components of a path's final score
type ScoreBreakdown struct {
	TotalScore       float64               `json:"totalScore"`
	ResourceScore    float64               `json:"resourceScore"`
	LatencyScore     float64               `json:"latencyScore"`
	CostScore        float64               `json:"costScore"`
	CapabilityScore  float64               `json:"capabilityScore"`
	PriorityFitScore float64               `json:"priorityFitScore"`
	AppliedWeights   config.ScoringWeights `json:"appliedWeights"`
}

// Scorer calculates multi-objective utility scores for feasible paths
type Scorer struct {
	cfg *config.Config
}

// NewScorer creates a new Scorer
func NewScorer(cfg *config.Config) *Scorer {
	return &Scorer{
		cfg: cfg,
	}
}

// ScorePaths computes multi-objective scores for all feasible candidate paths
func (s *Scorer) ScorePaths(
	workloadType config.WorkloadType,
	complexity config.ComplexityLevel,
	priority config.PriorityLevel,
	feasiblePaths map[config.ExecutionPathType]paths.ExecutionPathProfile,
	resState resources.ResourceState,
) map[config.ExecutionPathType]ScoreBreakdown {
	scores := make(map[config.ExecutionPathType]ScoreBreakdown)

	// Adjust weights dynamically based on workload priority and complexity
	weights := s.computeDynamicWeights(priority, complexity)

	for pathType, profile := range feasiblePaths {
		// CACHE fast path: instant response, zero cost, full fidelity
		if pathType == config.PathCache {
			scores[pathType] = ScoreBreakdown{
				TotalScore:       99.9,
				ResourceScore:    100.0,
				LatencyScore:     100.0,
				CostScore:        100.0,
				CapabilityScore:  100.0,
				PriorityFitScore: 100.0,
				AppliedWeights:   weights,
			}
			continue
		}

		resScore := s.calculateResourceScore(pathType, profile, resState)
		latScore := s.calculateLatencyScore(pathType, profile, resState)
		costScore := s.calculateCostScore(pathType, profile)
		capScore := s.calculateCapabilityScore(pathType, profile, complexity)
		prioScore := s.calculatePriorityFitScore(pathType, profile, priority)

		total := (resScore * weights.WeightResource) +
			(latScore * weights.WeightLatency) +
			(costScore * weights.WeightCost) +
			(capScore * weights.WeightCapability) +
			(prioScore * weights.WeightPriority)

		// Round to 1 decimal place
		total = math.Round(total*10) / 10

		scores[pathType] = ScoreBreakdown{
			TotalScore:       total,
			ResourceScore:    math.Round(resScore*10) / 10,
			LatencyScore:     math.Round(latScore*10) / 10,
			CostScore:        math.Round(costScore*10) / 10,
			CapabilityScore:  math.Round(capScore*10) / 10,
			PriorityFitScore: math.Round(prioScore*10) / 10,
			AppliedWeights:   weights,
		}
	}

	return scores
}

func (s *Scorer) computeDynamicWeights(priority config.PriorityLevel, complexity config.ComplexityLevel) config.ScoringWeights {
	w := config.ScoringWeights{
		WeightResource:   0.25,
		WeightLatency:    0.25,
		WeightCost:       0.15,
		WeightCapability: 0.25,
		WeightPriority:   0.10,
	}

	if complexity == config.ComplexityHigh {
		w.WeightCapability += 0.15
		w.WeightLatency -= 0.05
		w.WeightCost -= 0.05
		w.WeightResource -= 0.05
	}

	switch priority {
	case config.PriorityHigh:
		w.WeightLatency += 0.08
		w.WeightCost -= 0.08
	case config.PriorityLow:
		w.WeightCost += 0.15
		w.WeightLatency -= 0.10
		w.WeightPriority -= 0.05
	}

	// Normalize sum of weights to 1.0
	sum := w.WeightResource + w.WeightLatency + w.WeightCost + w.WeightCapability + w.WeightPriority
	if sum > 0 {
		w.WeightResource /= sum
		w.WeightLatency /= sum
		w.WeightCost /= sum
		w.WeightCapability /= sum
		w.WeightPriority /= sum
	}

	return w
}

func (s *Scorer) calculateResourceScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	resState resources.ResourceState,
) float64 {
	switch pathType {
	case config.PathHigherCapabilityLocal:
		// When host has plenty of headroom, full local model is the primary choice
		if resState.RAMPressure == resources.PressureLow && resState.CPUPressure == resources.PressureLow {
			return 100.0
		}
		if resState.RAMPressure == resources.PressureMedium {
			return 65.0
		}
		return 30.0

	case config.PathLightweightLocal:
		// Highly resilient under memory strain
		if resState.RAMPressure == resources.PressureHigh {
			return 95.0
		}
		if resState.RAMPressure == resources.PressureMedium {
			return 92.0
		}
		return 85.0

	case config.PathSimulatedCloud:
		// Offloads CPU/RAM to remote worker
		return 90.0

	case config.PathOfflineFallback:
		// Ultra low footprint emergency fallback
		return 70.0

	default:
		return 70.0
	}
}

func (s *Scorer) calculateLatencyScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	resState resources.ResourceState,
) float64 {
	switch pathType {
	case config.PathLightweightLocal:
		return 88.0 // ~85ms
	case config.PathHigherCapabilityLocal:
		return 78.0 // ~320ms (well within viva latency budgets)
	case config.PathSimulatedCloud:
		if resState.NetworkStatus == config.NetworkGood {
			return 68.0
		} else if resState.NetworkStatus == config.NetworkDegraded {
			return 40.0
		}
		return 15.0
	case config.PathOfflineFallback:
		return 65.0 // Fallback heuristic
	default:
		return 50.0
	}
}

func (s *Scorer) calculateCostScore(pathType config.ExecutionPathType, profile paths.ExecutionPathProfile) float64 {
	if profile.EstimatedCost <= 0.0 {
		return 100.0 // Free local processing
	}
	// Simulated cloud incurs quota/cost penalty
	return 40.0
}

func (s *Scorer) calculateCapabilityScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	complexity config.ComplexityLevel,
) float64 {
	base := profile.CapabilityScore

	switch complexity {
	case config.ComplexityHigh:
		if pathType == config.PathHigherCapabilityLocal {
			return 100.0 // Heavy model delivers necessary depth
		}
		if pathType == config.PathSimulatedCloud {
			return 95.0
		}
		if pathType == config.PathLightweightLocal {
			return 65.0 // Quantized model struggles with deep nuance
		}
		if pathType == config.PathOfflineFallback {
			return 25.0 // Fallback has poor accuracy on complex tasks
		}
		return base

	case config.ComplexityLow:
		if pathType == config.PathLightweightLocal {
			return 96.0 // Ideal fit for low complexity
		}
		return base

	default: // MEDIUM
		if pathType == config.PathLightweightLocal {
			return 85.0
		}
		if pathType == config.PathHigherCapabilityLocal {
			return 95.0
		}
		return base
	}
}

func (s *Scorer) calculatePriorityFitScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	priority config.PriorityLevel,
) float64 {
	switch priority {
	case config.PriorityHigh:
		if pathType == config.PathLightweightLocal {
			return 96.0
		}
		if pathType == config.PathHigherCapabilityLocal {
			return 90.0
		}
		if pathType == config.PathSimulatedCloud {
			return 60.0
		}
		return 35.0 // OFFLINE_FALLBACK is emergency only

	case config.PriorityLow:
		if pathType == config.PathLightweightLocal {
			return 85.0
		}
		if pathType == config.PathSimulatedCloud {
			return 80.0
		}
		return 40.0

	default: // MEDIUM
		if pathType == config.PathHigherCapabilityLocal {
			return 90.0
		}
		if pathType == config.PathLightweightLocal {
			return 88.0
		}
		return 35.0
	}
}
