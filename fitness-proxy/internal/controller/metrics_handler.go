package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
	"sync/atomic"
)

// MetricsHandler godoc
// @Summary      Получить динамические метрики
// @Description  Возвращает историю RPS, задержки и трафика за последние 60 секунд для графиков
// @Tags         Monitoring
// @Produce      json
// @Success      200  {object}  model.MetricsResponse
// @Router       /management/metrics [get]
func GetMetricsHandler(monitor *service.Monitor) gin.HandlerFunc {
	return func(c *gin.Context) {

		totalRequests := monitor.GetTotalRequests()
		totalErrors := atomic.LoadInt64(&monitor.TotalErrors)

		metrics := gin.H{
			"requests_per_minute": monitor.GetRequestsPerMinute(),
			"average_response_time_ms": monitor.GetAverageResponseTime(),
			"error_rate_percent": totalErrors,
			"rps_history": monitor.GetRPSHistory(),
			"traffic_history": monitor.GetTrafficHistory(),
			"latency_ms": monitor.GetLatency(),
			"total_traffic_bytes": monitor.GetTotalTrafficBytes(),
			"active_connections": monitor.GetActiveConnections(),
			"total_requests": totalRequests,
			"current_rps": monitor.GetCurrentRPS(),
		}

		c.JSON(200, metrics)
	}
}