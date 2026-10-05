package scoring

import (
	"math"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
)

// ScoreBreakdown details the components of a path's final score
type ScoreBreakdown struct {
	TotalScore       float64 `json:"totalScore"`
	ResourceScore    float64 `json:"resourceScore"`
	LatencyScore     float64 `json:"latencyScore"`
	CostScore        float64 `json:"costScore"`
	CapabilityScore  float64 `json:"capabilityScore"`
	PriorityFitScore float64 `json:"priorityFitScore"`
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
	w := s.cfg.Weights

	switch priority {
	case config.PriorityHigh:
		// Real-time viva proctoring requires low latency
		w.WeightLatency += 0.12
		w.WeightResource += 0.03
		w.WeightCost -= 0.10
		w.WeightPriority += 0.05
		w.WeightCapability -= 0.10

	case config.PriorityLow:
		// Non-critical background task; prioritize cost and resource conservation
		w.WeightLatency -= 0.15
		w.WeightCost += 0.15
		w.WeightResource += 0.10
		w.WeightPriority -= 0.05
		w.WeightCapability -= 0.05
	}

	if complexity == config.ComplexityHigh {
		w.WeightCapability += 0.15
		w.WeightLatency -= 0.05
		w.WeightCost -= 0.10
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
	case config.PathLightweightLocal:
		// Highly resilient to resource strain
		if resState.RAMPressure == resources.PressureHigh {
			return 85.0
		}
		return 95.0

	case config.PathHigherCapabilityLocal:
		// Excellent when host has plenty of headroom, lower when approaching boundaries
		if resState.RAMPressure == resources.PressureLow && resState.CPUPressure == resources.PressureLow {
			return 95.0
		}
		if resState.RAMPressure == resources.PressureMedium {
			return 70.0
		}
		return 40.0

	case config.PathSimulatedCloud:
		// Consumes virtually zero local RAM/CPU
		return 90.0

	case config.PathOfflineFallback:
		// Extremely low footprint
		return 98.0

	default:
		return 75.0
	}
}

func (s *Scorer) calculateLatencyScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	resState resources.ResourceState,
) float64 {
	switch pathType {
	case config.PathOfflineFallback:
		return 95.0 // ~15ms
	case config.PathLightweightLocal:
		return 85.0 // ~85ms
	case config.PathHigherCapabilityLocal:
		return 60.0 // ~320ms
	case config.PathSimulatedCloud:
		if resState.NetworkStatus == config.NetworkGood {
			return 70.0
		} else if resState.NetworkStatus == config.NetworkDegraded {
			return 45.0
		}
		return 20.0
	default:
		return 50.0
	}
}

func (s *Scorer) calculateCostScore(pathType config.ExecutionPathType, profile paths.ExecutionPathProfile) float64 {
	if profile.EstimatedCost <= 0.0 {
		return 100.0 // Free local processing
	}
	// Simulated cloud costs money
	return 50.0
}

func (s *Scorer) calculateCapabilityScore(
	pathType config.ExecutionPathType,
	profile paths.ExecutionPathProfile,
	complexity config.ComplexityLevel,
) float64 {
	base := profile.CapabilityScore

	switch complexity {
	case config.ComplexityHigh:
		// High complexity demands strong models
		if pathType == config.PathHigherCapabilityLocal || pathType == config.PathSimulatedCloud {
			return math.Min(100.0, base+5.0)
		}
		if pathType == config.PathOfflineFallback {
			return math.Max(20.0, base-20.0) // Penalize fallback heavily on high complexity
		}
		return base - 5.0

	case config.ComplexityLow:
		// Low complexity can be satisfied perfectly by lightweight local
		if pathType == config.PathLightweightLocal {
			return 95.0
		}
		return base

	default: // MEDIUM
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
		// Real-time demands speed and deterministic execution
		if pathType == config.PathLightweightLocal {
			return 95.0
		}
		if pathType == config.PathHigherCapabilityLocal {
			return 85.0
		}
		if pathType == config.PathSimulatedCloud {
			return 65.0 // Network jitter hazard
		}
		return 70.0

	case config.PriorityLow:
		// Low priority can run anywhere, prefer lightweight or cloud
		return 80.0

	default: // MEDIUM
		return 85.0
	}
}
