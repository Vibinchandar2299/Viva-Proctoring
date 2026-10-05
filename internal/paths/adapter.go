package paths

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/cache"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/network"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/quota"
)

// RouteExecutionRequest contains data needed for execution
type RouteExecutionRequest struct {
	RequestID    string                 `json:"requestId"`
	WorkloadType config.WorkloadType    `json:"workloadType"`
	Complexity   config.ComplexityLevel `json:"complexity"`
	Priority     config.PriorityLevel   `json:"priority"`
	Payload      interface{}            `json:"payload,omitempty"`
}

// ExecutionResult captures the output and measured execution metrics
type ExecutionResult struct {
	RequestID    string                   `json:"requestId"`
	SelectedPath config.ExecutionPathType `json:"selectedPath"`
	ExecutionMs  float64                  `json:"executionMs"`
	Success      bool                     `json:"success"`
	Cached       bool                     `json:"cached"`
	Output       interface{}              `json:"output"`
	ErrorMessage string                   `json:"errorMessage,omitempty"`
}

// ExecutionCoordinator dispatches AI workload execution across paths
type ExecutionCoordinator struct {
	cfg        *config.Config
	cache      *cache.LRUCache
	quotaMgr   *quota.QuotaManager
	networkMgr *network.NetworkManager
	httpClient *http.Client
}

