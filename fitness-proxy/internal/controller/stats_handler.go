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

        hits := cacheManager.GetHitRate()
        totalReq := monitor.GetTotalRequests();
        var actualHitRate float64 = 0.0

        if (totalReq > 0){
            actualHitRate = float64(hits) / float64(totalReq)
        }

        stats := gin.H{
            "uptime":   time.Since(startTime).String(),
            "goroutines": runtime.NumGoroutine(),
            "memory_usage_kb": m.Alloc / 1024,
            "total_requests": monitor.GetTotalRequests(),
            "average_response_time_ms": monitor.GetAverageResponseTime(),
            "cache": gin.H{
                "total_keys": cacheManager.GetKeysCount(),
                "hit_rate":  actualHitRate, 
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