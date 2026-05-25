package service_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"fitness-proxy/internal/model"
	"fitness-proxy/internal/repository/mocks" 
	"fitness-proxy/internal/service"

	"github.com/stretchr/testify/assert" 
	"go.uber.org/mock/gomock"
	"go.mongodb.org/mongo-driver/bson"
)

func TestGetAuditLogs_Success(t *testing.T) {
	// Создаем контроллер для моков
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// Инициализируем мок репозитория
	mockRepo := mocks.NewMockLogRepository(ctrl)

	// Создаем тестовые данные, которые мы ждем из базы
	expectedLogs := []model.AccessLog{
		{IP: "127.0.0.1", Level: "INFO", URL: "/api/test", Timestamp: time.Now()},
	}

	// Ожидаем вызов GetLogs с любым контекстом,
	// определенным фильтром и лимитом 100. И говорим вернуть expectedLogs.
	mockRepo.EXPECT().
		GetLogs(gomock.Any(), bson.M{"level": "INFO", "ip": "127.0.0.1"}, int64(100)).
		Return(expectedLogs, nil)

	// Передаем мок в сервис вместо реального репозитория Mongo
	logService := service.NewLogService(mockRepo)

	// Вызываем тестируемый метод
	result, err := logService.GetAuditLogs(context.Background(), "INFO", "127.0.0.1")

	// Проверяем результаты через assert
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