// NewExecutionCoordinator creates the coordinator
func NewExecutionCoordinator(
	cfg *config.Config,
	cache *cache.LRUCache,
	quotaMgr *quota.QuotaManager,
	networkMgr *network.NetworkManager,
) *ExecutionCoordinator {
	return &ExecutionCoordinator{
		cfg:        cfg,
		cache:      cache,
		quotaMgr:   quotaMgr,
		networkMgr: networkMgr,
		httpClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

// Execute coordinates the execution of the selected path
func (ec *ExecutionCoordinator) Execute(ctx context.Context, req *RouteExecutionRequest, path config.ExecutionPathType) (*ExecutionResult, error) {
	startTime := time.Now()

	switch path {
	case config.PathCache:
		cacheKey := cache.GenerateKey(string(req.WorkloadType), req.Payload)
		if entry, found := ec.cache.Get(cacheKey); found {
			elapsed := float64(time.Since(startTime).Microseconds()) / 1000.0
			return &ExecutionResult{
				RequestID:    req.RequestID,
				SelectedPath: config.PathCache,
				ExecutionMs:  elapsed,
				Success:      true,
				Cached:       true,
				Output:       entry.Result,
			}, nil
		}
		// If cache was requested but missing, fall back to lightweight local
		return ec.executeMock(req, config.PathLightweightLocal, startTime)

	case config.PathLightweightLocal:
		if !ec.cfg.MockExecution && ec.cfg.PythonLightweightURL != "" {
			res, err := ec.forwardToPython(ctx, ec.cfg.PythonLightweightURL, req, path, startTime)
			if err == nil {
				ec.saveToCache(req, res.Output, res.ExecutionMs)
				return res, nil
			}
		}
		res, err := ec.executeMock(req, config.PathLightweightLocal, startTime)
		if err == nil {
			ec.saveToCache(req, res.Output, res.ExecutionMs)
		}
		return res, err

	case config.PathHigherCapabilityLocal:
		if !ec.cfg.MockExecution && ec.cfg.PythonHigherCapURL != "" {
			res, err := ec.forwardToPython(ctx, ec.cfg.PythonHigherCapURL, req, path, startTime)
			if err == nil {
				ec.saveToCache(req, res.Output, res.ExecutionMs)
				return res, nil
			}
		}
		res, err := ec.executeMock(req, config.PathHigherCapabilityLocal, startTime)
		if err == nil {
			ec.saveToCache(req, res.Output, res.ExecutionMs)
		}
		return res, err

	case config.PathSimulatedCloud:
		// Deduct quota
		if !ec.quotaMgr.ConsumeQuota() {
			return nil, fmt.Errorf("cloud quota exhausted during execution")
		}
		res, err := ec.executeMock(req, config.PathSimulatedCloud, startTime)
		if err == nil {
			ec.saveToCache(req, res.Output, res.ExecutionMs)
		}
		return res, err

	case config.PathOfflineFallback:
		if !ec.cfg.MockExecution && ec.cfg.PythonOfflineURL != "" {
			res, err := ec.forwardToPython(ctx, ec.cfg.PythonOfflineURL, req, path, startTime)
			if err == nil {
				return res, nil
			}
		}
		return ec.executeMock(req, config.PathOfflineFallback, startTime)

	default:
		return nil, fmt.Errorf("unknown execution path: %s", path)
	}
}

func (ec *ExecutionCoordinator) saveToCache(req *RouteExecutionRequest, output interface{}, execMs float64) {
	key := cache.GenerateKey(string(req.WorkloadType), req.Payload)
	ec.cache.Set(key, string(req.WorkloadType), output, execMs)
}

func (ec *ExecutionCoordinator) forwardToPython(
	ctx context.Context,
	url string,
	req *RouteExecutionRequest,
	path config.ExecutionPathType,
	startTime time.Time,
) (*ExecutionResult, error) {
	body, err := json.Marshal(req)
	if err != nil {
		return nil, err
	}

	httpReq, err := http.NewRequestWithContext(ctx, "POST", url+"/predict", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")

	resp, err := ec.httpClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("python service returned status %d", resp.StatusCode)
	}

	var output interface{}
	if err := json.NewDecoder(resp.Body).Decode(&output); err != nil {
		return nil, err
	}

	elapsed := float64(time.Since(startTime).Milliseconds())
	return &ExecutionResult{
		RequestID:    req.RequestID,
		SelectedPath: path,
		ExecutionMs:  elapsed,
		Success:      true,
		Cached:       false,
		Output:       output,
	}, nil
}

// executeMock generates realistic output and simulated latency
func (ec *ExecutionCoordinator) executeMock(
	req *RouteExecutionRequest,
	path config.ExecutionPathType,
	startTime time.Time,
) (*ExecutionResult, error) {
	var simulatedDelay time.Duration
	var output map[string]interface{}

	switch path {
	case config.PathLightweightLocal:
		simulatedDelay = 65 * time.Millisecond
		output = map[string]interface{}{
			"status":     "PROCESSED_LIGHTWEIGHT_LOCAL",
			"workload":   req.WorkloadType,
			"confidence": 0.88,
			"summary":    fmt.Sprintf("Evaluated %s using fast quantized model", req.WorkloadType),
		}

	case config.PathHigherCapabilityLocal:
		simulatedDelay = 240 * time.Millisecond
		output = map[string]interface{}{
			"status":     "PROCESSED_HIGHER_CAPABILITY_LOCAL",
			"workload":   req.WorkloadType,
			"confidence": 0.97,
			"summary":    fmt.Sprintf("Evaluated %s using full precision deep model with deep reasoning", req.WorkloadType),
		}

	case config.PathSimulatedCloud:
		netLatency := time.Duration(ec.networkMgr.GetLatencyMs()) * time.Millisecond
		simulatedDelay = 120*time.Millisecond + netLatency
		output = map[string]interface{}{
			"status":         "PROCESSED_SIMULATED_CLOUD",
			"workload":       req.WorkloadType,
			"confidence":     0.98,
			"quotaRemaining": ec.quotaMgr.Summary()["quotaRemaining"],
			"summary":        fmt.Sprintf("Evaluated %s via simulated cloud API", req.WorkloadType),
		}

	case config.PathOfflineFallback:
		simulatedDelay = 15 * time.Millisecond
		output = map[string]interface{}{
			"status":     "PROCESSED_OFFLINE_FALLBACK",
			"workload":   req.WorkloadType,
			"confidence": 0.65,
			"summary":    fmt.Sprintf("Evaluated %s using emergency offline heuristic engine", req.WorkloadType),
		}
	}

	time.Sleep(simulatedDelay)
	elapsed := float64(time.Since(startTime).Milliseconds())

	return &ExecutionResult{
		RequestID:    req.RequestID,
		SelectedPath: path,
		ExecutionMs:  elapsed,
		Success:      true,
		Cached:       false,
		Output:       output,
	}, nil
}
