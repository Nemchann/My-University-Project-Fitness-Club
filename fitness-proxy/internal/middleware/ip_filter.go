package middleware

import (
	"log"
	"net"
    "net/http"
	"fitness-proxy/internal/service"
	"github.com/gin-gonic/gin"
    "fitness-proxy/internal/model"
    "sync"
    "time"
)

var verifiedClients = sync.Map{} // Ключ: string (IP), Значение: time.Time (время прохождения)

func IPFilter(manager *service.IPManager, logChan chan model.AccessLog, m *service.Monitor) gin.HandlerFunc {
    return func(c *gin.Context) {

        if c.Request.Method == "OPTIONS" {
            c.Next()
            return
        }
        // ЕСЛИ ЗАПРОС ИДЕТ НА ПРОХОЖДЕНИЕ КАПЧИ — ПРОПУСКАЕМ БЕЗ ПРОВЕРОК СЛИСКОВ!
        if c.FullPath() == "/api/proxy/management/ip_access/verify-captcha" || c.Request.URL.Path == "/api/proxy/management/ip_access/verify-captcha" {
            c.Next()
            return
        }

        rawIP := c.ClientIP()

        // Если пришел локальный IPv6, принудительно делаем из него IPv4 loopback
        if rawIP == "::1" {
            rawIP = "127.0.0.1"
        }

        ip := net.ParseIP(rawIP)

        allowed, reason := manager.IsAllowed(ip) 

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
        if reason == "grey" {
            checkIP := rawIP
            if checkIP == "::1" || checkIP == "localhost" {
                checkIP = "127.0.0.1"
            }
            
            if !captchaPassed(rawIP) {
                log.Printf("CAPTCHA REQUIRED: IP %s must pass verification", rawIP)
                
                // Возвращаем статус 403 (или специальный 428 Precondition Required), 
                // сообщая фронтенду, что требуется капча
                c.AbortWithStatusJSON(http.StatusPreconditionRequired, gin.H{
                    "error":   "Captcha verification required",
                    "status":  "grey_list",
                    "ip":      rawIP,
                })
                return
            }
            log.Printf("CAPTCHA PASSED PREVIOUSLY: IP %s allowed via cookie", rawIP)
        }
        c.Set("is_blocked", false)
        c.Next()
    }
}

// Простая и надежная проверка прохождения капчи через Cookie
func captchaPassed(rawIP string) bool {
    val, found := verifiedClients.Load(rawIP)
    if !found {
        return false
    }
    
    verifiedTime := val.(time.Time)
    // Проверяем время действия капчи — например, 10 минут
    if time.Since(verifiedTime) > 10*time.Minute {
        verifiedClients.Delete(rawIP) // Время истекло, удаляем
        return false
    }
    
    return true
}

func StoreMap(ip string, time time.Time){
    verifiedClients.Store(ip, time)
}

