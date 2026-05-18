package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
	"fmt"
	"net/http"
	"net"
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
func AddRuleHandler(ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req AddRuleRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(400, gin.H{"error": "Неверный формат данных"})
			return
		}

		// 1. Сохраняем в MongoDB
		err := ipManager.AddRule(req.IP, req.Type)
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
func ReloadRulesHandler(ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Просто дергаем метод менеджера. Менеджер сам знает, как сходить в Mongo.
    	err := ipManager.ReloadFromDB(c.Request.Context())
    	if err != nil {
        	// Если что-то пошло не так на стороне БД или парсинга
        	c.JSON(http.StatusInternalServerError, gin.H{
           		"error": fmt.Sprintf("Failed to rebuild Radix Tree: %v", err),
        	})
        	return
    	}

    	c.JSON(http.StatusOK, gin.H{
        	"status": "success",
        	"message": "Radix Tree successfully rebuilt from database data",
    	})
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
func GetAllRulesHandler(ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		rules, err := ipManager.GetAll(c.Request.Context())
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
func DeleteRuleHandler(ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		id := c.Param("id")
		err := ipManager.RemoveRule(c.Request.Context(), id)
		if err != nil {
			c.JSON(500, gin.H{"error": "Не удалось удалить"})
			return
		}

		rules, errAll := ipManager.GetAll(c.Request.Context())


		if errAll != nil {
			c.JSON(500, gin.H{"error": "Не найти"})
			return
		}
		
		// После удаления из БД лучше обновить Radix Tree (вызвать Reload)
		ipManager.Reload(rules) 
		c.JSON(200, gin.H{"message": "Правило удалено"})
	}
}

func CheckIPStatus (ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Получаем IP из query-параметра, например: /check_ip?ip=192.168.1.1
		ipStr := c.Query("ip")
		if ipStr == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Parameter 'ip' is required"})
			c.Abort()
			return
		}

		ip := net.ParseIP(ipStr)
		if ip == nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid IP address format"})
			c.Abort()
			return
		}

		// Вызываем твой IsAllowed из IPManager
		_, listName := ipManager.IsAllowed(ip.To4())

		// Если IP не найден ни в одном рейнджере, IsAllowed вернет пустую строку или "default"
		if listName == "" {
			listName = "default"
		}

		c.JSON(http.StatusOK, gin.H{
			"ip":     ipStr,
			"status": listName, // вернет "blacklisted", "whitelisted", "grey" или "default"
		})
	}
}