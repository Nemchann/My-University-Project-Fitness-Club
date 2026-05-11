package middleware

import (
	"github.com/gin-gonic/gin"
)

//Используется для того, чтобы браузер не блокировал запросы к нашему API из-за политики CORS. 
// В данном случае мы разрешаем все источники, что подходит для разработки, 
// но в продакшене стоит ограничить список разрешенных доменов.
func CORSMiddleware() gin.HandlerFunc {
    return func(c *gin.Context) {
        c.Writer.Header().Set("Access-Control-Allow-Origin", "*") // Для разработки пойдет
        c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
        c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

        if c.Request.Method == "OPTIONS" {
            c.AbortWithStatus(204)
            return
        }
        c.Next()
    }
}