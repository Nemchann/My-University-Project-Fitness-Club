package middleware_test

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"fitness-proxy/internal/middleware"
	"fitness-proxy/internal/service"
	"go.uber.org/mock/gomock"
	"fitness-proxy/internal/repository/mocks"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

// Инициализируем тестовый CacheManager
func setupTestCache(t *testing.T) *service.CacheManager {
	// Создаем CacheManager с дефолтным TTL 
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)
	cm := service.NewCacheManager(5 * time.Minute, mockRepo)
	return cm
}

func TestCacheMiddleware_MissAndStore(t *testing.T) {
	gin.SetMode(gin.TestMode)
	cm := setupTestCache(t)

	r := gin.New()
	r.Use(middleware.CacheMiddleware(cm))

	// Кэшируемый путь из твоего массива cacheablePaths
	targetPath := "/api/fitness-club/users/get_all_clients"
	
	// Эмулируем ответ Java-бэкенда при первом запросе (Cache Miss)
	r.GET(targetPath, func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"data": "clients_list"})
	})

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, targetPath, nil)

	req.RequestURI = targetPath

	r.ServeHTTP(w, req)

	// Проверяем первый ответ
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Contains(t, w.Body.String(), "clients_list")

	// Проверяем, что данные сохранились в кэш-менеджер
	// Ключ в middleware формируется через c.Request.RequestURI
	cachedData, found := cm.Get(targetPath)
	assert.True(t, found, "Данные должны сохраниться в кэше")
	assert.Contains(t, string(cachedData), "clients_list")
}

func TestCacheMiddleware_Hit(t *testing.T) {
	gin.SetMode(gin.TestMode)
	cm := setupTestCache(t)

	// Насильно сохраняем подготовленные данные в кэш (Cache Hit)
	targetURI := "/api/fitness-club/users/get_all_trainers"
	expectedJSON := `{"data":"cached_trainers"}`
	cm.Set(targetURI, []byte(expectedJSON))

	r := gin.New()
	r.Use(middleware.CacheMiddleware(cm))

	// Добавляем хендлер. Если middleware сработает неверно и пропустит запрос дальше, 
	// вернется "bad_response", если сработает кэш — мы до хендлера не дойдем.
	r.GET(targetURI, func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"data": "bad_response"})
	})

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, targetURI, nil)

	req.RequestURI = targetURI

	r.ServeHTTP(w, req)

	// Проверяем ответ
	assert.Equal(t, http.StatusOK, w.Code)
	assert.JSONEq(t, expectedJSON, w.Body.String(), "Данные должны были вернуться ИЗ КЭША")
}

func TestCacheMiddleware_NotCacheablePathOrMethod(t *testing.T) {
	gin.SetMode(gin.TestMode)
	cm := setupTestCache(t)

	r := gin.New()
	r.Use(middleware.CacheMiddleware(cm))

	// 1. Некэшируемый путь
	nonCacheablePath := "/api/fitness-club/unsupported_path"
	r.GET(nonCacheablePath, func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"data": "raw"})
	})

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, nonCacheablePath, nil)
	r.ServeHTTP(w, req)

	_, found := cm.Get(nonCacheablePath)
	assert.False(t, found, "Некэшируемый путь не должен сохраняться")

	// 2. Метод POST (не должен кэшироваться)
	cacheablePath := "/api/fitness-club/users/get_all_clients"
	r.POST(cacheablePath, func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "created"})
	})

	w2 := httptest.NewRecorder()
	req2, _ := http.NewRequest(http.MethodPost, cacheablePath, nil)
	r.ServeHTTP(w2, req2)

	_, foundPost := cm.Get(cacheablePath)
	assert.False(t, foundPost, "POST запросы не должны сохраняться в кэш")
}