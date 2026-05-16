package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
    "time"
    "runtime"

)

func GetStatsHandler(limiterManager *service.IPRateLimiter, cacheManager *service.CacheManager, 
    ipManager *service.IPManager, monitor *service.Monitor) gin.HandlerFunc {
    startTime := time.Now() // Можно вынести в глобальную переменную при старте

    return func(c *gin.Context) {
        var m runtime.MemStats
        runtime.ReadMemStats(&m)

        stats := gin.H{
            "uptime":   time.Since(startTime).String(),
            "goroutines": runtime.NumGoroutine(),
            "memory_usage_kb": m.Alloc / 1024,
            "total_requests": monitor.GetTotalRequests(),
            "average_response_time_ms": monitor.GetAverageResponseTime(),
            "cache": gin.H{
                "total_keys": cacheManager.GetKeysCount(),
                "hit_rate":   cacheManager.GetHitRate(), 
            },
            "security": gin.H{
                "active_rules": ipManager.GetRulesCount(),
                "blocked_requests": ipManager.GetBlockedCount(),
            },
            "system_status": gin.H{
                "upstream_reachable": true, // Тут можно добавить проверку Java-бэкенда 
            },
        }

        c.JSON(200, stats)
    }
}