package config

import (
	"encoding/json"
	"os"
	"strconv"
)

// WorkloadType defines supported AI tasks in Viva Proctoring
type WorkloadType string

const (
	WorkloadIdentityVerification   WorkloadType = "IDENTITY_VERIFICATION"
	WorkloadFaceDetection          WorkloadType = "FACE_DETECTION"
	WorkloadMultipleFaceDetection  WorkloadType = "MULTIPLE_FACE_DETECTION"
	WorkloadObjectDetection        WorkloadType = "OBJECT_DETECTION"
	WorkloadSpeechToText           WorkloadType = "SPEECH_TO_TEXT"
	WorkloadAnswerEvaluation       WorkloadType = "ANSWER_EVALUATION"
	WorkloadCommunicationAnalysis  WorkloadType = "COMMUNICATION_ANALYSIS"
	WorkloadFollowUpGeneration     WorkloadType = "FOLLOW_UP_GENERATION"
)

// ComplexityLevel defines workload complexity
type ComplexityLevel string

const (
	ComplexityLow    ComplexityLevel = "LOW"
	ComplexityMedium ComplexityLevel = "MEDIUM"
	ComplexityHigh   ComplexityLevel = "HIGH"
)

// PriorityLevel defines proctoring task priority
type PriorityLevel string

const (
	PriorityLow    PriorityLevel = "LOW"
	PriorityMedium PriorityLevel = "MEDIUM"
	PriorityHigh   PriorityLevel = "HIGH"
)

// ExecutionPathType defines the route destination
type ExecutionPathType string

const (
	PathLightweightLocal      ExecutionPathType = "LIGHTWEIGHT_LOCAL"
	PathHigherCapabilityLocal ExecutionPathType = "HIGHER_CAPABILITY_LOCAL"
	PathCache                 ExecutionPathType = "CACHE"
	PathSimulatedCloud        ExecutionPathType = "SIMULATED_CLOUD"
	PathOfflineFallback       ExecutionPathType = "OFFLINE_FALLBACK"
)

// NetworkStatus defines network state
type NetworkStatus string

const (
	NetworkGood     NetworkStatus = "GOOD"
	NetworkDegraded NetworkStatus = "DEGRADED"
	NetworkPoor     NetworkStatus = "POOR"
	NetworkOffline  NetworkStatus = "OFFLINE"
)

// ResourceThresholds configures pressure levels
type ResourceThresholds struct {
	CPULowThreshold       float64 `json:"cpuLowThreshold"`       // default 60%
	CPUMediumThreshold    float64 `json:"cpuMediumThreshold"`    // default 80%
	RAMLowThreshold       float64 `json:"ramLowThreshold"`       // default 65%
	RAMMediumThreshold    float64 `json:"ramMediumThreshold"`    // default 80%
	HigherCapMinRAMMb     uint64  `json:"higherCapMinRAMMb"`     // minimum free MB for higher capability (default 2048 MB)
	LightweightMinRAMMb   uint64  `json:"lightweightMinRAMMb"`   // minimum free MB for lightweight (default 512 MB)
}

// ScoringWeights configures multi-objective scoring formula
type ScoringWeights struct {
	WeightResource   float64 `json:"weightResource"`   // 0.25
	WeightLatency    float64 `json:"weightLatency"`    // 0.30
	WeightCost       float64 `json:"weightCost"`       // 0.15
	WeightCapability float64 `json:"weightCapability"` // 0.20
	WeightPriority   float64 `json:"weightPriority"`   // 0.10
}

// WorkloadDefaults defines fallback complexity & priority
type WorkloadDefaults struct {
	Complexity ComplexityLevel `json:"complexity"`
	Priority   PriorityLevel   `json:"priority"`
}

