package main

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/api"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/cache"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/feasibility"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/metrics"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/network"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/paths"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/quota"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/resources"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/router"
	"github.com/Vibinchandar2299/Viva-Proctoring/internal/scoring"
)

const banner = `
================================================================================
   AI-ROUTE VIVA — Low-Cost Resource-Aware AI Request Router
   HackWithAMYPO 2026 | PS1 Engine (Integrated with PS5 Viva Proctoring)
================================================================================
   Design : Rule-Based + Resource-Aware + Multi-Objective + Dynamic (No ML model)
   Target : Indian College Lab (~8 GB RAM, CPU-friendly, Offline-tolerant)
================================================================================
`

func main() {
	// 1. Structured Logging
	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelInfo,
	}))
	slog.SetDefault(logger)

	fmt.Print(banner)

	// 2. Load Configuration
	cfgPath := os.Getenv("CONFIG_PATH")
	cfg := config.LoadConfig(cfgPath)

	slog.Info("Configuration loaded",
		"port", cfg.ServerPort,
		"springBootUrl", cfg.SpringBootURL,
		"mockExecution", cfg.MockExecution,
		"quotaLimit", cfg.SimulatedQuotaLimit,
	)

	// 3. Initialize Core Subsystems
	lruCache := cache.NewLRUCache(cfg.CacheMaxEntries, time.Duration(cfg.CacheTTLSeconds)*time.Second)
	quotaMgr := quota.NewQuotaManager(cfg.SimulatedQuotaLimit, cfg.SimulatedCostPerRequest)
	networkMgr := network.NewNetworkManager()
	pathRegistry := paths.NewPathRegistry(cfg)
	resMonitor := resources.NewResourceMonitor(cfg)
	resMonitor.Start(2 * time.Second) // Live sampling every 2 seconds
	defer resMonitor.Stop()

	// 4. Initialize Decision Pipeline Components
	feasFilter := feasibility.NewFilter(cfg, pathRegistry, lruCache, networkMgr, quotaMgr)
	scorer := scoring.NewScorer(cfg)
	tracker := metrics.NewTracker(1000)
	coordinator := paths.NewExecutionCoordinator(cfg, lruCache, quotaMgr, networkMgr)

	// 5. Build Routing Engine
	engine := router.NewEngine(
		cfg,
		resMonitor,
		feasFilter,
		scorer,
		pathRegistry,
		coordinator,
		lruCache,
		networkMgr,
		quotaMgr,
		tracker,
	)

	// 6. Setup HTTP API Server
	apiHandler := api.NewHandler(engine, cfg)
	httpHandler := api.SetupRouter(apiHandler)

	server := &http.Server{
		Addr:         fmt.Sprintf(":%d", cfg.ServerPort),
		Handler:      httpHandler,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// 7. Graceful Shutdown Setup
	stopChan := make(chan os.Signal, 1)
	signal.Notify(stopChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		slog.Info("AI-ROUTE VIVA Go Router listening", "address", fmt.Sprintf("http://localhost:%d", cfg.ServerPort))
		slog.Info("Endpoints ready: POST /route | GET /resources | GET /health | GET /metrics | GET /routing/history")
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			slog.Error("Server terminated unexpectedly", "error", err)
			os.Exit(1)
		}
	}()

	<-stopChan
	slog.Info("Shutting down AI-ROUTE VIVA service gracefully...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		slog.Error("Forced shutdown error", "error", err)
	}

	slog.Info("Service stopped cleanly")
}
