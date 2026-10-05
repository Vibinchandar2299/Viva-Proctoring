package cache

import (
	"container/list"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"sync"
	"time"
)

// Entry represents an item in the LRU cache
type Entry struct {
	Key          string      `json:"key"`
	WorkloadType string      `json:"workloadType"`
	Result       interface{} `json:"result"`
	CreatedAt    time.Time   `json:"createdAt"`
	LastAccessed time.Time   `json:"lastAccessed"`
	ExecutionMs  float64     `json:"executionMs"`
}

type cacheElement struct {
	key       string
	value     *Entry
	expiresAt time.Time
}

// LRUCache is a concurrency-safe in-memory LRU cache with TTL support
type LRUCache struct {
	mu         sync.RWMutex
	capacity   int
	ttl        time.Duration
	items      map[string]*list.Element
	evictList  *list.List
	totalHits  uint64
	totalMisses uint64
}

// NewLRUCache creates an LRU cache with the specified capacity and TTL
func NewLRUCache(capacity int, ttl time.Duration) *LRUCache {
	if capacity <= 0 {
		capacity = 500
	}
	return &LRUCache{
		capacity:  capacity,
		ttl:       ttl,
		items:     make(map[string]*list.Element),
		evictList: list.New(),
	}
}

// GenerateKey generates a deterministic SHA-256 cache key from workloadType and input payload
func GenerateKey(workloadType string, payload interface{}) string {
	hasher := sha256.New()
	hasher.Write([]byte(workloadType))
	hasher.Write([]byte(":"))

	if payload != nil {
		if bytes, err := json.Marshal(payload); err == nil {
			hasher.Write(bytes)
		} else {
			hasher.Write([]byte("default-payload"))
		}
	} else {
		hasher.Write([]byte("empty-payload"))
	}

	return hex.EncodeToString(hasher.Sum(nil))
}

// Get looks up a key in the cache. Returns entry, found, isExpired
func (c *LRUCache) Get(key string) (*Entry, bool) {
	c.mu.Lock()
	defer c.mu.Unlock()

	elem, exists := c.items[key]
	if !exists {
		c.totalMisses++
		return nil, false
	}

	item := elem.Value.(*cacheElement)
	if !item.expiresAt.IsZero() && time.Now().After(item.expiresAt) {
		// Expired
		c.evictElement(elem)
		c.totalMisses++
		return nil, false
	}

	// Move to front (most recently used)
	c.evictList.MoveToFront(elem)
	item.value.LastAccessed = time.Now()
	c.totalHits++

	return item.value, true
}

// Set adds or updates an entry in the LRU cache
func (c *LRUCache) Set(key string, workloadType string, result interface{}, executionMs float64) {
	c.mu.Lock()
	defer c.mu.Unlock()

	var expiresAt time.Time
	if c.ttl > 0 {
		expiresAt = time.Now().Add(c.ttl)
	}

	// Update existing
	if elem, exists := c.items[key]; exists {
		c.evictList.MoveToFront(elem)
		item := elem.Value.(*cacheElement)
		item.expiresAt = expiresAt
		item.value.Result = result
		item.value.LastAccessed = time.Now()
		item.value.ExecutionMs = executionMs
		return
	}

	// Evict oldest if full
	if c.evictList.Len() >= c.capacity {
		c.evictOldest()
	}

	entry := &Entry{
		Key:          key,
		WorkloadType: workloadType,
		Result:       result,
		CreatedAt:    time.Now(),
		LastAccessed: time.Now(),
		ExecutionMs:  executionMs,
	}

	elem := c.evictList.PushFront(&cacheElement{
		key:       key,
		value:     entry,
		expiresAt: expiresAt,
	})
	c.items[key] = elem
}

func (c *LRUCache) evictOldest() {
	elem := c.evictList.Back()
	if elem != nil {
		c.evictElement(elem)
	}
}

func (c *LRUCache) evictElement(elem *list.Element) {
	c.evictList.Remove(elem)
	item := elem.Value.(*cacheElement)
	delete(c.items, item.key)
}

// Len returns current number of entries
func (c *LRUCache) Len() int {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.evictList.Len()
}

// Stats returns cache statistics
func (c *LRUCache) Stats() map[string]interface{} {
	c.mu.RLock()
	defer c.mu.RUnlock()

	hitRate := 0.0
	total := c.totalHits + c.totalMisses
	if total > 0 {
		hitRate = float64(c.totalHits) / float64(total) * 100.0
	}

	return map[string]interface{}{
		"size":        c.evictList.Len(),
		"capacity":    c.capacity,
		"hits":        c.totalHits,
		"misses":      c.totalMisses,
		"hitRatePct":  hitRate,
	}
}

// Clear flushes all entries
func (c *LRUCache) Clear() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.items = make(map[string]*list.Element)
	c.evictList.Init()
}