// Config holds all router runtime configuration
type Config struct {
	ServerPort              int                         `json:"serverPort"`
	SpringBootURL           string                      `json:"springBootUrl"`
	PythonLightweightURL    string                      `json:"pythonLightweightUrl"`
	PythonHigherCapURL      string                      `json:"pythonHigherCapUrl"`
	PythonOfflineURL        string                      `json:"pythonOfflineUrl"`
	CacheMaxEntries         int                         `json:"cacheMaxEntries"`
	CacheTTLSeconds         int                         `json:"cacheTtlSeconds"`
	SimulatedQuotaLimit     int                         `json:"simulatedQuotaLimit"`
	SimulatedCostPerRequest float64                     `json:"simulatedCostPerRequest"`
	Thresholds              ResourceThresholds          `json:"thresholds"`
	Weights                 ScoringWeights              `json:"weights"`
	WorkloadDefaultsMap     map[WorkloadType]WorkloadDefaults `json:"workloadDefaults"`
	DefaultBaselinePath     ExecutionPathType           `json:"defaultBaselinePath"`
	MockExecution           bool                        `json:"mockExecution"` // true allows standalone testing without Python services
}

// DefaultConfig provides sensible defaults optimized for 8GB RAM college lab environments
func DefaultConfig() *Config {
	return &Config{
		ServerPort:              8082,
		SpringBootURL:           getEnv("SPRING_BOOT_URL", "http://localhost:8080"),
		PythonLightweightURL:    getEnv("PYTHON_LIGHTWEIGHT_URL", "http://localhost:5001"),
		PythonHigherCapURL:      getEnv("PYTHON_HIGHER_CAP_URL", "http://localhost:5002"),
		PythonOfflineURL:        getEnv("PYTHON_OFFLINE_URL", "http://localhost:5003"),
		CacheMaxEntries:         1000,
		CacheTTLSeconds:         3600,
		SimulatedQuotaLimit:     50,
		SimulatedCostPerRequest: 0.05,
		MockExecution:           true, // Enables instant standalone local execution & benchmarks
		DefaultBaselinePath:     PathHigherCapabilityLocal,
		Thresholds: ResourceThresholds{
			CPULowThreshold:     60.0,
			CPUMediumThreshold:  80.0,
			RAMLowThreshold:     65.0,
			RAMMediumThreshold:  80.0,
			HigherCapMinRAMMb:   2048,
			LightweightMinRAMMb: 512,
		},
		Weights: ScoringWeights{
			WeightResource:   0.25,
			WeightLatency:    0.30,
			WeightCost:       0.15,
			WeightCapability: 0.20,
			WeightPriority:   0.10,
		},
		WorkloadDefaultsMap: map[WorkloadType]WorkloadDefaults{
			WorkloadIdentityVerification:  {Complexity: ComplexityLow, Priority: PriorityHigh},
			WorkloadFaceDetection:         {Complexity: ComplexityLow, Priority: PriorityHigh},
			WorkloadMultipleFaceDetection: {Complexity: ComplexityLow, Priority: PriorityHigh},
			WorkloadObjectDetection:       {Complexity: ComplexityMedium, Priority: PriorityMedium},
			WorkloadSpeechToText:          {Complexity: ComplexityMedium, Priority: PriorityHigh},
			WorkloadAnswerEvaluation:      {Complexity: ComplexityHigh, Priority: PriorityHigh},
			WorkloadCommunicationAnalysis: {Complexity: ComplexityMedium, Priority: PriorityMedium},
			WorkloadFollowUpGeneration:    {Complexity: ComplexityHigh, Priority: PriorityMedium},
		},
	}
}

// LoadConfig loads configuration from optional file or environment
func LoadConfig(filePath string) *Config {
	cfg := DefaultConfig()

	if filePath != "" {
		if data, err := os.ReadFile(filePath); err == nil {
			_ = json.Unmarshal(data, cfg)
		}
	}

	if portStr := os.Getenv("PORT"); portStr != "" {
		if p, err := strconv.Atoi(portStr); err == nil {
			cfg.ServerPort = p
		}
	}
	if mockStr := os.Getenv("MOCK_EXECUTION"); mockStr != "" {
		cfg.MockExecution = mockStr == "true" || mockStr == "1"
	}
	if quotaStr := os.Getenv("SIMULATED_QUOTA_LIMIT"); quotaStr != "" {
		if q, err := strconv.Atoi(quotaStr); err == nil {
			cfg.SimulatedQuotaLimit = q
		}
	}

	return cfg
}

func getEnv(key, defaultVal string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return defaultVal
}
