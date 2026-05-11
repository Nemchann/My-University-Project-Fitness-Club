package main

import (
	"log"
	//"net/http"
	"net/http/httputil"
	"net/url"
	"github.com/joho/godotenv"
	"github.com/gin-gonic/gin"
	"context"
	"time"

	"fitness-proxy/internal/repository"
	"fitness-proxy/internal/model"
	"fitness-proxy/internal/middleware"
	"fitness-proxy/internal/service"
	"fitness-proxy/internal/controller"

	"os"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

//Запуск: go run cmd/proxy/main.go 


func init() {
    if err := godotenv.Load(); err != nil {
        log.Print("Файл .env не найден")
    }
}

func main() {


	if err := godotenv.Load(); err != nil {
    log.Fatalf("Ошибка загрузки .env: %v", err) // Fatalf остановит программу и скажет почему
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// 1. Подключаемся к Mongo (используя URI из .env)

	uri := os.Getenv("MONGODB_URI")
	if uri == "" {
    	log.Fatal("MONGODB_URI не установлен в .env")
	}
	
    client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
	if err != nil {
    	log.Fatalf("Ошибка подключения к Mongo: %v", err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
    	log.Fatalf("Mongo недоступна: %v", err)
	}

	db := client.Database(os.Getenv("DB_NAME"))
	

	// 2. Инициализируем репозиторий
    logRepo := repository.NewMongoLogRepo(db)

    // 3. Создаем канал для логов
    logChan := make(chan model.AccessLog, 500)

    // 4. Запускаем "слушателя" канала в фоне (п. 3.1.3 - асинхронность) - воркер, который будет сохранять логи в Mongo
    go func() {
        for entry := range logChan {
            // Используем фоновый контекст, чтобы не привязываться к HTTP-запросу
            _ = logRepo.Save(context.Background(), entry)
        }
    }()


    // 5. Передаем канал в Middleware
	r := gin.Default()

	//Сразу доверяем localhost, чтобы не париться с реальными IP при тестах
	r.SetTrustedProxies([]string{"127.0.0.1"})

	r.Use(middleware.AsyncLogger(logChan))

	// 1. Инициализируем репозиторий IP
	ipRepo := repository.NewMongoIPRepo(db)

	// 2. Создаем менеджер (пока пустой)
	ipManager := service.NewIPManager()

	rateLimiter := service.NewIPRateLimiter(1, 2) // 1 запрос в секунду, с "burst" до 2

	cacheManager := service.NewCacheManager(5 * time.Minute) // Кеш на 5 минут

	// 3. Загружаем правила из базы (делаем это ОДИН РАЗ при старте)
	rules, err := ipRepo.GetAll(context.Background())
	if err != nil {
    	log.Fatalf("Не удалось загрузить IP-правила: %v", err)
	}

	// 4. Наполняем менеджер данными (нужно будет добавить метод Import в менеджер)
	for _, rule := range rules {
    	err := ipManager.AddRule(rule.Network, rule.Type)
    	if err != nil {
        	log.Printf("Ошибка при добавлении правила %s: %v", rule.Network, err)
        	continue // Пропускаем битое правило и идем дальше
    	}
	}

	log.Printf("Загружено правил для IP: %d", len(rules))

	r.Use(middleware.IPFilter(ipManager, logChan))

	r.Use(middleware.RateLimitMiddleware(rateLimiter, ipManager))

	r.Use(middleware.CacheMiddleware(cacheManager))


	// Адрес Java-бэкенда
	target := os.Getenv("JAVA_BACKEND_URL")
	remote, err := url.Parse(target)
	if err != nil {
		log.Fatalf("Ошибка конфигурации целевого URL: %v", err)
	}

	// Настраиваем стандартный Reverse Proxy
	proxy := httputil.NewSingleHostReverseProxy(remote)

	// Middleware для логирования (заготовка под Mongo)
	r.Use(func(c *gin.Context) {
		log.Printf("Запрос: %s %s от IP: %s", c.Request.Method, c.Request.URL.Path, c.ClientIP())
		
		// Здесь будет логика фильтрации IP (п. 1.2)
		
		c.Next()
	})


	// Группа для управления прокси
	admin := r.Group("/management")
	{
    	admin.GET("/reload", controller.ReloadRulesHandler(ipRepo, ipManager))
	}

	// Проксируем всё остальное, что НЕ начинается с /management
	r.NoRoute(func(c *gin.Context) {
			c.Request.Host = remote.Host
			proxy.ServeHTTP(c.Writer, c.Request)
	})

	log.Println("Proxy запущен на порту :9000")
	r.Run(":9000") 
}