package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/router"
)

// Handler houses all HTTP endpoints for the Go Router service
type Handler struct {
	engine *router.Engine
	cfg    *config.Config
}

// NewHandler creates a new API Handler
func NewHandler(engine *router.Engine, cfg *config.Config) *Handler {
	return &Handler{
		engine: engine,
		cfg:    cfg,
	}
}

// HandleHealth provides service health and path status (GET /health)
func (h *Handler) HandleHealth(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	pathsMap := h.engine.GetRegistry().GetAllPaths()
	pathStatus := make(map[string]bool, len(pathsMap))
	for k, v := range pathsMap {
		pathStatus[string(k)] = v.Available
	}

	resp := map[string]interface{}{
		"status":      "UP",
		"service":     "go-router",
		"mode":        "RULE_BASED_RESOURCE_AWARE",
		"target":      "Indian College Lab (<=8GB RAM, CPU-friendly)",
		"paths":       pathStatus,
		"network":     h.engine.GetNetworkManager().GetStatus(),
		"quotaRemaining": h.engine.GetQuotaManager().Summary()["quotaRemaining"],
		"cacheEntries":   h.engine.GetCache().Len(),
	}

	writeJSON(w, http.StatusOK, resp)
}

// HandleResources returns real-time hardware and network metrics (GET /resources)
func (h *Handler) HandleResources(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	resState := h.engine.GetResourceMonitor().GetCurrentState()
	writeJSON(w, http.StatusOK, resState)
}

// HandleRoute handles incoming routing decisions (POST /route)
func (h *Handler) HandleRoute(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req router.RouteRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return
	}

	resp, err := h.engine.Route(r.Context(), &req)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

// HandleMetrics returns aggregated routing performance metrics (GET /metrics)
func (h *Handler) HandleMetrics(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	metricsData := h.engine.GetTracker().GetMetrics()
	writeJSON(w, http.StatusOK, metricsData)
}

// HandleHistory returns decision audit logs (GET /routing/history)
func (h *Handler) HandleHistory(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	limitStr := r.URL.Query().Get("limit")
	limit := 100
	if limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
			limit = l
		}
	}

	workloadFilter := r.URL.Query().Get("workload")
	historyList := h.engine.GetTracker().GetHistory(limit, workloadFilter)

	writeJSON(w, http.StatusOK, map[string]interface{}{
		"total":   len(historyList),
		"history": historyList,
	})
}

// SimulationRequest allows test scripts and judges to simulate hardware stress
type SimulationRequest struct {
	Mode           string                `json:"mode"`           // "NORMAL", "HIGH_RAM", "HIGH_CPU", "OFFLINE_NET", "DEGRADED_NET", "QUOTA_EXHAUSTED", "RESET"
	CPUUsage       *float64              `json:"cpuUsage,omitempty"`
	RAMUsage       *float64              `json:"ramUsage,omitempty"`
	RAMAvailableMb *uint64               `json:"ramAvailableMb,omitempty"`
	NetworkStatus  *config.NetworkStatus `json:"networkStatus,omitempty"`
	QuotaRemaining *int                  `json:"quotaRemaining,omitempty"`
}

// HandleSimulate allows on-the-fly hardware/network stress testing (POST /simulate/condition)
func (h *Handler) HandleSimulate(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var simReq SimulationRequest
	if err := json.NewDecoder(r.Body).Decode(&simReq); err != nil {
		http.Error(w, "Invalid simulation body: "+err.Error(), http.StatusBadRequest)
		return
	}

	switch simReq.Mode {
	case "RESET", "NORMAL":
		h.engine.GetResourceMonitor().ClearSimulationOverride()
		h.engine.GetNetworkManager().Reset()
		h.engine.GetQuotaManager().Reset()

	case "HIGH_RAM":
		// Simulates 85% RAM pressure, 1020 MB free
		h.engine.GetResourceMonitor().SetSimulationOverride(&resources.ResourceState{
			CPUUsage:       45.0,
			RAMUsage:       85.0,
			RAMTotalMb:     8192,
			RAMAvailableMb: 1020,
			GPUAvailable:   false,
			GPUUsage:       0,
			NetworkStatus:  config.NetworkGood,
			LatencyMs:      25.0,
			BandwidthKbps:  50000.0,
		})

	case "HIGH_CPU":
		// Simulates 92% CPU pressure
		h.engine.GetResourceMonitor().SetSimulationOverride(&resources.ResourceState{
			CPUUsage:       92.0,
			RAMUsage:       55.0,
			RAMTotalMb:     8192,
			RAMAvailableMb: 3600,
			GPUAvailable:   false,
			GPUUsage:       0,
			NetworkStatus:  config.NetworkGood,
			LatencyMs:      25.0,
			BandwidthKbps:  50000.0,
		})

	case "OFFLINE_NET":
		h.engine.GetNetworkManager().SetStatus(config.NetworkOffline)

	case "DEGRADED_NET":
		h.engine.GetNetworkManager().SetStatus(config.NetworkDegraded)

	case "POOR_NET":
		h.engine.GetNetworkManager().SetStatus(config.NetworkPoor)

	case "QUOTA_EXHAUSTED":
		h.engine.GetQuotaManager().SetRemaining(0)

	case "CUSTOM":
		current := h.engine.GetResourceMonitor().GetCurrentState()
		if simReq.CPUUsage != nil {
			current.CPUUsage = *simReq.CPUUsage
		}
		if simReq.RAMUsage != nil {
			current.RAMUsage = *simReq.RAMUsage
		}
		if simReq.RAMAvailableMb != nil {
			current.RAMAvailableMb = *simReq.RAMAvailableMb
		}
		if simReq.NetworkStatus != nil {
			current.NetworkStatus = *simReq.NetworkStatus
			h.engine.GetNetworkManager().SetStatus(*simReq.NetworkStatus)
		}
		if simReq.QuotaRemaining != nil {
			h.engine.GetQuotaManager().SetRemaining(*simReq.QuotaRemaining)
		}
		h.engine.GetResourceMonitor().SetSimulationOverride(&current)
	}

	state := h.engine.GetResourceMonitor().GetCurrentState()
	writeJSON(w, http.StatusOK, map[string]interface{}{
		"status":        "SIMULATION_UPDATED",
		"mode":          simReq.Mode,
		"resourceState": state,
		"quota":         h.engine.GetQuotaManager().Summary(),
	})
}

// HandleBenchmarkCompare runs side-by-side comparison with FIXED_EXECUTION baseline (POST /benchmark/compare)
func (h *Handler) HandleBenchmarkCompare(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req router.RouteRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		req = router.RouteRequest{
			WorkloadType: config.WorkloadAnswerEvaluation,
			Complexity:   config.ComplexityHigh,
			Priority:     config.PriorityHigh,
		}
	}

	fixedPathStr := r.URL.Query().Get("fixedPath")
	fixedPath := config.ExecutionPathType(fixedPathStr)
	if fixedPath == "" {
		fixedPath = config.PathHigherCapabilityLocal
	}

	result, err := h.engine.CompareWithBaseline(r.Context(), &req, fixedPath)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, result)
}

// HandleClearCache clears the in-memory cache (POST /cache/clear)
func (h *Handler) HandleClearCache(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	h.engine.GetCache().Clear()
	writeJSON(w, http.StatusOK, map[string]string{
		"status":  "CACHE_CLEARED",
		"message": "LRU cache successfully flushed",
	})
}

func writeJSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(data)
}
