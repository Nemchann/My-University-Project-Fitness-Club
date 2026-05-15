package service

import (
	"context"
	"fitness-proxy/internal/model"
	"fitness-proxy/internal/repository"
	"log"
	"sync"
	"time"
	"strings"
	"sync/atomic"
)

type CacheItem struct {
    Data      []byte
    ExpiresAt time.Time
}

type CacheManager struct {
    storage map[string]CacheItem
	pathSettings map[string]time.Duration // Храним тут наши TTL из базы
    mu      sync.RWMutex
	defaultTTL time.Duration
	cachedCount atomic.Int64
}

func NewCacheManager(defaultTTL time.Duration) *CacheManager {
	return &CacheManager{
		storage:    make(map[string]CacheItem),
		defaultTTL: defaultTTL,
	}
}

func (m *CacheManager) LoadSettings(repo *repository.MongoCacheRepo) {
    settings, err := repo.GetSettings(context.Background())
    if err != nil {
        log.Printf("Ошибка загрузки настроек кеша: %v", err)
        return
    }

    newSettings := make(map[string]time.Duration)
    for _, s := range settings {
        newSettings[s.Path] = time.Duration(s.TTLSeconds) * time.Second
    }

    m.mu.Lock()
    m.pathSettings = newSettings
    m.mu.Unlock()
    log.Println("Настройки кеша обновлены из БД")
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

func (c *CacheManager) GetTTLForPath(path string) time.Duration {
	c.mu.RLock()
	ttl, exists := c.pathSettings[path]
	c.mu.RUnlock()

	if !exists {
		return c.defaultTTL
	}

	return ttl
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

func (c *CacheManager) GetPathSettingsByID(id string, repo *repository.MongoCacheRepo) (model.CacheSetting, error) {
	setting, err := repo.GetByID(context.Background(), id)
	if err != nil {
		return model.CacheSetting{}, err
	}
	return *setting, nil
}

func (m *CacheManager) DeleteByPath(pathPrefix string) int {
	m.mu.Lock()
	defer m.mu.Unlock()
	
	deletedCount := 0
	for key := range m.storage {
		if strings.Contains(key, pathPrefix) {
			delete(m.storage, key)
			deletedCount++
		}
	}
	return deletedCount
}

func (m *CacheManager) UpdateTTL(id string, ttl int, repo *repository.MongoCacheRepo) {
	m.mu.Lock()
	defer m.mu.Unlock()

	repo.UpdateTTL(context.Background(), id, ttl)

	m.LoadSettings(repo) // Заодно подгружаем настройки кеша
}

func (m  *CacheManager) GetKeysCount() int{
	return len(m.pathSettings)
}

func (m *CacheManager) IncrementCachedCount() {
	m.cachedCount.Add(1)
}

func (m *CacheManager) GetHitRate() int{
	return int(m.cachedCount.Load())
}