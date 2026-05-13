package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
	"fitness-proxy/internal/repository"
)

func FlushCacheHandler(cache *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		cache.Flush()
		c.JSON(200, gin.H{"status": "success", "message": "Cache cleared successfully"})
	}
}

func GetSettingByIDHandler(cacheRepo *repository.MongoCacheRepo, cacheManager *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		id := c.Param("id")
		result, err := cacheManager.GetPathSettingsByID(id, cacheRepo)
		if err != nil {
			c.JSON(500, gin.H{"error": "Failed to get cache setting"})
			return
		}
		c.JSON(200, gin.H{"status": "success", "data": result})
	}
}

func DeleteSettingByIDHandler(cacheRepo *repository.MongoCacheRepo) gin.HandlerFunc{
	return func(c *gin.Context) {
		id := c.Param("id")

		err := cacheRepo.DeleteById(c.Request.Context(), id)

		if err != nil{
			c.JSON(500, gin.H{"error": "Failed to get cache setting"})
			return
		}

		c.JSON(200, gin.H{"id": id, "message": "Настройка TTL для данного пути удалена"})
	}
}


func DeleteSettingsByPathHandler(cacheManager *service.CacheManager) gin.HandlerFunc{
	return func(c *gin.Context) {
		path := c.Query("path") // Извлекаем ?path=/api/trainers
		if path == "" {
			c.JSON(400, gin.H{"error": "Необходимо указать path"})
			return
		}

		// Вызываем метод очистки в менеджере
		count := cacheManager.DeleteByPath(path)
		
		c.JSON(200, gin.H{
			"message": "Кеш очищен",
			"path":    path,
			"deleted": count,
		})
	}
}