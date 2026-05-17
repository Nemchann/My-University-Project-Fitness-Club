package middleware

import (
	"net"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
)

// Глобальные настройки (можно вынести в отдельный config файл)
const (
    // ПО УМОЛЧАНИЮ (Обычные пользователи):
    // Разрешаем короткие всплески, но ограничиваем общее дневное и часовое потребление,
    // чтобы один клиент не исчерпал лимиты стороннего фитнес-API.
    DefaultRateSecond = 5.0
    DefaultRateMinute = 60    // В среднем 1 запрос в секунду, если размазать на минуту
    DefaultRateHour   = 1000  // Защита от зациклившихся скриптов на стороне клиента
    DefaultRateDay    = 10000 // Лимит на сутки для одного стандартного пользователя
    DefaultBurst = 10

    // БЕЛЫЙ СПИСОК (Доверенные сервисы / Партнеры / Фронтенд-приложения):
    // Здесь лимиты огромные, так как мы доверяем этим источникам.
    WhiteRateSecond  = 50.0
    WhiteRateMinute = 1500  // Высокая пропускная способность для аналитики или синхронизации
    WhiteRateHour   = 30000 
    WhiteRateDay    = 100000
    WhiteBurst = 100

    // СЕРЫЙ СПИСОК (Подозрительные IP / Потенциальные спамеры):
    // Очень жесткие ограничения. Мы даем им совершать запросы, но заставляем их "страдать" от медлительности.
    GreyRateSecond   = 0.5
    GreyRateMinute  = 10   // Максимум 10 запросов в минуту
    GreyRateHour    = 50   // Быстро упрутся в потолок, если это бот
    GreyRateDay     = 200  // Суточный лимит, блокирующий парсинг данных
    GreyBurst  = 1
)

func RateLimitMiddleware(limiterManager *service.IPRateLimiter, ipManager *service.IPManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		ipStr := c.ClientIP()
        ip := net.ParseIP(ipStr)
        
        // Получаем правило для этого IP из нашего Radix Tree
        // Тебе нужно будет немного дописать IsAllowed, чтобы он возвращал само правило (IPRule)
        reason := ipManager.GetRuleInfo(ip) 

        var rs float64
        var rm int
        var rh int
        var rd int
        var b int

        switch reason {
        case "blacklisted":
            // Мы уже отсекли их в IPFilter, но на всякий случай
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

        // Запрос проходит, только если ОБА лимитера дали добро
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