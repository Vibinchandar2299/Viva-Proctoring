package api

import (
	"log/slog"
	"net/http"
	"time"
)

// SetupRouter registers all HTTP routes and middleware
func SetupRouter(h *Handler) http.Handler {
	mux := http.NewServeMux()

	// Core API Endpoints
	mux.HandleFunc("/health", h.HandleHealth)
	mux.HandleFunc("/resources", h.HandleResources)
	mux.HandleFunc("/route", h.HandleRoute)
	mux.HandleFunc("/metrics", h.HandleMetrics)
	mux.HandleFunc("/routing/history", h.HandleHistory)

	// Simulation and Benchmark Extensions
	mux.HandleFunc("/simulate/condition", h.HandleSimulate)
	mux.HandleFunc("/benchmark/compare", h.HandleBenchmarkCompare)
	mux.HandleFunc("/cache/clear", h.HandleClearCache)

	// Wrap in logging and CORS middleware
	return loggingMiddleware(corsMiddleware(mux))
}

func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func loggingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		next.ServeHTTP(w, r)
		duration := time.Since(start)

		if r.URL.Path != "/health" && r.URL.Path != "/resources" {
			slog.Debug("HTTP Request",
				"method", r.Method,
				"path", r.URL.Path,
				"remote", r.RemoteAddr,
				"durationMs", float64(duration.Microseconds())/1000.0,
			)
		}
	})
}
