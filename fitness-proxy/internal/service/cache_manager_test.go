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

func TestCacheManager_LoadSettings_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	// Имитируем, что в базе лежат 2 настройки кэша
	mockSettings := []model.CacheSetting{
		{Path: "/api/v1/workouts", TTLSeconds: 60},
		{Path: "/api/v1/profile", TTLSeconds: 300},
	}
	mockRepo.EXPECT().GetSettings(gomock.Any()).Return(mockSettings, nil)

	cm := service.NewCacheManager(time.Minute, mockRepo)
	
	// Вызываем загрузку
	cm.LoadSettings()

	// Проверяем, что настройки успешно перетекли в RAM (внутренний pathSettings)
	// Через твой метод GetTTLForPathRAM:
	assert.Equal(t, 60*time.Second, cm.GetTTLForPathRAM("/api/v1/workouts"))
	assert.Equal(t, 300*time.Second, cm.GetTTLForPathRAM("/api/v1/profile"))
	// Проверяем количество ключей
	assert.Equal(t, 2, cm.GetKeysCount())
}

func TestCacheManager_LoadSettings_DatabaseError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	// Имитируем ошибку подключения к MongoDB (например, timeout или connection refused)
	mockRepo.EXPECT().
		GetSettings(gomock.Any()).
		Return(nil, errors.New("mongo: connection topology failed or timeout"))

	// Инициализируем менеджер кэша
	cm := service.NewCacheManager(time.Minute, mockRepo)

	// Вызываем метод. Под капотом он поймает ошибку, выведет её в лог и безопасно выйдет
	cm.LoadSettings()

	// Проверяем, что паники не произошло, а карта настроек осталась пустой
	assert.Equal(t, 0, cm.GetKeysCount())
}

func TestCacheManager_Get_Expired(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	// Создаем менеджер с дефолтным TTL в 1 секунду
	cm := service.NewCacheManager(time.Second, mockRepo)

	key := "/api/v1/exercises"
	data := []byte("cached response")

	// 1. Сохраняем данные
	cm.Set(key, data)

	// 2. Искусственно перематываем время вперед или просто ждем чуть больше 1 секунды
	time.Sleep(1100 * time.Millisecond)

	// 3. Пытаемся получить данные
	cachedData, found := cm.Get(key)

	// 4. Проверяем, что данные признаны протухшими и удалены
	assert.False(t, found)
	assert.Nil(t, cachedData)
}

func TestCacheManager_GetTTLForPathRAM_Default(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	// Задаем дефолтный TTL, например, 45 секунд
	cm := service.NewCacheManager(45*time.Second, mockRepo)

	// Ищем путь, которого точно нет в RAM-карте настроек
	ttl := cm.GetTTLForPathRAM("/api/v1/unknown-path")

	// Должен вернуться дефолтный TTL
	assert.Equal(t, 45*time.Second, ttl)
}

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

func TestCacheManager_UpdateTTL_InvalidID(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl) // Репозиторий вообще не должен вызываться!

	cm := service.NewCacheManager(time.Minute, mockRepo)

	// Передаем заведомо некорректный ID
	err := cm.UpdateTTL(context.Background(), "invalid-id-format", 120)

	// Должна вернуться ошибка валидации гекса, а репозиторий остаться нетронутым
	assert.Error(t, err)
}

func TestCacheManager_Flush(t *testing.T) {
	// 1. Инициализируем менеджер кэша в памяти
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)
	cacheManager := service.NewCacheManager(5 * time.Minute, mockRepo)

	// 2. Кладем несколько разных записей в оперативку
	cacheManager.Set("/api/v1/exercises", []byte("data1"))
	cacheManager.Set("/api/v1/workouts", []byte("data2"))

	// Проверяем, что они записались
	_, found1 := cacheManager.Get("/api/v1/exercises")
	_, found2 := cacheManager.Get("/api/v1/workouts")
	assert.True(t, found1)
	assert.True(t, found2)

	// 3. Вызываем тотальную очистку
	cacheManager.Flush()

	// 4. Проверяем, что кэш абсолютно пуст
	_, found1AfterFlush := cacheManager.Get("/api/v1/exercises")
	_, found2AfterFlush := cacheManager.Get("/api/v1/workouts")
	
	assert.False(t, found1AfterFlush)
	assert.False(t, found2AfterFlush)
}

func TestCacheManager_HitRateCounter(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRepo := mocks.NewMockCacheRepository(ctrl)

	cm := service.NewCacheManager(time.Minute, mockRepo)

	// Изначально счетчик должен быть 0
	assert.Equal(t, 0, cm.GetHitRate())

	// Накручиваем клики по кэшу
	cm.IncrementCachedCount()
	cm.IncrementCachedCount()

	// Проверяем, что стало 2
	assert.Equal(t, 2, cm.GetHitRate())
}