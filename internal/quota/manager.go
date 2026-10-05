package quota

import (
	"sync"
)

// QuotaManager tracks simulated cloud API quota and cost
type QuotaManager struct {
	mu             sync.RWMutex
	quotaLimit     int
	quotaRemaining int
	costPerRequest float64
	totalSpent     float64
	totalConsumed  int
}

// NewQuotaManager creates a new quota manager
func NewQuotaManager(limit int, costPerRequest float64) *QuotaManager {
	if limit <= 0 {
		limit = 50
	}
	if costPerRequest <= 0 {
		costPerRequest = 0.05
	}
	return &QuotaManager{
		quotaLimit:     limit,
		quotaRemaining: limit,
		costPerRequest: costPerRequest,
		totalSpent:     0.0,
		totalConsumed:  0,
	}
}

// HasQuota checks if cloud quota is available
func (qm *QuotaManager) HasQuota() bool {
	qm.mu.RLock()
	defer qm.mu.RUnlock()
	return qm.quotaRemaining > 0
}

// ConsumeQuota deducts 1 request from quota and adds to total spent
func (qm *QuotaManager) ConsumeQuota() bool {
	qm.mu.Lock()
	defer qm.mu.Unlock()

	if qm.quotaRemaining <= 0 {
		return false
	}

	qm.quotaRemaining--
	qm.totalConsumed++
	qm.totalSpent += qm.costPerRequest
	return true
}

// SetRemaining manually overrides remaining quota (useful for testing quota exhaustion)
func (qm *QuotaManager) SetRemaining(remaining int) {
	qm.mu.Lock()
	defer qm.mu.Unlock()
	if remaining < 0 {
		remaining = 0
	}
	qm.quotaRemaining = remaining
}

// Reset resets remaining quota back to quotaLimit
func (qm *QuotaManager) Reset() {
	qm.mu.Lock()
	defer qm.mu.Unlock()
	qm.quotaRemaining = qm.quotaLimit
	qm.totalSpent = 0.0
	qm.totalConsumed = 0
}

// GetCostPerRequest returns cost per request
func (qm *QuotaManager) GetCostPerRequest() float64 {
	qm.mu.RLock()
	defer qm.mu.RUnlock()
	return qm.costPerRequest
}

// Summary returns current quota metrics
func (qm *QuotaManager) Summary() map[string]interface{} {
	qm.mu.RLock()
	defer qm.mu.RUnlock()

	return map[string]interface{}{
		"quotaLimit":     qm.quotaLimit,
		"quotaRemaining": qm.quotaRemaining,
		"costPerRequest": qm.costPerRequest,
		"totalSpent":     qm.totalSpent,
		"totalConsumed":  qm.totalConsumed,
		"isExhausted":    qm.quotaRemaining <= 0,
	}
}
