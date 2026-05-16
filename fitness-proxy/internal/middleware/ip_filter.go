package middleware

import (
	"log"
	"net"
    //"net/http"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
    "fitness-proxy/internal/model"
)

// Добавить метод Clear(), который очищает рейнджеры

func IPFilter(manager *service.IPManager, logChan chan model.AccessLog, m *service.Monitor) gin.HandlerFunc {
    return func(c *gin.Context) {
        ip := net.ParseIP(c.ClientIP())
        rawIP := c.ClientIP() // На случай, если IP не парсится
        if ip != nil && ip.To4() != nil {
            rawIP = ip.To4().String() // Всегда будет 127.0.0.1
        }

        allowed, reason := manager.IsAllowed(ip.To4()) //Это нужно для того, если пришел адрес ::1

        if !allowed || reason == "blacklisted" {
            log.Printf("BLOCK: IP %s rejected. Reason: %s", ip, reason)
            c.Set("block_reason", "blacklist")
            c.Set("abort_reason", "IP in Blacklist")
            // ТЗ 1.2.1: прерываем запрос с ошибкой 403
            c.AbortWithStatusJSON(403, gin.H{
                "error": "Access denied",
                "ip":    rawIP,
            })
            return
        }
        // if reason == "grey"{
        //     if !captchaPassed(c) {          // ← твоя проверка CAPTCHA
        //         c.AbortWithStatus(http.StatusForbidden) // или редирект на страницу с CAPTCHA
        //         return
        //     }
        // }
        c.Set("is_blocked", false)
        c.Next()
    }
}

//Функция для капчи, которую стоит доработать
// func captchaPassed(c *gin.Context) bool {
//     token, err := c.Cookie("captcha_token")
//     if err != nil {
//         return false
//     }
//     return validateToken(token) // проверка подписи или обращение к Redis
// }

