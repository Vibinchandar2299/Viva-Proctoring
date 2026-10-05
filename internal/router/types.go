package router

import (
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

// RouteRequest models the payload received from Spring Boot or client
type RouteRequest struct {
	RequestID    string                 `json:"requestId"`
	WorkloadType config.WorkloadType    `json:"workloadType"`
	Complexity   config.ComplexityLevel `json:"complexity,omitempty"` // Optional
	Priority     config.PriorityLevel   `json:"priority,omitempty"`   // Optional
	Payload      interface{}            `json:"payload,omitempty"`    // Optional request payload/question/frame
	Execute      bool                   `json:"execute,omitempty"`    // If true, coordinates actual execution
}

// RouteResponse models the response returned to Spring Boot or client
type RouteResponse struct {
	RequestID       string                             `json:"requestId"`
	SelectedPath    config.ExecutionPathType           `json:"selectedPath"`
	Reasoning       string                             `json:"reasoning"`
	LatencyMs       float64                            `json:"latencyMs"`
	EstimatedCost   float64                            `json:"estimatedCost"`
	ResourceState   resources.ResourceState            `json:"resourceState"`
	Scores          map[string]float64                 `json:"scores"`
	ScoreDetails    map[config.ExecutionPathType]scoring.ScoreBreakdown `json:"scoreDetails,omitempty"`
	FeasiblePaths   []config.ExecutionPathType         `json:"feasiblePaths"`
	InfeasiblePaths map[config.ExecutionPathType]string `json:"infeasiblePaths,omitempty"`
	CacheHit        bool                               `json:"cacheHit"`
	ExecutionResult *paths.ExecutionResult             `json:"executionResult,omitempty"`
	Timestamp       string                             `json:"timestamp"`
}

// BaselineComparisonResult holds side-by-side benchmark comparison
type BaselineComparisonResult struct {
	WorkloadType       config.WorkloadType      `json:"workloadType"`
	Complexity         config.ComplexityLevel   `json:"complexity"`
	Priority           config.PriorityLevel     `json:"priority"`
	SimulatedCondition string                   `json:"simulatedCondition"`
	
	// AI-Route Dynamic Strategy
	AIRouteSelectedPath   config.ExecutionPathType `json:"aiRouteSelectedPath"`
	AIRouteLatencyMs      float64                  `json:"aiRouteLatencyMs"`
	AIRouteCost           float64                  `json:"aiRouteCost"`
	AIRouteSuccess        bool                     `json:"aiRouteSuccess"`
	AIRouteReasoning      string                   `json:"aiRouteReasoning"`
	
	// Baseline Fixed Strategy
	BaselineFixedPath     config.ExecutionPathType `json:"baselineFixedPath"`
	BaselineLatencyMs     float64                  `json:"baselineLatencyMs"`
	BaselineCost          float64                  `json:"baselineCost"`
	BaselineSuccess       bool                     `json:"baselineSuccess"`
	BaselineFailureReason string                   `json:"baselineFailureReason,omitempty"`
	
	// Direct Delta Observations
	LatencyDeltaMs        float64                  `json:"latencyDeltaMs"`
	CostDelta             float64                  `json:"costDelta"`
	AdaptationObserved    bool                     `json:"adaptationObserved"`
	Analysis              string                   `json:"analysis"`
}
