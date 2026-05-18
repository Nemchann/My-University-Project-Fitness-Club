package service

import (
	"context"
	"fitness-proxy/internal/model"
	"fitness-proxy/internal/repository"
	"log"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
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
	cacheRepository repository.CacheRepository
}

func NewCacheManager(defaultTTL time.Duration, repository repository.CacheRepository) *CacheManager {
	return &CacheManager{
		storage:    make(map[string]CacheItem),
		defaultTTL: defaultTTL,
		cacheRepository: repository,
	}
}

func (m *CacheManager) LoadSettings() {
    settings, err := m.cacheRepository.GetSettings(context.Background())
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

func (c *CacheManager) GetTTLForPathRAM(path string) time.Duration {
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

func (c *CacheManager) GetPathSettingsByID(id string) (model.CacheSetting, error) {
	setting, err := c.cacheRepository.GetByID(context.Background(), id)
	if err != nil {
		return model.CacheSetting{}, err
	}
	return *setting, nil
}

func (m *CacheManager) DeleteFromRAMByPath(pathPrefix string) int {
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

func (m *CacheManager) DeleteByID(ctx context.Context, id string) error {
	err := m.cacheRepository.DeleteByID(ctx, id)

	return err
}

func (m *CacheManager) UpdateTTL(ctx context.Context, id string, ttl int64) error {
	objID, err := primitive.ObjectIDFromHex(id)

	if err != nil{
		return err
	}
	
	m.cacheRepository.UpdateTTL(ctx, objID, ttl)

	m.LoadSettings() // Заодно подгружаем настройки кеша

	return nil
}

func (m *CacheManager) GetTTLForPath(ctx context.Context, path string) (int, error) {
	ttl, err := m.cacheRepository.GetTTLForPath(ctx, path)
	if err != nil {
		return 0, err
	}
	return ttl, nil
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