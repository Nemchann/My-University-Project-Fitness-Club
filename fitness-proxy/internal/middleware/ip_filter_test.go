package middleware_test

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"fitness-proxy/internal/middleware"
	"fitness-proxy/internal/model"
	"fitness-proxy/internal/service"
	"fitness-proxy/internal/repository/mocks" // Укажи правильный путь к мокам репозиториев
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestIPFilter_Middleware(t *testing.T) {
	gin.SetMode(gin.TestMode)

	// Настраиваем GoMock контроллер
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// Создаём заглушки для внешних зависимостей Middleware
	mockIPRepo := mocks.NewMockIPRepository(ctrl)
	
	// Обучаем мок игнорировать InsertRule при добавлении тестовых правил
	mockIPRepo.EXPECT().
		InsertRule(gomock.Any(), gomock.Any(), gomock.Any()).
		Return(nil).
		AnyTimes()

	// Инициализируем менеджеры
	ipManager := service.NewIPManager(mockIPRepo)
	monitor := service.NewMonitor()

	// Та самая заглушка для канала логов
	logChan := make(chan model.AccessLog, 10)

	// Добавляем тестовые правила в оперативную память менеджера
	_ = ipManager.AddRule("192.168.1.1/32", "black")
	_ = ipManager.AddRule("10.0.0.0/24", "white")

	// Настраиваем тестовый роутер
	r := gin.New()
	r.Use(middleware.IPFilter(ipManager, logChan, monitor))
	r.GET("/protected", func(c *gin.Context) {
		c.String(http.StatusOK, "welcome")
	})

	// Описываем тестовые сценарии
	tests := []struct {
		name       string
		clientIP   string
		expectedStatus int
		expectedBody   string
	}{
		{
			name:           "1. IP в черном списке должен получить 403",
			clientIP:       "192.168.1.1:5544",
			expectedStatus: http.StatusForbidden,
			expectedBody:   `"error":"Access denied"`,
		},
		{
			name:           "2. IP в белом списке должен успешно пройти",
			clientIP:       "10.0.0.5:5544",
			expectedStatus: http.StatusOK,
			expectedBody:   "welcome",
		},
		{
			name:           "3. IP без явного правила (default) должен пройти",
			clientIP:       "172.16.0.1:5544",
			expectedStatus: http.StatusOK,
			expectedBody:   "welcome",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest(http.MethodGet, "/protected", nil)
			req.RemoteAddr = tt.clientIP
			w := httptest.NewRecorder()

			r.ServeHTTP(w, req)

			assert.Equal(t, tt.expectedStatus, w.Code)
			assert.Contains(t, w.Body.String(), tt.expectedBody)
		})
	}
}