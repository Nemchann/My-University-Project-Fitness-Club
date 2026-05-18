package middleware_test

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"fitness-proxy/internal/repository/mocks"
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
	"golang.org/x/time/rate"
)

//Тестировать: go test ./internal/middleware -coverprofile=coverage.out
// Допустим, твое Middleware выглядит примерно так и принимает менеджер лимитов и менеджер IP.
// Если оно устроено немного иначе, просто адаптируй вызов.
func NewRateLimitMiddleware(limiterManager *service.IPRateLimiter, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		clientIP := c.ClientIP()
		
		// 1. Определяем статус IP через IPManager (white, grey, black, default)
		_, ipType := ipManager.IsAllowed(net.ParseIP(clientIP)) 
		
		var limits *service.IPLimiters
		
		// 2. Выбираем константы в зависимости от типа IP
		switch ipType {
		case "whitelisted":
			limits = limiterManager.GetLimiters(clientIP, 50.0, 1500, 30000, 100000, 100)
		case "grey":
			limits = limiterManager.GetLimiters(clientIP, 0.5, 10, 50, 200, 2)
		default: // default и black (хотя black должен отсекаться раньше другим middleware)
			limits = limiterManager.GetLimiters(clientIP, 5.0, 60, 1000, 10000, 10)
		}

		// 3. Проверяем все 4 лимита
		if !limits.Second.Allow() || !limits.Minute.Allow() || !limits.Hour.Allow() || !limits.Day.Allow() {
			c.JSON(http.StatusTooManyRequests, gin.H{"error": "Too many requests"})
			c.Abort()
			return
		}

		c.Next()
	}
}

func TestRateLimitMiddleware(t *testing.T) {
	gin.SetMode(gin.TestMode)

	// 1. Создаем контроллер моков
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// 2. Создаем мок для IP-репозитория (замени путь на твой пакет mocks)
	mockIPRepo := mocks.NewMockIPRepository(ctrl)

	// 3. Обучаем мок: когда во втором тесте вызовется AddRule, 
	// репозиторий должен просто сказать "успешно, записал" (Return(nil))
	// Используем gomock.Any(), чтобы не привязываться к жестким строкам
	mockIPRepo.EXPECT().
		InsertRule(gomock.Any(), gomock.Any(), gomock.Any()).
		Return(nil).
		AnyTimes() // Позволяет вызывать метод сколько угодно раз

	// 4. Передаем наш обученный мок в IPManager вместо nil!
	ipManager := service.NewIPManager(mockIPRepo) 
	
	// Оставляем лимитер как есть (он работает в RAM и моки не требует)
	limiterManager := service.NewIPRateLimiter(rate.Limit(10), 10)

	// Создаем тестовый роутер Gin и вешаем наше Middleware
	r := gin.New()
	r.Use(NewRateLimitMiddleware(limiterManager, ipManager))
	r.GET("/test", func(c *gin.Context) {
		c.String(http.StatusOK, "success")
	})
	t.Run("1. Обычный запрос под лимитом — Пропускает (200 OK)", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodGet, "/test", nil)
		req.RemoteAddr = "192.168.1.100:1234" // Имитируем IP обычного пользователя
		w := httptest.NewRecorder()

		r.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		assert.Equal(t, "success", w.Body.String())
	})

	t.Run("2. Превышение лимита для Серых IP — Блокирует (429 Too Many Requests)", func(t *testing.T) {
		// Искусственно сделаем этот IP серым (через AddRule или напрямую в зависимости от твоего кода)
		_ = ipManager.AddRule("10.0.0.5/32", "grey") 

		// Серый лимит в минуту = 10. Сделаем 11 запросов подряд!
		for i := 0; i < 11; i++ {
			req := httptest.NewRequest(http.MethodGet, "/test", nil)
			req.RemoteAddr = "10.0.0.5:1234"
			w := httptest.NewRecorder()
			
			r.ServeHTTP(w, req)

			if i < 10 {
				// Первые 10 запросов (в рамках лимита) должны пройти
				assert.Equal(t, http.StatusOK, w.Code)
			} else {
				// 11-й запрос должен наткнуться на отказ!
				assert.Equal(t, http.StatusTooManyRequests, w.Code)
			}
		}
	})
}