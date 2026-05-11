package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

func GetStatsHandler(limiterManager *service.IPRateLimiter) gin.HandlerFunc {
    return func(c *gin.Context) {
        c.JSON(200, gin.H{
            "active_limiters": limiterManager.GetCount(), // нужно будет добавить метод в сервис
            "message": "Proxy is running smoothly",
        })
    }
}