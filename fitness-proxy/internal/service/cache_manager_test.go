package service_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"fitness-proxy/internal/repository/mocks" // Путь к сгенерированным мокам
	"fitness-proxy/internal/service"
	"fitness-proxy/internal/model"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestCacheService_GetTTLForPath(t *testing.T) {
	type testCase struct {
		name        string
		path        string
		setupMock   func(m *mocks.MockCacheRepository)
		expectedTTL int
		expectErr   bool
	}

	tests := []testCase{
		{
			name: "1. Путь найден в БД — возвращаем сохраненный TTL",
			path: "/api/v1/fitness/exercises",
			setupMock: func(m *mocks.MockCacheRepository) {
				// Ожидаем, что сервис вызовет репозиторий с этим путем
				m.EXPECT().GetTTLForPath(gomock.Any(), "/api/v1/fitness/exercises").Return(60, nil)
			},
			expectedTTL: 60,
			expectErr:   false,
		},
		{
			name: "2. Путь не найден в БД — репозиторий возвращает ошибку",
			path: "/api/v1/unknown",
			setupMock: func(m *mocks.MockCacheRepository) {
				m.EXPECT().GetTTLForPath(gomock.Any(), "/api/v1/unknown").Return(0, errors.New("mongo: no documents in result"))
			},
			expectedTTL: 0,
			expectErr:   true, // Сервис должен пробросить ошибку или вернуть дефолт (смотря как у тебя написано)
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			ctrl := gomock.NewController(t)
			defer ctrl.Finish()
			mockRepo := mocks.NewMockCacheRepository(ctrl)

			tt.setupMock(mockRepo)

			// Создаем сервис кэша и передаем ему наш мок
			cacheService := service.NewCacheManager(5 * time.Minute, mockRepo)

			ttl, err := cacheService.GetTTLForPath(context.Background(), tt.path) 

			if tt.expectErr {
				assert.Error(t, err)
			} else {
				assert.NoError(t, err)
				assert.Equal(t, tt.expectedTTL, ttl)
			}
		})
	}
}

func TestCacheService_DeleteFromRAMByPath(t *testing.T) {
	// 1. Инициализируем менеджер кэша (допустим, он хранит данные в памяти)
    cacheManager := service.NewCacheManager(5 * time.Minute, nil) // Репозиторий не нужен для этого теста

    path := "/api/v1/fitness/exercises"
    dummyResponse := []byte("{\"status\": \"ok\"}")

    // 2. Искусственно кладём данные в кэш приложения
    cacheManager.Set(path, dummyResponse)

    // Проверяем, что они там правда появились
    cachedData, found := cacheManager.Get(path)
    assert.True(t, found)
    assert.Equal(t, dummyResponse, cachedData)

    // 3. Вызываем твой третий способ — удаление из RAM
    cacheManager.DeleteFromRAMByPath(path)

    // 4. Проверяем, что данных больше нет!
    _, foundAfterDelete := cacheManager.Get(path)
    assert.False(t, foundAfterDelete) // Кэш должен быть пуст
}

func TestCacheService_DeleteRuleByID_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	testID := "60d5ec9f1a3b4c001f8e94ba" // Пример валидного ObjectID в виде строки

	// Обучаем мок ожидать вызов удаления по ID и возвращать nil (успех)
	mockRepo.EXPECT().DeleteByID(gomock.Any(), testID).Return(nil)

	cacheService := service.NewCacheManager(5 * time.Minute, mockRepo)
	err := cacheService.DeleteByID(context.Background(), testID)

	assert.NoError(t, err)
}

func TestCacheService_DeleteRuleByID_NotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	testID := "60d5ec9f1a3b4c001f8e94ba"

	// Имитируем ситуацию, когда документ не найден в Mongo
	mockRepo.EXPECT().
		DeleteByID(gomock.Any(), testID).
		Return(errors.New("документ с id 60d5ec9f1a3b4c001f8e94ba не найден"))

	cacheService := service.NewCacheManager(5 * time.Minute, mockRepo)
	err := cacheService.DeleteByID(context.Background(), testID)

	assert.Error(t, err)
	assert.Contains(t, err.Error(), "не найден")
}

func TestCacheService_UpdateTTL(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	id := "60d5ec9f1a3b4c001f8e94ba" // Пример валидного ObjectID в виде строки
	objID, _ := primitive.ObjectIDFromHex(id)
	var newTTL int64 = 120

	// 1. Ожидаем вызов обновления TTL в базе данных
	mockRepo.EXPECT().
		UpdateTTL(gomock.Any(), objID, newTTL).
		Return(nil) // Успешно обновили

	// 2. ДОБАВЛЯЕМ ОЖИДАНИЕ GETSETTINGS (потому что код сервиса его вызывает!)
	// Мы возвращаем пустой слайс или тестовые настройки, чтобы сервис не упал дальше
	mockRepo.EXPECT().
		GetSettings(gomock.Any()).
		Return([]model.CacheSetting{}, nil) 

	cacheService := service.NewCacheManager(5 * time.Minute, mockRepo)
	err := cacheService.UpdateTTL(context.Background(), id, newTTL)

	assert.NoError(t, err)
}