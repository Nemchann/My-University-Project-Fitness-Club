package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Потом вынести в main.go и добавить туда все эндпоинты управления 
func SetupRouter(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager, 
    limiterManager *service.IPRateLimiter, 
    cacheManager *service.CacheManager, r *gin.Engine) *gin.RouterGroup {
    
    // Группа управления
    admin := r.Group("/management")
    {
        admin.GET("/reload", ReloadRulesHandler(ipRepo, ipManager))

        admin.GET("/stats", GetStatsHandler(limiterManager))

        admin.GET("/rules", GetAllRulesHandler(ipRepo))
        
        admin.DELETE("/cache", FlushCacheHandler(cacheManager)) // Новый метод для очистки кеша

        admin.POST("/insert_rule", AddRuleHandler(ipRepo, ipManager))

        admin.DELETE("/delete/:id", DeleteRuleHandler(ipRepo, ipManager))
    }

    return admin
}