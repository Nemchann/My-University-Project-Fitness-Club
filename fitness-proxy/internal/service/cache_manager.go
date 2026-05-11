package service

import (
	"time"
	"sync"
)

type CacheItem struct {
    Data      []byte
    ExpiresAt time.Time
}

type CacheManager struct {
    storage map[string]CacheItem
    mu      sync.RWMutex
	defaultTTL time.Duration
}

func NewCacheManager(defaultTTL time.Duration) *CacheManager {
	return &CacheManager{
		storage:    make(map[string]CacheItem),
		defaultTTL: defaultTTL,
	}
}

// Set сохраняет данные в кеш
func (c *CacheManager) Set(key string, data []byte) {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.storage[key] = CacheItem{
		Data:      data,
		ExpiresAt: time.Now().Add(c.defaultTTL),
	}
}

// Get извлекает данные, если они еще живы
func (c *CacheManager) Get(key string) ([]byte, bool) {
	c.mu.RLock()
	item, exists := c.storage[key]
	c.mu.RUnlock()

	if !exists {
		return nil, false
	}

	// Проверяем, не "протухли" ли данные
	if time.Now().After(item.ExpiresAt) {
		c.Delete(key) // Удаляем старье
		return nil, false
	}

	return item.Data, true
}

func (c *CacheManager) Delete(key string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	delete(c.storage, key)
}

func (c *CacheManager) Flush() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.storage = make(map[string]CacheItem)
}

