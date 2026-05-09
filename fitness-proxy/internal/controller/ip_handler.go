package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

func ReloadRulesHandler(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 1. Снова лезем в базу за свежими правилами
		rules, err := ipRepo.GetAll(c.Request.Context())
		if err != nil {
			c.JSON(500, gin.H{"error": "Failed to fetch rules from DB"})
			return
		}

		// 2. Вызываем метод Reload, который мы написали в IPManager
		if err := ipManager.Reload(rules); err != nil {
			c.JSON(500, gin.H{"error": "Failed to rebuild Radix Tree"})
			return
		}

		c.JSON(200, gin.H{"status": "success", "loaded_rules": len(rules)})
	}
}