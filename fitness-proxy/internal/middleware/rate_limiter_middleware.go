package middleware

import (
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Глобальные настройки 
const (
    // По умолчанию:
    DefaultRateSecond = 10.0
    DefaultRateMinute = 120    
    DefaultRateHour   = 2000  
    DefaultRateDay    = 20000 
    DefaultBurst = 20

    // Белый список
    WhiteRateSecond  = 50.0
    WhiteRateMinute = 1500
    WhiteRateHour   = 30000 
    WhiteRateDay    = 100000
    WhiteBurst = 100

    // Серый список
    GreyRateSecond   = 4
    GreyRateMinute  = 80   
    GreyRateHour    = 400   
    GreyRateDay     = 1600  
    GreyBurst  = 8
)

func RateLimitMiddleware(limiterManager *service.IPRateLimiter, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		ipStr := c.ClientIP()
        ip := net.ParseIP(ipStr)
        
        // Получаем правило для этого IP из Radix Tree
        reason := ipManager.GetRuleInfo(ip) 

        var rs float64
        var rm int
        var rh int
        var rd int
        var b int

        switch reason {
        case "blacklisted":
            c.AbortWithStatus(403)
            return
        case "whitelisted":
            rs, rm, rh, rd, b = WhiteRateSecond, WhiteRateMinute, WhiteRateHour, WhiteRateDay, WhiteBurst
        case "grey":
            rs, rm, rh, rd, b = GreyRateSecond, GreyRateMinute, GreyRateHour, GreyRateDay, GreyBurst
        default:
            rs, rm, rh, rd, b = DefaultRateSecond, DefaultRateMinute, DefaultRateHour, DefaultRateDay, DefaultBurst
        }

        limiters := limiterManager.GetLimiters(ipStr, rs, rm, rh, rd, b) 

        // Запрос проходит, только если все лимитеры дали добро
        if !limiters.Second.Allow() || !limiters.Minute.Allow() || !limiters.Hour.Allow() || !limiters.Day.Allow() {
            c.Header("Retry-After", "2")
            c.Set("abort_reason", "Rate limit exceeded") // Чтобы логгер записал причину
            c.Set("block_reason", "rate_limit")
            c.AbortWithStatusJSON(429, gin.H{"error": "Too many requests. Slow down!"})
            return
        }
		c.Next()
	}
}