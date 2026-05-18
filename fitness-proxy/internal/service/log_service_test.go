package service_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"fitness-proxy/internal/model"
	"fitness-proxy/internal/repository/mocks" // Путь к твоим мокам
	"fitness-proxy/internal/service"

	"github.com/stretchr/testify/assert" // Популярная библиотека для лаконичных проверок
	"go.uber.org/mock/gomock"
	"go.mongodb.org/mongo-driver/bson"
)

func TestGetAuditLogs_Success(t *testing.T) {
	// 1. Создаем контроллер для моков
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// 2. Инициализируем мок репозитория
	mockRepo := mocks.NewMockLogRepository(ctrl)

	// 3. Создаем тестовые данные, которые мы "якобы" ждем из базы
	expectedLogs := []model.AccessLog{
		{IP: "127.0.0.1", Level: "INFO", URL: "/api/test", Timestamp: time.Now()},
	}

	// 4. Обучаем наш мок: ожидаем вызов GetLogs с любым контекстом,
	// определенным фильтром и лимитом 100. И говорим вернуть expectedLogs.
	mockRepo.EXPECT().
		GetLogs(gomock.Any(), bson.M{"level": "INFO", "ip": "127.0.0.1"}, int64(100)).
		Return(expectedLogs, nil)

	// 5. Передаем мок в сервис вместо реального репозитория Mongo
	logService := service.NewLogService(mockRepo)

	// 6. Вызываем тестируемый метод
	result, err := logService.GetAuditLogs(context.Background(), "INFO", "127.0.0.1")

	// 7. Проверяем результаты через assert
	assert.NoError(t, err)
	assert.Len(t, result, 1)
	assert.Equal(t, "127.0.0.1", result[0].IP)
}

func TestGetAuditLogs_DatabaseError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mocks.NewMockLogRepository(ctrl)

	// Обучаем мок возвращать ошибку
	mockRepo.EXPECT().
		GetLogs(gomock.Any(), gomock.Any(), int64(100)).
		Return(nil, errors.New("mongo connection timeout"))

	logService := service.NewLogService(mockRepo)

	// Вызываем метод
	result, err := logService.GetAuditLogs(context.Background(), "", "")

	// Проверяем, что сервис правильно пробросил ошибку наверх
	assert.Error(t, err)
	assert.Nil(t, result)
	assert.Equal(t, "mongo connection timeout", err.Error())
}