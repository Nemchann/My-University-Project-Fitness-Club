package middleware

import (
    "github.com/gin-gonic/gin"
    "github.com/google/uuid"
)

func RequestID() gin.HandlerFunc {
    return func(c *gin.Context) {
        // Проверяем, не прислал ли клиент уже свой ID (редко, но бывает)
        reqID := c.GetHeader("X-Request-ID")
        if reqID == "" {
            reqID = uuid.New().String()
        }

        // Устанавливаем ID в контекст Gin, чтобы его могли достать другие хендлеры
        c.Set("RequestID", reqID)

        // Добавляем ID в заголовок ответа, чтобы клиент его видел
        c.Header("X-Request-ID", reqID)

        c.Next()
    }
}