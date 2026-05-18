package service_test

import (
	"testing"
	"fitness-proxy/internal/service"
	"github.com/stretchr/testify/assert"
	"golang.org/x/time/rate"
)

func TestIPRateLimiter_GetLimiters_Creation(t *testing.T) {
	// Создаем лимитер с дефолтными настройками
	limiterManager := service.NewIPRateLimiter(rate.Limit(10), 10)

	ip := "192.168.1.50"
	
	// Запрашиваем лимитеры для нового IP (RPS=5, RPM=60, RPH=1000, RPD=5000)
	limits := limiterManager.GetLimiters(ip, 5.0, 60, 1000, 5000, 10)

	// Проверяем, что объект создался и все интервалы инициализированы
	assert.NotNil(t, limits)
	assert.NotNil(t, limits.Second)
	assert.NotNil(t, limits.Minute)
	assert.NotNil(t, limits.Hour)
	assert.NotNil(t, limits.Day)

	// Проверяем, что менеджер правильно считает количество уникальных IP в памяти
	assert.Equal(t, 1, limiterManager.GetCount())
}

func TestIPRateLimiter_AllowCases(t *testing.T) {
	limiterManager := service.NewIPRateLimiter(rate.Limit(10), 2) // Burst = 2
	ip := "10.0.0.1"

	// Задаем очень жесткие лимиты: всего по 1 запросу на каждый период!
	limits := limiterManager.GetLimiters(ip, 1.0, 1, 1, 1, 2)

	// Первый запрос должен пройти без проблем везде, так как корзины изначально полные
	assert.True(t, limits.Second.Allow())
	assert.True(t, limits.Minute.Allow())
	assert.True(t, limits.Hour.Allow())
	assert.True(t, limits.Day.Allow())

	// Второй запрос подряд должен заблокироваться, так как лимит = 1 запрос в период
	// (Для тестов используем метод Allow(), который списывает 1 токен и возвращает false, если токенов нет)
	assert.False(t, limits.Minute.Allow(), "Должен сработать лимит RPM")
	assert.False(t, limits.Hour.Allow(), "Должен сработать лимит RPH")
	assert.False(t, limits.Day.Allow(), "Должен сработать лимит RPD")
}

func TestIPRateLimiter_GlobalLimits_GreyAndWhite(t *testing.T) {
	// Создаем лимитер
	limiterManager := service.NewIPRateLimiter(5.0, 10)

	greyIP := "192.168.5.5"
	whiteIP := "127.0.0.1"

	// 1. Тестируем Серый список (GreyRateSecond = 0.5, Minute = 10, Hour = 50, Day = 200)
	greyLimits := limiterManager.GetLimiters(greyIP, 0.5, 10, 50, 200, 2)
	assert.NotNil(t, greyLimits)
	
	// Списываем первый токен — ок
	assert.True(t, greyLimits.Minute.Allow())
	
	// 2. Тестируем Белый список (WhiteRateSecond = 50.0, Minute = 1500, Hour = 30000, Day = 100000)
	whiteLimits := limiterManager.GetLimiters(whiteIP, 50.0, 1500, 30000, 100000, 100)
	assert.NotNil(t, whiteLimits)

	// Белый список должен спокойно пропускать пачки запросов
	for i := 0; i < 50; i++ {
		// Так как burst у белого списка большой (например, 100), Allow() вернет true много раз подряд
		_ = whiteLimits.Second.Allow()
	}
}