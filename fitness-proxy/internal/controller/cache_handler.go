package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

func FlushCacheHandler(cache *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		cache.Flush()
		c.JSON(200, gin.H{"status": "success", "message": "Cache cleared successfully"})
	}
}