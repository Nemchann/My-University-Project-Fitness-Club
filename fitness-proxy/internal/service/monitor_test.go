package service_test

import (
	//"sync"
	"testing"
	"fitness-proxy/internal/service"
	"github.com/stretchr/testify/assert"
	"net/http"
	"net/http/httptest"
	"github.com/gin-gonic/gin"
)

func TestMonitor_BasicMetricsAndLatency(t *testing.T) {
	m := service.NewMonitor()

	// 1. Проверяем начальные значения через геттеры
	assert.Equal(t, int64(0), m.GetTotalRequests())
	assert.Equal(t, int64(0), m.GetActiveConnections())
	assert.Equal(t, int64(0), m.GetTotalTrafficBytes())
	assert.Equal(t, int64(0), m.GetAverageResponseTime())

	// 2. Тестируем расчет Latency (скользящее среднее)
	// Первый замер — должен установиться как есть
	m.UpdateLatency(100)
	assert.Equal(t, int64(100), m.GetAverageResponseTime())

	// Второй замер — должен рассчитаться по формуле (100*9 + 50) / 10 = 95
	m.UpdateLatency(50)
	assert.Equal(t, int64(95), m.GetAverageResponseTime())
	
	// Метод-дубликат GetLatency тоже должен вернуть 95
	assert.Equal(t, int64(95), m.GetLatency())
}

func TestMonitor_HistoryAndRPM(t *testing.T) {
	m := service.NewMonitor()

	// Напрямую через историю (так как это срез внутри структуры) мы проверить не можем, 
	// но мы можем проверить GetRequestsPerMinute, если в истории появятся данные.
	// Для Unit-теста мы можем проверить, что история возвращает корректные копии срезов.
	historyRPS := m.GetRPSHistory()
	historyTraffic := m.GetTrafficHistory()

	assert.Len(t, historyRPS, 60)
	assert.Len(t, historyTraffic, 60)

	// Проверим, что GetRequestsPerMinute корректно считает среднее значение.
	// Изначально там нули, среднее 0.
	assert.Equal(t, int64(0), m.GetRequestsPerMinute())
}

func TestMonitor_RecordClientActivity(t *testing.T) {
	m := service.NewMonitor()
	ip := "192.168.1.50"

	// 1. Первый запрос от клиента (обычный, без блокировок)
	m.RecordClientActivity(ip, 500, "")

	clientsMap := m.GetClientsMap()
	val, exists := clientsMap.Load(ip)
	assert.True(t, exists)

	stats := val.(*service.ClientStats)
	assert.Equal(t, ip, stats.IP)
	assert.Equal(t, int64(1), stats.TotalRequests)
	assert.Equal(t, int64(500), stats.BytesTransferred)
	assert.Equal(t, int64(0), stats.BlockedBlacklist)

	// 2. Второй запрос от того же клиента — попал под раздачу лимитера (rate_limit)
	m.RecordClientActivity(ip, 0, "rate_limit")
	assert.Equal(t, int64(2), stats.TotalRequests)
	assert.Equal(t, int64(1), stats.BlockedRateLimit)

	// 3. Третий запрос — забанен по блеклисту (blacklist)
	m.RecordClientActivity(ip, 0, "blacklist")
	assert.Equal(t, int64(3), stats.TotalRequests)
	assert.Equal(t, int64(1), stats.BlockedBlacklist)
}

func TestMonitor_Middleware(t *testing.T) {
	gin.SetMode(gin.TestMode)
	m := service.NewMonitor()

	r := gin.New()
	r.Use(m.Middleware())
	r.GET("/ping", func(c *gin.Context) {
		// Имитируем, что IPFilter передал статус блокировки лимитера
		c.Set("block_reason", "rate_limit")
		c.String(http.StatusOK, "pong")
	})

	req := httptest.NewRequest(http.MethodGet, "/ping", nil)
	req.RemoteAddr = "172.20.10.2:1234"
	w := httptest.NewRecorder()

	r.ServeHTTP(w, req)

	// Проверяем, что глобальные счетчики монитора увеличились
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, int64(1), m.GetTotalRequests())
	assert.GreaterOrEqual(t, m.GetTotalTrafficBytes(), int64(0))

	// Проверяем, что внутри middleware зафиксировалась активность по IP
	val, exists := m.GetClientsMap().Load("172.20.10.2")
	assert.True(t, exists)
	
	stats := val.(*service.ClientStats)
	assert.Equal(t, int64(1), stats.TotalRequests)
	assert.Equal(t, int64(1), stats.BlockedRateLimit)
}