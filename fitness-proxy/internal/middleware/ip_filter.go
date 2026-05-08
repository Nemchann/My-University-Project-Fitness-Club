package middleware

import (
	"log"
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Добавить метод Clear(), который очищает рейнджеры

func IPFilter(manager *service.IPManager) gin.HandlerFunc {
    return func(c *gin.Context) {
        ip := net.ParseIP(c.ClientIP())
        allowed, reason := manager.IsAllowed(ip)

        if !allowed {
            log.Printf("Доступ запрещен для IP %s: %s", ip, reason)
            c.AbortWithStatusJSON(403, gin.H{"error": "Your IP is blacklisted"})
            return
        }
        
        c.Next()
    }
}