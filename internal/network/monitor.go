package network

import (
	"sync"
	"time"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
)

// NetworkManager manages network connectivity state, latency estimation, and simulation
type NetworkManager struct {
	mu           sync.RWMutex
	currentStatus config.NetworkStatus
	simLatencyMs float64
	simBandwidth float64
	lastUpdated  time.Time
}

// NewNetworkManager initializes network state to GOOD by default
func NewNetworkManager() *NetworkManager {
	return &NetworkManager{
		currentStatus: config.NetworkGood,
		simLatencyMs: 25.0,
		simBandwidth: 50000.0, // 50 Mbps
		lastUpdated:  time.Now(),
	}
}

// GetStatus returns the current network status
func (nm *NetworkManager) GetStatus() config.NetworkStatus {
	nm.mu.RLock()
	defer nm.mu.RUnlock()
	return nm.currentStatus
}

// GetLatencyMs returns the current latency in milliseconds
func (nm *NetworkManager) GetLatencyMs() float64 {
	nm.mu.RLock()
	defer nm.mu.RUnlock()
	return nm.simLatencyMs
}

// IsAvailable returns true if network is not OFFLINE
func (nm *NetworkManager) IsAvailable() bool {
	nm.mu.RLock()
	defer nm.mu.RUnlock()
	return nm.currentStatus != config.NetworkOffline
}

// SetStatus overrides network state for simulation (GOOD, DEGRADED, POOR, OFFLINE)
func (nm *NetworkManager) SetStatus(status config.NetworkStatus) {
	nm.mu.Lock()
	defer nm.mu.Unlock()

	nm.currentStatus = status
	nm.lastUpdated = time.Now()

	switch status {
	case config.NetworkGood:
		nm.simLatencyMs = 25.0
		nm.simBandwidth = 50000.0
	case config.NetworkDegraded:
		nm.simLatencyMs = 150.0
		nm.simBandwidth = 8000.0
	case config.NetworkPoor:
		nm.simLatencyMs = 450.0
		nm.simBandwidth = 1500.0
	case config.NetworkOffline:
		nm.simLatencyMs = 99999.0
		nm.simBandwidth = 0.0
	}
}

// Reset restores default GOOD state
func (nm *NetworkManager) Reset() {
	nm.SetStatus(config.NetworkGood)
}

// StateSummary returns a structured map of current network properties
func (nm *NetworkManager) StateSummary() map[string]interface{} {
	nm.mu.RLock()
	defer nm.mu.RUnlock()

	return map[string]interface{}{
		"status":      nm.currentStatus,
		"latencyMs":   nm.simLatencyMs,
		"bandwidth":   nm.simBandwidth,
		"isAvailable": nm.currentStatus != config.NetworkOffline,
		"lastUpdated": nm.lastUpdated.Format(time.RFC3339),
	}
}
