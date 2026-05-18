package service_test

import (
	"fitness-proxy/internal/repository/mocks" // Путь к твоим мокам
	"fitness-proxy/internal/service"
    "fitness-proxy/internal/model"
	"testing"
    "net"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

//Тестирровать: go test ./internal/service -coverprofile=coverage.out
func TestIPManager_IsAllowed(t *testing.T) {
	type testCase struct {
		name           string
		clientIP       string
		setupRules     func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository)
		expectedAllow  bool
		expectedReason string
	}

	tests := []testCase{
		{
			name:     "1. IP в черном списке — отказ",
			clientIP: "192.168.1.100",
			setupRules: func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository) {
				// Обучаем мок разрешать вставку в БД
				mockRepo.EXPECT().InsertRule(gomock.Any(), "192.168.1.100/32", "black").Return(nil)
				_ = mgr.AddRule("192.168.1.100/32", "black")
			},
			expectedAllow:  false,
			expectedReason: "blacklisted",
		},
		{
			name:     "2. IP в белом списке — пропуск",
			clientIP: "10.0.0.1",
			setupRules: func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository) {
				mockRepo.EXPECT().InsertRule(gomock.Any(), "10.0.0.1/32", "white").Return(nil)
				_ = mgr.AddRule("10.0.0.1/32", "white")
			},
			expectedAllow:  true,
			expectedReason: "whitelisted",
		},
		{
			name:     "3. IP в сером списке — пропуск с пометкой grey",
			clientIP: "172.16.0.20",
			setupRules: func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository) {
				mockRepo.EXPECT().InsertRule(gomock.Any(), "172.16.0.20/32", "grey").Return(nil)
				_ = mgr.AddRule("172.16.0.20/32", "grey")
			},
			expectedAllow:  true,
			expectedReason: "grey",
		},
		{
			name:     "4. IP нет в списках — ветка default",
			clientIP: "8.8.8.8",
			setupRules: func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository) {
				// Ничего не добавляем
			},
			expectedAllow:  true,
			expectedReason: "default",
		},
        {
            name:     "5. Подсеть CIDR в черном списке — бан для IP из этой подсети",
            clientIP: "10.0.5.23",
            setupRules: func(mgr *service.IPManager, mockRepo *mocks.MockIPRepository) {
                mockRepo.EXPECT().InsertRule(gomock.Any(), "10.0.0.0/16", "black").Return(nil)
                _ = mgr.AddRule("10.0.0.0/16", "black")
            },
            expectedAllow:  false,
            expectedReason: "blacklisted",
        },
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			ctrl := gomock.NewController(t)
			defer ctrl.Finish()
			mockRepo := mocks.NewMockIPRepository(ctrl)

			ipManager := service.NewIPManager(mockRepo)

			tt.setupRules(ipManager, mockRepo)

			allowed, reason := ipManager.IsAllowed(net.ParseIP(tt.clientIP))

			assert.Equal(t, tt.expectedAllow, allowed)
			assert.Equal(t, tt.expectedReason, reason)
		})
	}
}

func TestIPManager_Reload(t *testing.T) {
    ctrl := gomock.NewController(t)
    defer ctrl.Finish()
    mockRepo := mocks.NewMockIPRepository(ctrl)

    ipManager := service.NewIPManager(mockRepo)

    // Создаем слайс правил, как будто прочитали из конфига или БД
    testRules := []model.IPRule{
        {Network: "192.168.50.0/24", Type: "black"},
        {Network: "1.1.1.1/32", Type: "white"},
    }

    // Вызываем Reload
    err := ipManager.Reload(testRules)
    assert.NoError(t, err)

    // Проверяем, что правила применились
    allowed, reason := ipManager.IsAllowed(net.ParseIP("192.168.50.15"))
    assert.False(t, allowed)
    assert.Equal(t, "blacklisted", reason)
}