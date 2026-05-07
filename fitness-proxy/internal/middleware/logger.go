package middleware

import (
	"time"

	"fitness-proxy/internal/model"

	"github.com/gin-gonic/gin"
)

func AsyncLogger(logChan chan<- model.AccessLog) gin.HandlerFunc {
    return func(c *gin.Context) {
        start := time.Now()
        
        c.Next() // Пропускаем запрос в Java

        // Формируем лог после ответа
        entry := model.AccessLog{
            IP:         c.ClientIP(),
            URL:        c.Request.URL.Path,
            Method:     c.Request.Method,
            StatusCode: c.Writer.Status(),
            Latency:    time.Since(start).Milliseconds(),
            Timestamp:  time.Now(),
        }

        // Отправляем в канал асинхронно
        select {
        case logChan <- entry:
        default:
            // Если канал забит, не тормозим прокси, просто пишем в консоль
        }
    }
}