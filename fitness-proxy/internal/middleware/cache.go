package middleware

import (
	"bytes"
	"net/http"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

type responseBodyWriter struct {
	gin.ResponseWriter
	body *bytes.Buffer
}

// Перехватываем метод Write, чтобы сохранить копию данных
func (w responseBodyWriter) Write(b []byte) (int, error) {
	w.body.Write(b)
	return w.ResponseWriter.Write(b)
}

func CacheMiddleware(cache *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 1. Кешируем только GET запросы
		if c.Request.Method != http.MethodGet {
			c.Next()
			return
		}

		// Используем полный URI (путь + параметры) как ключ
		key := c.Request.RequestURI

		// 2. Проверяем кеш
		if data, found := cache.Get(key); found {
			c.Data(http.StatusOK, "application/json", data)
			c.Abort() // Дальше к Java не идем!
			return
		}

		// 3. Если в кеше нет — "шпионим" за ответом Java
		w := &responseBodyWriter{body: &bytes.Buffer{}, ResponseWriter: c.Writer}
		c.Writer = w

		c.Next()

		// 4. После того как Java ответила, сохраняем результат (если статус 200)
		if c.Writer.Status() == http.StatusOK {
			cache.Set(key, w.body.Bytes())
		}
	}
}