package middleware_test

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"fitness-proxy/internal/model"
	"fitness-proxy/internal/middleware"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func TestAsyncLogger_Success(t *testing.T) {
	// Переводим Gin в тестовый режим, чтобы не спамил в консоль во время тестов
	gin.SetMode(gin.TestMode)

	// Создаем буферизованный канал для логов
	logChan := make(chan model.AccessLog, 1)

	// Инициализируем роутер Gin и подключаем наше middleware
	r := gin.New()
	r.Use(middleware.AsyncLogger(logChan))

	// Тестовый хендлер, который имитирует успешный ответ от Java-бэкенда
	r.GET("/api/test-path", func(c *gin.Context) {
		c.Set("request_id", "test-123")
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	// Имитируем HTTP-запрос
	req, _ := http.NewRequest(http.MethodGet, "/api/test-path", nil)
	req.RemoteAddr = "192.168.1.50:1234" // Задаем тестовый IP клиента
	w := httptest.NewRecorder()

	// Запускаем обработку запроса
	r.ServeHTTP(w, req)

	// Проверяем HTTP статус ответа
	assert.Equal(t, http.StatusOK, w.Code)

	// Читаем лог из асинхронного канала с таймаутом (чтобы тест не завис, если лог не пришел)
	select {
	case entry := <-logChan:
		// Проверяем корректность заполнения полей структуры лога
		assert.Equal(t, "192.168.1.50", entry.IP)
		assert.Equal(t, "/api/test-path", entry.URL)
		assert.Equal(t, "INFO", entry.Level)
		assert.Equal(t, http.MethodGet, entry.Method)
		assert.Equal(t, "test-123", entry.RequestID)
		assert.Equal(t, http.StatusOK, entry.StatusCode)
		assert.True(t, entry.Latency >= 0)
		assert.WithinDuration(t, time.Now(), entry.Timestamp, 2*time.Second)
	case <-time.After(1 * time.Second):
		t.Fatal("Таймаут: лог не был отправлен в канал logChan")
	}
}

func TestAsyncLogger_AbortedRequest(t *testing.T) {
	gin.SetMode(gin.TestMode)
	logChan := make(chan model.AccessLog, 1)

	r := gin.New()
	r.Use(middleware.AsyncLogger(logChan))

	// Имитируем сценарий, когда IPFilter заблокировал запрос (например, черный список)
	r.GET("/api/blocked", func(c *gin.Context) {
		c.Set("request_id", "test-403")
		c.Set("abort_reason", "blacklisted")
		c.AbortWithStatus(http.StatusForbidden)
	})

	req, _ := http.NewRequest(http.MethodGet, "/api/blocked", nil)
	req.RemoteAddr = "127.0.0.1:5555"
	w := httptest.NewRecorder()

	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusForbidden, w.Code)

	select {
	case entry := <-logChan:
		assert.Equal(t, "127.0.0.1", entry.IP)
		assert.Equal(t, "WARN", entry.Level) // По твоему коду: статус 403 >= 400 — это WARN
		assert.Equal(t, "blacklisted", entry.Reason)
		assert.Equal(t, http.StatusForbidden, entry.StatusCode)
	case <-time.After(1 * time.Second):
		t.Fatal("Таймаут: лог блокировки не пришел")
	}
}

func TestGetLevel(t *testing.T) {
	// Юнит-тест на вспомогательную функцию перевода статусов в уровни
	assert.Equal(t, "ERROR", middleware.GetLevel(500))
	assert.Equal(t, "ERROR", middleware.GetLevel(503))
	assert.Equal(t, "WARN", middleware.GetLevel(400))
	assert.Equal(t, "WARN", middleware.GetLevel(428))
	assert.Equal(t, "INFO", middleware.GetLevel(200))
	assert.Equal(t, "INFO", middleware.GetLevel(302))
}