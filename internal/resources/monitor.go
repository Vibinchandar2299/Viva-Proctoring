package resources

import (
	"context"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"sync"
	"syscall"
	"time"
	"unsafe"

	"github.com/Vibinchandar2299/Viva-Proctoring/internal/config"
)

// PressureLevel categorizes resource saturation
type PressureLevel string

const (
	PressureLow    PressureLevel = "LOW"
	PressureMedium PressureLevel = "MEDIUM"
	PressureHigh   PressureLevel = "HIGH"
)

// ResourceState models the instantaneous hardware & network conditions
type ResourceState struct {
	CPUUsage       float64              `json:"cpuUsage"`       // 0 - 100 percentage
	RAMUsage       float64              `json:"ramUsage"`       // 0 - 100 percentage
	RAMTotalMb     uint64               `json:"ramTotalMb"`     // Total RAM in MB
	RAMAvailableMb uint64               `json:"ramAvailableMb"` // Free/Available RAM in MB
	GPUAvailable   bool                 `json:"gpuAvailable"`
	GPUUsage       float64              `json:"gpuUsage"`       // 0 - 100 percentage
	GPUMemoryMb    uint64               `json:"gpuMemoryMb"`
	NetworkStatus  config.NetworkStatus `json:"networkStatus"`  // GOOD, DEGRADED, POOR, OFFLINE
	LatencyMs      float64              `json:"latencyMs"`      // Measured or simulated network latency
	BandwidthKbps  float64              `json:"bandwidthKbps"`
	CPUPressure    PressureLevel        `json:"cpuPressure"`    // LOW, MEDIUM, HIGH
	RAMPressure    PressureLevel        `json:"ramPressure"`    // LOW, MEDIUM, HIGH
	IsSimulated    bool                 `json:"isSimulated"`    // Indicates if overrides are active
	Timestamp      string               `json:"timestamp"`
}

// Windows MEMORYSTATUSEX struct for kernel32 GlobalMemoryStatusEx
type memoryStatusEx struct {
	dwLength                uint32
	dwMemoryLoad            uint32
	ullTotalPhys            uint64
	ullAvailPhys            uint64
	ullTotalPageFile        uint64
	ullAvailPageFile        uint64
	ullTotalVirtual         uint64
	ullAvailVirtual         uint64
	ullAvailExtendedVirtual uint64
}

// ResourceMonitor periodically samples host metrics and supports simulation hooks
type ResourceMonitor struct {
	mu             sync.RWMutex
	cfg            *config.Config
	currentState   ResourceState
	overrideState  *ResourceState
	prevIdleTime   uint64
	prevKernelTime uint64
	prevUserTime   uint64
	stopChan       chan struct{}
}

// NewResourceMonitor initializes the monitor
func NewResourceMonitor(cfg *config.Config) *ResourceMonitor {
	rm := &ResourceMonitor{
		cfg:      cfg,
		stopChan: make(chan struct{}),
	}
	rm.sampleRealHardware()
	return rm
}

// Start begins periodic background sampling
func (rm *ResourceMonitor) Start(interval time.Duration) {
	ticker := time.NewTicker(interval)
	go func() {
		for {
			select {
			case <-ticker.C:
				rm.sampleRealHardware()
			case <-rm.stopChan:
				ticker.Stop()
				return
			}
		}
	}()
}

// Stop terminates the background sampling loop
func (rm *ResourceMonitor) Stop() {
	close(rm.stopChan)
}

// GetCurrentState returns the current resource metrics (override or live)
func (rm *ResourceMonitor) GetCurrentState() ResourceState {
	rm.mu.RLock()
	defer rm.mu.RUnlock()

	if rm.overrideState != nil {
		res := *rm.overrideState
		res.Timestamp = time.Now().UTC().Format(time.RFC3339)
		res.CPUPressure = rm.calculateCPUPressure(res.CPUUsage)
		res.RAMPressure = rm.calculateRAMPressure(res.RAMUsage)
		res.IsSimulated = true
		return res
	}

	state := rm.currentState
	state.Timestamp = time.Now().UTC().Format(time.RFC3339)
	state.CPUPressure = rm.calculateCPUPressure(state.CPUUsage)
	state.RAMPressure = rm.calculateRAMPressure(state.RAMUsage)
	return state
}

// SetSimulationOverride activates a mock state for demonstration or testing
func (rm *ResourceMonitor) SetSimulationOverride(override *ResourceState) {
	rm.mu.Lock()
	defer rm.mu.Unlock()
	rm.overrideState = override
}

// ClearSimulationOverride restores real live hardware polling
func (rm *ResourceMonitor) ClearSimulationOverride() {
	rm.mu.Lock()
	defer rm.mu.Unlock()
	rm.overrideState = nil
}

func (rm *ResourceMonitor) calculateCPUPressure(cpu float64) PressureLevel {
	if cpu >= rm.cfg.Thresholds.CPUMediumThreshold {
		return PressureHigh
	}
	if cpu >= rm.cfg.Thresholds.CPULowThreshold {
		return PressureMedium
	}
	return PressureLow
}

