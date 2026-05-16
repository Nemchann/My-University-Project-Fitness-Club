package controller

import (
	"fitness-proxy/internal/service"
	"fitness-proxy/internal/model"
	"github.com/gin-gonic/gin"
)

// LogsHandler godoc
// @Summary      Получить асинхронные логи аудита
// @Description  Возвращает последние 100 структурированных JSON-логов из MongoDB с возможностью фильтрации по уровню и IP
// @Tags         Monitoring
// @Produce      json
// @Param        level query     string  false  "Фильтр по уровню логов (DEBUG, INFO, WARN, ERROR)"
// @Param        ip    query     string  false  "Фильтр по IP-адресу клиента"
// @Success      200   {object}  map[string][]LogDocument
// @Failure      500   {object}  map[string]string "error: Не удалось прочитать логи из БД"
// @Router       /management/logs [get]
func LogsHandler(logService *service.LogService) gin.HandlerFunc {
	return func(c *gin.Context) {
        levelFilter := c.Query("level")
        ipFilter := c.Query("ip")

        // Вызываем сервис вместо прямого запроса в Mongo
        logs, err := logService.GetAuditLogs(c.Request.Context(), levelFilter, ipFilter)
        if err != nil {
            c.JSON(500, gin.H{"error": "Не удалось получить логи аудита"})
            return
        }

        if logs == nil {
            logs = []model.AccessLog{}
        }

        c.JSON(200, gin.H{"logs": logs})
    }
}