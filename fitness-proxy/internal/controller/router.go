package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Потом вынести в main.go и добавить туда все эндпоинты управления 
func SetupRouter(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager, 
    limiterManager *service.IPRateLimiter, 
    cacheManager *service.CacheManager, cacheRepo *repository.MongoCacheRepo, r *gin.Engine) *gin.RouterGroup {
    
    // Группа управления - админка
    admin := r.Group("/management")
    {
        admin.GET("/reload", ReloadRulesHandler(ipRepo, ipManager))

        admin.GET("/stats", GetStatsHandler(limiterManager))

        admin.GET("/rules", GetAllRulesHandler(ipRepo))
        
        admin.DELETE("/cache", FlushCacheHandler(cacheManager)) // Новый метод для очистки кеша

        admin.POST("/insert_rule", AddRuleHandler(ipRepo, ipManager))

        admin.GET("/cache_setting/:id", GetSettingByIDHandler(cacheRepo, cacheManager)) // Новый метод для получения TTL по ID

        admin.DELETE("/delete/:id", DeleteRuleHandler(ipRepo, ipManager))

        admin.DELETE("/cache_settings/:id", DeleteSettingByIDHandler(cacheRepo)) // Новый метод для удаления настройки кеша по ID

        admin.DELETE("/cache_settings/purge", DeleteSettingsByPathHandler(cacheManager)) // Новый метод для удаления настройки кеша по пути
    }

    return admin
}