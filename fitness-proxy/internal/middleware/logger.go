package middleware

import (
	"time"

	"fitness-proxy/internal/model"

    "github.com/rs/zerolog/log"
    "github.com/rs/zerolog"
	"github.com/gin-gonic/gin"
    "fmt"
)

func AsyncLogger(logChan chan<- model.AccessLog) gin.HandlerFunc {
    return func(c *gin.Context) {
        start := time.Now()
        
        c.Next() // Пропускаем запрос в Java

        reason, _ := c.Get("abort_reason")

        val, exists := c.Get("request_id")
        requestID := "unknown"
        if exists {
            requestID = val.(string)
        }

        // Формируем лог после ответа
        entry := model.AccessLog{
            IP:         c.ClientIP(),
            URL:        c.Request.URL.Path,
            Level:      getLevel(c.Writer.Status()),
            Method:     c.Request.Method,
            RequestID:  requestID,
            StatusCode: c.Writer.Status(),
            Reason:  fmt.Sprintf("%v", reason),
            Latency:    time.Since(start).Milliseconds(),
            Timestamp:  time.Now(),
        }

        // Отправляем в канал асинхронно
        select {
        case logChan <- entry:
        default:
            // Если канал забит, не тормозим прокси, просто пишем в консоль
        }

        duration := time.Since(start)
        status := c.Writer.Status()

        // Определяем уровень лога через Zerolog
        var event *zerolog.Event
        if status >= 500 {
            event = log.Error()
        } else if status >= 400 {
            event = log.Warn()
        } else {
            event = log.Info()
        }

        event.
            Int("status", status).
            Str("method", c.Request.Method).
            Str("path", c.Request.URL.Path).
            Str("ip", c.ClientIP()).
            Dur("latency", duration).
            Msg("processed request")
    }
}

//Додумать, чтобы можно было нормально делать уровни логирования
func getLevel(status int) string {
    if status >= 500 {
        return "ERROR"
    }
    if status >= 400 {
        return "WARN"
    }
    return "INFO"
}