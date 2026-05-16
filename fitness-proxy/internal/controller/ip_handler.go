package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

type AddRuleRequest struct {
	IP   string `json:"network" binding:"required"`
	Type string `json:"type" binding:"required"` // "black", "white", "grey"
}


// @Summary Добавить правило IP
// @Description Добавляет новый IP в черный, белый или серый список
// @Tags IP-Management
// @Accept  json
// @Produce  json
// @Param   request body model.IPRule true "Данные правила"
// @Success 200 {object} map[string]string
// @Router /management/rules [post]
func AddRuleHandler(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req AddRuleRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(400, gin.H{"error": "Неверный формат данных"})
			return
		}

		// 1. Сохраняем в MongoDB
		err := ipRepo.InsertRule(c.Request.Context(), req.IP, req.Type)
		if err != nil {
			c.JSON(500, gin.H{"error": "Ошибка сохранения в БД"})
			return
		}

		// 2. Обновляем Radix Tree в памяти, чтобы изменения вступили в силу сразу
		ipManager.UpdateRule(req.IP, req.Type)

		c.JSON(200, gin.H{"status": "success", "message": "Правило добавлено"})
	}
}

// @Summary Подгрузить правила IP
// @Description Обновляет актуальные правила IP
// @Tags IP-Management
// @Produce  json
// @Success 200 {object} map[string]string
// @Router /management/reload [get]
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

// Получение всех правил


// @Summary Получить все правила IP
// @Description Возвращает список всех правил IP
// @Tags IP-Management
// @Produce  json
// @Success 200 {object} []model.IPRule
// @Failure 500 {object} map[string]string
// @Router /management/rules [get]
func GetAllRulesHandler(ipRepo *repository.MongoIPRepo) gin.HandlerFunc {
	return func(c *gin.Context) {
		rules, err := ipRepo.GetAll(c.Request.Context())
		if err != nil {
			c.JSON(500, gin.H{"error": "Ошибка БД"})
			return
		}
		c.JSON(200, rules)
	}
}

// Удаление правила по ID

// @Summary Удалить правило IP
// @Description Удаляет запись из MongoDB по её ObjectID
// @Tags IP-Management
// @Param id path string true "ObjectID правила"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Router /management/rules/{id} [delete]
func DeleteRuleHandler(ipRepo *repository.MongoIPRepo, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		id := c.Param("id")
		err := ipRepo.DeleteByID(c.Request.Context(), id)
		if err != nil {
			c.JSON(500, gin.H{"error": "Не удалось удалить"})
			return
		}

		rules, errAll := ipRepo.GetAll(c.Request.Context())


		if errAll != nil {
			c.JSON(500, gin.H{"error": "Не найти"})
			return
		}
		
		// После удаления из БД лучше обновить Radix Tree (вызвать Reload)
		ipManager.Reload(rules) 
		c.JSON(200, gin.H{"message": "Правило удалено"})
	}
}