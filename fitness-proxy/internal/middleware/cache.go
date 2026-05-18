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
		// 1. Кешируем только GET запросы
		if c.Request.Method != http.MethodGet || !isCacheable(c.Request.URL.Path){
			c.Next()
			return
		}

		// Используем полный URI (путь + параметры) как ключ
		key := c.Request.RequestURI

		// 2. Проверяем кеш
		if data, found := cache.Get(key); found {
			c.Data(http.StatusOK, "application/json", data)
			fmt.Println("Из кеша")
			cache.IncrementCachedCount()
			c.Abort() // Дальше к Java не идем!
			return
		}

		// 3. Если в кеше нет — "шпионим" за ответом Java
		w := &responseBodyWriter{body: &bytes.Buffer{}, ResponseWriter: c.Writer}
		c.Writer = w

		c.Next()


		path := c.Request.URL.Path
		ttl := cache.GetTTLForPathRAM(path) // Получаем TTL из нашей карты

		// 4. После того как Java ответила, сохраняем результат (если статус 200 или 300-400)
		if c.Writer.Status() == http.StatusOK || (c.Writer.Status() >= 300 && c.Writer.Status() < 400) && ttl > 0 {
			cache.Set(key, w.body.Bytes())
		}
	}
}