package controller

import (
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
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

		metrics := gin.H{
			"requests_per_minute": monitor.GetRequestsPerMinute(),
			"average_response_time_ms": monitor.GetAverageResponseTime(),
			//"error_rate_percent": monitor.GetErrorRate(), - Добавить метод позже, когда будет логирование ошибок
			"rps_history": monitor.GetRPSHistory(),
			"traffic_history": monitor.GetTrafficHistory(),
			"latency_ms": monitor.GetLatency(),
		}

		c.JSON(200, metrics)
	}
}