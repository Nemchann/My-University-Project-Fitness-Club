package middleware

import (
	"log"
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
    "fitness-proxy/internal/model"
)

// Добавить метод Clear(), который очищает рейнджеры

func IPFilter(manager *service.IPManager, logChan chan model.AccessLog) gin.HandlerFunc {
    return func(c *gin.Context) {
        ip := net.ParseIP(c.ClientIP())
        allowed, reason := manager.IsAllowed(ip)

        if !allowed || reason == "blacklisted" {
            log.Printf("BLOCK: IP %s rejected. Reason: %s", ip, reason)

            c.Set("abort_reason", "IP in Blacklist")
            // ТЗ 1.2.1: прерываем запрос с ошибкой 403
            c.AbortWithStatusJSON(403, gin.H{
                "error": "Access denied",
                "ip":    ip.String(),
            })
            return
        }
        
        c.Next()
    }
}

