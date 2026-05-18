package middleware

import (
	"net/http"
	"github.com/gin-gonic/gin"
)

func MaxBodySize(limit int64) gin.HandlerFunc {
    return func(c *gin.Context) {
        // Ограничиваем размер считываемого тела
        c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, limit)
        
        // Пытаемся прочитать один байт, чтобы проверить лимит
        if c.Request.ContentLength > limit {
            c.JSON(http.StatusRequestEntityTooLarge, gin.H{"error": "Payload too large"})
            c.Abort()
            return
        }
        c.Next()
    }
}