package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"

    _ "fitness-proxy/docs"
    swaggerFiles "github.com/swaggo/files"
    ginSwagger "github.com/swaggo/gin-swagger"
)

// Потом вынести в main.go и добавить туда все эндпоинты управления 
func SetupRouter(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager, 
    limiterManager *service.IPRateLimiter, 
    cacheManager *service.CacheManager, cacheRepo *repository.MongoCacheRepo, 
    m *service.Monitor, r *gin.Engine) *gin.RouterGroup {
    
    // Группа управления - админка
    admin := r.Group("/api/proxy/management")
    {
        admin.GET("/reload", ReloadRulesHandler(ipRepo, ipManager))

        admin.GET("/stats", GetStatsHandler(limiterManager, cacheManager, ipManager))

        admin.GET("/rules", GetAllRulesHandler(ipRepo))
        
        admin.DELETE("/cache", FlushCacheHandler(cacheManager)) // Новый метод для очистки кеша

        admin.POST("/insert_rule", AddRuleHandler(ipRepo, ipManager))

        admin.GET("/cache_setting/:id", GetSettingByIDHandler(cacheRepo, cacheManager)) // Новый метод для получения TTL по ID

        admin.DELETE("/rules/:id", DeleteRuleHandler(ipRepo, ipManager))

        admin.DELETE("/cache_settings/:id", DeleteSettingByIDHandler(cacheRepo)) // Новый метод для удаления настройки кеша по ID

        admin.DELETE("/cache_settings/purge", DeleteSettingsByPathHandler(cacheManager)) // Новый метод для удаления настройки кеша
        // 
        // // Документация будет доступна по адресу http://localhost:9000/swagger/index.html
        r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
    }

    return admin
}