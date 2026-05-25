package middleware

import (
	"bytes"
	"fitness-proxy/internal/service"
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type responseBodyWriter struct {
	gin.ResponseWriter
	body *bytes.Buffer
}

var cacheablePaths = []string{
    "/api/fitness-club/users/get_all_clients",
    "/api/fitness-club/schedules/get_schedules_by_week",
	"/api/fitness-club/schedules/get_schedules_by_date",
    "/api/fitness-club/bookings/past",
	"/api/fitness-club/users/get_all_trainers",
	"/api/fitness-club/users/get/",
}

func isCacheable(path string) bool {
    for _, p := range cacheablePaths {
        if strings.HasPrefix(path, p) {
            return true
        }
    }
    return false
}

// Перехватываем метод Write, чтобы сохранить копию данных
func (w responseBodyWriter) Write(b []byte) (int, error) {
	w.body.Write(b)
	return w.ResponseWriter.Write(b)
}

func CacheMiddleware(cache *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Кешируем только GET запросы
		if c.Request.Method != http.MethodGet || !isCacheable(c.Request.URL.Path){
			c.Next()
			return
		}

		// Используем полный URI (путь + параметры) как ключ
		key := c.Request.RequestURI

		// Проверяем кеш
		if data, found := cache.Get(key); found {

			c.Header("Access-Control-Allow-Origin", "http://localhost:5173") // порт твоего фронта
    		c.Header("Access-Control-Allow-Credentials", "true")
			// 1. Обязательно принудительно выставляем заголовки
			c.Header("Content-Type", "application/json; charset=utf-8")
			
			// 2. Явно передаем длину кэшированных байт, чтобы браузер не обрезал поток
			c.Header("Content-Length", fmt.Sprintf("%d", len(data)))

			c.Writer.WriteHeader(http.StatusOK)

			_, err := c.Writer.Write(data)
			if err != nil {
				fmt.Println("Ошибка записи кэшированного ответа фронтенду:", err)
			}
			
			fmt.Println("🚀 [CACHE HIT] Успешно отдано из кэша для фронтенда!")
			cache.IncrementCachedCount()
			c.Abort() // Дальше к Java не идем

			return
		}

		// Если в кеше нет - следим за ответом Java
		w := &responseBodyWriter{body: &bytes.Buffer{}, ResponseWriter: c.Writer}
		c.Writer = w

		c.Next()

		path := c.Request.URL.Path
		ttl := cache.GetTTLForPathRAM(path) // Получаем TTL из нашей мапы

		// После того как Java ответила, сохраняем результат (если статус 200 или 300-400)
		if c.Writer.Status() == http.StatusOK || (c.Writer.Status() >= 300 && c.Writer.Status() < 400) && ttl > 0 {
			cache.Set(key, w.body.Bytes())
			fmt.Println("💾 [CACHE MISS] Данные успешно сохранены в кэш прокси")
		}
	}
}