package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Потом вынести в main.go и добавить туда все эндпоинты управления 
func SetupRouter(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager, 
    limiterManager *service.IPRateLimiter, proxyHandler gin.HandlerFunc, cacheManager *service.CacheManager) *gin.Engine {
    r := gin.Default()

    // Группа управления
    admin := r.Group("/management")
    {
        admin.GET("/reload", ReloadRulesHandler(ipRepo, ipManager))

        admin.GET("/stats", GetStatsHandler(limiterManager))
        
        admin.DELETE("/cache", FlushCacheHandler(cacheManager)) // Новый метод для очистки кеша
    }

    // Все остальное — в прокси
    r.NoRoute(proxyHandler)

    return r
}