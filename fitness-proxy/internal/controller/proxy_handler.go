package controller

import (
	"github.com/gin-gonic/gin"
	"net/http"
	//"time"
)

//Подумать, что делать с этим
func ProxyHandler(javaBackendURL string) gin.HandlerFunc {
    return func(c *gin.Context) {
        // 1. Достаем RequestID, который создал наш Middleware
        reqID, exists := c.Get("RequestID")
        if !exists {
            reqID = "unknown"
        }

        // 2. Создаем запрос к бэкенду
        targetURL := javaBackendURL + c.Param("proxyPath")
        proxyReq, _ := http.NewRequest(c.Request.Method, targetURL, c.Request.Body)

        // 3. Копируем заголовки и ДОБАВЛЯЕМ наш ID
        for header, values := range c.Request.Header {
            for _, value := range values {
                proxyReq.Header.Add(header, value)
            }
        }
        proxyReq.Header.Set("X-Request-ID", reqID.(string)) 
        proxyReq.Header.Set("X-Forwarded-For", c.ClientIP())

        // 4. Отправляем запрос (с твоей логикой Retry)
        // client := &http.Client{Timeout: 10 * time.Second}
        // resp, err := client.Do(proxyReq)

		
        // ... дальше обработка ответа
    }
}