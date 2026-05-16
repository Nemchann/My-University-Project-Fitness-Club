package controller

import (
	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/service"
	"fmt"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

// @Summary Очистить кеш
// @Description Удаляет все записи из кеша в памяти
// @Tags Cache-Management
// @Produce  json
// @Success 200 {object} map[string]string
// @Router /management/cache [delete]
func FlushCacheHandler(cache *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		cache.Flush()
		c.JSON(200, gin.H{"status": "success", "message": "Cache cleared successfully"})
	}
}

// @Summary Получить настройки кеша по ID
// @Description Возвращает настройки кеша для указанного ObjectID
// @Tags Cache-Management
// @Produce  json
// @Param id path string true "ObjectID настройки"
// @Success 200 {object} map[string]interface{}
// @Failure 500 {object} map[string]string
// @Router /management/cache_setting/{id} [get]
func GetSettingByIDHandler(cacheRepo *repository.MongoCacheRepo, cacheManager *service.CacheManager) gin.HandlerFunc {
	return func(c *gin.Context) {
		id := c.Param("id")
		result, err := cacheManager.GetPathSettingsByID(id, cacheRepo)
		if err != nil {
			c.JSON(500, gin.H{"error": "Failed to get cache setting"})
			return
		}
		c.JSON(200, gin.H{"status": "success", "data": result})
	}
}

// @Summary Удалить настройку кеша по ID
// @Description Удаляет настройку кеша для указанного ObjectID
// @Tags Cache-Management
// @Produce  json
// @Param id path string true "ObjectID настройки"
// @Success 200 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /management/cache_settings/{id} [delete]
func DeleteSettingByIDHandler(cacheRepo *repository.MongoCacheRepo) gin.HandlerFunc{
	return func(c *gin.Context) {
		id := c.Param("id")

		err := cacheRepo.DeleteById(c.Request.Context(), id)

		if err != nil{
			c.JSON(500, gin.H{"error": "Failed to get cache setting"})
			return
		}

		c.JSON(200, gin.H{"id": id, "message": "Настройка TTL для данного пути удалена"})
	}
}

// @Summary Удалить настройки кеша по началу пути
// @Description Удаляет все настройки кеша для совпадений по началу пути (например, /api/users. Удалит в том числе /api/users/uuid)
// @Tags Cache-Management
// @Produce  json
// @Param path query string true "Путь для удаления настроек"
// @Success 200 {object} map[string]interface{}
// @Failure 400 {object} map[string]string
// @Router /management/cache_settings/purge [delete]
func DeleteSettingsByPathHandler(cacheManager *service.CacheManager) gin.HandlerFunc{
	return func(c *gin.Context) {
		path := c.Query("path") // Извлекаем ?path=/api/trainers
		if path == "" {
			c.JSON(400, gin.H{"error": "Необходимо указать path"})
			return
		}

		// Вызываем метод очистки в менеджере
		count := cacheManager.DeleteByPath(path)
		
		c.JSON(200, gin.H{
			"message": "Кеш очищен",
			"path":    path,
			"deleted": count,
		})
	}
}

// @Summary Изменить TTL настройки кеша по id
// @Description Изменяет ttl у опредленной настройки кеша по id
// @Tags Cache-Management
// @Accept json
// @Produce  json
// @Param        id          path      string  true  "ID настройки кеша (Hex)"
// @Param        ttl_request body      string  true  "Новое значение TTL в JSON" schema{type=object,properties={ttl_seconds=integer}}
// @Success 200 {object} map[string]interface{}
// @Failure 400 {object} map[string]string
// @Router /management/cache_settings/{id} [put]
func UpdateTTLByIDHandler(cacheManager *service.CacheManager, cacheRepo *repository.MongoCacheRepo) gin.HandlerFunc {
	return func(c *gin.Context){
		id := c.Param("id")
		var input struct {
            TTLSeconds int64 `json:"ttl_seconds" binding:"required,min=1"`
        }

		objID, err := primitive.ObjectIDFromHex(id)
		if err != nil {
    		c.JSON(400, gin.H{"error": "Неверный формат ID"})
    		return
		}

        if err := c.ShouldBindJSON(&input); err != nil {
            c.JSON(400, gin.H{"error": "Неверный формат TTL"})
            return
        }

        // 1. Обновляем в MongoDB
        errDB := cacheRepo.UpdateTTL(c.Request.Context(), objID, input.TTLSeconds)
        if errDB != nil {
			fmt.Println(errDB.Error())
            c.JSON(500, gin.H{"error": "Ошибка БД"})
            return
        }

        // 2. Сразу перегружаем настройки в память прокси
        cacheManager.LoadSettings(cacheRepo)

        c.JSON(200, gin.H{"message": "TTL обновлен и применен"})
    }

}	