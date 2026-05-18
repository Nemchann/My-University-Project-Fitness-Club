package controller

import (
	"fitness-proxy/internal/service"
	"sync/atomic"
	"sort"
	"github.com/gin-gonic/gin"
)

// ClientsHandler godoc
// @Summary      Получить топ активных клиентов и нарушителей
// @Description  Возвращает список самых активных IP-адресов с сортировкой по количеству запросов и логами блокировок
// @Tags         Monitoring
// @Produce      json
// @Success      200      {object}  map[string][]service.ClientStats
// @Router       /management/clients [get]
func GetClientsHandler(monitor *service.Monitor) gin.HandlerFunc {
	return func(c *gin.Context) {
		var allClients []service.ClientStats

		// 1. Обходим sync.Map и копируем данные в слайс
		monitor.GetClientsMap().Range(func(key, value interface{}) bool {
			stats := value.(*service.ClientStats)
			// Считываем атомарные значения, чтобы данные были консистентны
			allClients = append(allClients, service.ClientStats{
				IP:               stats.IP,
				TotalRequests:    atomic.LoadInt64(&stats.TotalRequests),
				BlockedRequests:  atomic.LoadInt64(&stats.BlockedRequests),
				BytesTransferred: atomic.LoadInt64(&stats.BytesTransferred),
			})
			return true // продолжаем обход
		})

		// 2. Сортируем слайс по убыванию total_requests (сначала самые активные)
		sort.Slice(allClients, func(i, j int) bool {
			return allClients[i].TotalRequests > allClients[j].TotalRequests
		})

		// 3. Обрезаем до Топ-10, если клиентов больше
		limit := 10
		if len(allClients) < limit {
			limit = len(allClients)
		}
		topClients := allClients[:limit]

		// 4. Отдаем результат фронтенду
		c.JSON(200, gin.H{
			"top_clients": topClients,
		})
	}
}