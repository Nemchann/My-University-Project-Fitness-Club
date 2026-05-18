package controller

import (
	"github.com/gin-gonic/gin"
	"time"
	"go.mongodb.org/mongo-driver/mongo"
	"net/http"
)

func HealthHandler(db *mongo.Client, javaURL string) gin.HandlerFunc {
    return func(c *gin.Context) {
        status := "OK"
        details := gin.H{}

        // 1. Проверка MongoDB
        err := db.Ping(c.Request.Context(), nil)
        if err != nil {
            status = "Partially Available"
            details["mongodb"] = "disconnected"
        } else {
            details["mongodb"] = "connected"
        }

        // 2. Проверка Java-бэкенда (Upstream)
        client := http.Client{Timeout: 2 * time.Second}
        resp, err := client.Get(javaURL + "/api/fitness-club/common/health")
        if err != nil || resp.StatusCode != 200 {
            status = "Degraded"
            details["java_backend"] = "unreachable"
        } else {
            details["java_backend"] = "reachable"
        }

        c.JSON(200, gin.H{
            "status":  status,
            "details": details,
            "time":    time.Now().Format(time.RFC3339),
        })
    }
}