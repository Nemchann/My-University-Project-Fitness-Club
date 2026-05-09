package middleware

import (
	"net"
	"fitness-proxy/internal/service"
	"golang.org/x/time/rate"
	"github.com/gin-gonic/gin"
)

// Глобальные настройки (можно вынести в отдельный config файл)
const (
    DefaultRate = 5.0
    DefaultBurst = 10

    WhiteRate  = 50.0
    WhiteBurst = 100

    GreyRate   = 0.5
    GreyBurst  = 1
)

func RateLimitMiddleware(limiterManager *service.IPRateLimiter, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		ipStr := c.ClientIP()
        ip := net.ParseIP(ipStr)
        
        // Получаем правило для этого IP из нашего Radix Tree
        // Тебе нужно будет немного дописать IsAllowed, чтобы он возвращал само правило (IPRule)
        reason := ipManager.GetRuleInfo(ip) 

        var r float64
        var b int

        switch reason {
        case "blacklisted":
            // Мы уже отсекли их в IPFilter, но на всякий случай
            c.AbortWithStatus(403)
            return
        case "whitelisted":
            r, b = WhiteRate, WhiteBurst
        case "grey":
            r, b = GreyRate, GreyBurst
        default:
            r, b = DefaultRate, DefaultBurst
        }

        limiter := limiterManager.GetLimiter(ipStr, rate.Limit(r), b)

		if !limiter.Allow() {
			c.AbortWithStatusJSON(429, gin.H{"error": "Too many requests. Slow down!"})
			return
		}
		c.Next()
	}
}