func (rm *ResourceMonitor) calculateRAMPressure(ram float64) PressureLevel {
	if ram >= rm.cfg.Thresholds.RAMMediumThreshold {
		return PressureHigh
	}
	if ram >= rm.cfg.Thresholds.RAMLowThreshold {
		return PressureMedium
	}
	return PressureLow
}

// sampleRealHardware performs real OS-level sampling on Windows / cross-platform
func (rm *ResourceMonitor) sampleRealHardware() {
	var totalMb, availMb uint64
	var ramUsagePct float64
	var cpuUsagePct float64

	if runtime.GOOS == "windows" {
		totalMb, availMb, ramUsagePct = sampleWindowsRAM()
		cpuUsagePct = rm.sampleWindowsCPU()
	} else {
		// Fallback for non-windows
		var memStats runtime.MemStats
		runtime.ReadMemStats(&memStats)
		totalMb = 8192
		allocMb := memStats.Alloc / (1024 * 1024)
		if allocMb > totalMb {
			allocMb = totalMb / 2
		}
		availMb = totalMb - allocMb
		ramUsagePct = float64(allocMb) / float64(totalMb) * 100.0
		cpuUsagePct = 25.0
	}

	gpuAvailable, gpuUsage, gpuMem := sampleGPU()

	rm.mu.Lock()
	defer rm.mu.Unlock()

	rm.currentState = ResourceState{
		CPUUsage:       cpuUsagePct,
		RAMUsage:       ramUsagePct,
		RAMTotalMb:     totalMb,
		RAMAvailableMb: availMb,
		GPUAvailable:   gpuAvailable,
		GPUUsage:       gpuUsage,
		GPUMemoryMb:    gpuMem,
		NetworkStatus:  config.NetworkGood,
		LatencyMs:      24.0,
		BandwidthKbps:  50000.0,
		IsSimulated:    false,
		Timestamp:      time.Now().UTC().Format(time.RFC3339),
	}
}

func sampleWindowsRAM() (uint64, uint64, float64) {
	kernel32 := syscall.NewLazyDLL("kernel32.dll")
	globalMemoryStatusEx := kernel32.NewProc("GlobalMemoryStatusEx")

	var msx memoryStatusEx
	msx.dwLength = uint32(unsafe.Sizeof(msx))

	ret, _, _ := globalMemoryStatusEx.Call(uintptr(unsafe.Pointer(&msx)))
	if ret == 0 {
		return 8192, 4096, 50.0
	}

	totalMb := msx.ullTotalPhys / (1024 * 1024)
	availMb := msx.ullAvailPhys / (1024 * 1024)
	usagePct := float64(msx.dwMemoryLoad)

	return totalMb, availMb, usagePct
}

func (rm *ResourceMonitor) sampleWindowsCPU() float64 {
	kernel32 := syscall.NewLazyDLL("kernel32.dll")
	getSystemTimes := kernel32.NewProc("GetSystemTimes")

	var idleTime, kernelTime, userTime syscall.Filetime
	ret, _, _ := getSystemTimes.Call(
		uintptr(unsafe.Pointer(&idleTime)),
		uintptr(unsafe.Pointer(&kernelTime)),
		uintptr(unsafe.Pointer(&userTime)),
	)

	if ret == 0 {
		return 20.0
	}

	idle := (uint64(idleTime.HighDateTime) << 32) + uint64(idleTime.LowDateTime)
	kernel := (uint64(kernelTime.HighDateTime) << 32) + uint64(kernelTime.LowDateTime)
	user := (uint64(userTime.HighDateTime) << 32) + uint64(userTime.LowDateTime)

	if rm.prevIdleTime == 0 {
		rm.prevIdleTime = idle
		rm.prevKernelTime = kernel
		rm.prevUserTime = user
		return 15.0
	}

	deltaIdle := idle - rm.prevIdleTime
	deltaKernel := kernel - rm.prevKernelTime
	deltaUser := user - rm.prevUserTime

	rm.prevIdleTime = idle
	rm.prevKernelTime = kernel
	rm.prevUserTime = user

	totalSystem := deltaKernel + deltaUser
	if totalSystem == 0 {
		return 10.0
	}

	cpuPercent := float64(totalSystem-deltaIdle) / float64(totalSystem) * 100.0
	if cpuPercent < 0 {
		cpuPercent = 0
	} else if cpuPercent > 100 {
		cpuPercent = 100
	}

	return cpuPercent
}

func sampleGPU() (bool, float64, uint64) {
	// Fast check for nvidia-smi with timeout
	ctx, cancel := context.WithTimeout(context.Background(), 800*time.Millisecond)
	defer cancel()

	cmd := exec.CommandContext(ctx, "nvidia-smi", "--query-gpu=utilization.gpu,memory.total", "--format=csv,noheader,nounits")
	output, err := cmd.Output()
	if err != nil {
		return false, 0, 0
	}

	parts := strings.Split(strings.TrimSpace(string(output)), ",")
	if len(parts) >= 2 {
		util, _ := strconv.ParseFloat(strings.TrimSpace(parts[0]), 64)
		mem, _ := strconv.ParseUint(strings.TrimSpace(parts[1]), 10, 64)
		return true, util, mem
	}

	return true, 0, 0
}
