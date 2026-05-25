package middleware

import (
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
    "strings"
)

// Глобальные настройки 
const (
    // По умолчанию:
    DefaultRateSecond = 50.0
    DefaultRateMinute = 600.0    
    DefaultRateHour   = 8000.0  
    DefaultRateDay    = 70000.0 
    DefaultBurst = 200

    // Белый список
    WhiteRateSecond  = 1000.0
    WhiteRateMinute = 3000
    WhiteRateHour   = 50000 
    WhiteRateDay    = 150000
    WhiteBurst = 2000

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

        if strings.HasPrefix(c.Request.URL.Path, "/swagger/") {
            c.Next()
            return
        }

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