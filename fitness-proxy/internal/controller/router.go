package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"

    "go.mongodb.org/mongo-driver/mongo"
    _ "fitness-proxy/docs"
    swaggerFiles "github.com/swaggo/files"
    ginSwagger "github.com/swaggo/gin-swagger"
)


//Инициализация Swagger: swag init -g cmd/proxy/main.go

// Админка здесь
func SetupRouter(ipManager *service.IPManager, 
    limiterManager *service.IPRateLimiter, 
    cacheManager *service.CacheManager, 
    m *service.Monitor, client *mongo.Client, target string, logsService *service.LogService, r *gin.Engine) *gin.RouterGroup {
    
    // Группа управления - админка
    admin := r.Group("/api/proxy/management")
    {
        admin.GET("/reload", ReloadRulesHandler(ipManager))

        admin.POST("/ip_access/verify-captcha", VerifyCaptchaHandler())

        admin.GET("/stats", GetStatsHandler(limiterManager, cacheManager, ipManager, m))

        admin.GET("/metrics", GetMetricsHandler(m))

        admin.GET("/health", HealthHandler(client, target))

        admin.GET("/logs", LogsHandler(logsService)) 

        admin.GET("/rules", GetAllRulesHandler(ipManager))

        admin.GET("/clients", GetClientsHandler(m))
        
        admin.DELETE("/cache", FlushCacheHandler(cacheManager)) 

        admin.POST("/insert_rule", AddRuleHandler(ipManager))

        admin.GET("/check_ip", CheckIPStatus(ipManager)) 

        admin.GET("/cache_setting/:id", GetSettingByIDHandler(cacheManager)) 

        admin.PUT("/cache_settings/:id", UpdateTTLByIDHandler(cacheManager)) 

        admin.DELETE("/rules/:id", DeleteRuleHandler(ipManager))

        admin.DELETE("/cache_settings/:id", DeleteSettingByIDHandler(cacheManager)) 

        admin.DELETE("/cache_settings/purge", DeleteSettingsByPathHandler(cacheManager)) // Новый метод для удаления настройки кеша
         
        // // Документация будет доступна по адресу http://localhost:9000/swagger/index.html
        r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
    }

    return admin
}