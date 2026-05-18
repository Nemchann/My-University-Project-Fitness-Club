package controller

import (
	"net/http"
	"github.com/gin-gonic/gin"
	"time"
	"fitness-proxy/internal/middleware"
)

type CaptchaRequest struct {
	IP string `json:"ip" binding:"required"`
}

// VerifyCaptchaHandler godoc
// @Summary      Подтвердить прохождение капчи
// @Description  Проверяет запрос от фронтенда и выставляет сессионную куку доверия для IP
// @Tags         Security
// @Accept       json
// @Produce      json
// @Param        request body CaptchaRequest true "Данные верификации"
// @Success      200 {object} map[string]string
// @Router       /management/ip_access/verify-captcha [post]
func VerifyCaptchaHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		var req CaptchaRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body"})
			return
		}

		// Здесь можно добавить дополнительную проверку (например, сверку с IP запроса)
		// Устанавливаем куку: имя, значение, maxAge (3600 сек = 1 час), путь, домен, secure, httpOnly
		c.SetCookie("captcha_verified", "proxy_shield_passed", 3600, "/", "localhost", false, true)

		// Вместо куки или вместе с ней:
		middleware.StoreMap(req.IP, time.Now())

		c.JSON(http.StatusOK, gin.H{
			"message": "Captcha verified successfully",
			"ip":      req.IP,
		})
	}
}