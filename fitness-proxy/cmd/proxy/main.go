package main

import (
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"github.com/joho/godotenv"
	"github.com/gin-gonic/gin"
	"context"
	"time"
	"syscall"
	"os/signal"

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


// @title           Swagger Example API
// @version         1.0
// @description     Пример сервера для документации
// @termsOfService  http://swagger.io/terms/

// @contact.name   API Support
// @contact.url    http://www.swagger.io/support
// @contact.email  support@swagger.io

// @license.name  Apache 2.0
// @license.url   http://apache.org

// @host      localhost:9000
// @BasePath  /api/proxy

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
    logRepo := repository.NewMongoLogRepository(db)

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

	r.Use(middleware.RequestID())

	r.Use(middleware.AsyncLogger(logChan))

	// 1. Инициализируем репозиторий IP
	ipRepo := repository.NewMongoIPRepo(db)

	// 2. Создаем менеджер для IP-правил и другие сервисы
	ipManager := service.NewIPManager(ipRepo) //Тут нужно подправить

	rateLimiter := service.NewIPRateLimiter(1, 2) // Нужно будет убрать параметры, они задаются в middleware в зависимости от типа IP (черный, белый, серый)

	logsService := service.NewLogService(logRepo) // Сервис для получения логов, который будет использоваться в контроллере

	cacheRepo := repository.NewMongoCacheRepo(db)

	cacheManager := service.NewCacheManager(5 * time.Minute) // Кеш на 5 минут

	cacheManager.LoadSettings(cacheRepo)

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

	monitor := service.NewMonitor() // Создаем один экземпляр
	go monitor.StartRPSResetter() //Подумать, что можно с этим сделать

	r.Use(monitor.Middleware())

	r.Use(middleware.IPFilter(ipManager, logChan, monitor))

	r.Use(middleware.RateLimitMiddleware(rateLimiter, ipManager))

	r.Use(middleware.CacheMiddleware(cacheManager))

	r.Use(middleware.CORSMiddleware())

	r.Use(middleware.MaxBodySize(2 * 1024 * 1024))


	// Адрес Java-бэкенда
	target := os.Getenv("JAVA_BACKEND_URL")
	remote, err := url.Parse(target)
	if err != nil {
		log.Fatalf("Ошибка конфигурации целевого URL: %v", err)
	}

	r.Use(controller.ProxyHandler(target))
	// Настраиваем стандартный Reverse Proxy
	proxy := httputil.NewSingleHostReverseProxy(remote)

	// Middleware для логирования (заготовка под Mongo)
	r.Use(func(c *gin.Context) {
		log.Printf("Запрос: %s %s от IP: %s", c.Request.Method, c.Request.URL.Path, c.ClientIP())
		
		
		c.Next()
	})

	admin := controller.SetupRouter(ipRepo, ipManager, rateLimiter, 
		cacheManager, cacheRepo, monitor, client, target, logsService, r)

	admin.Handlers.Last() // Нужна для того, чтобы компилятор не ругался, что не использую переменную admin

	// Проксируем всё остальное, что НЕ начинается с /management
	r.NoRoute(func(c *gin.Context) {
			c.Request.Host = remote.Host
			proxy.ServeHTTP(c.Writer, c.Request)
	})

	log.Println("Proxy запущен на порту :9000")


	//Код для graceful shutdown, чтобы не обрывать активные соединения при остановке сервера (например, Ctrl+C)
	srv := &http.Server{
        Addr:    ":9000",
        Handler: r,
    }

    // Запускаем сервер в отдельной горутине, чтобы он не блокировал основной поток
    go func() {
        if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
            log.Fatalf("listen: %s\n", err)
        }
    }()

    // Канал для ожидания сигналов от системы (например, Ctrl+C)
    quit := make(chan os.Signal, 1)
    signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
    <-quit // Блокируемся здесь, пока не придет сигнал
    log.Println("Shutting down proxy server...")

    // Даем серверу 5 секунд на завершение текущих запросов
	//Исправить ошибку компилятора
    shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 5*time.Second)
    defer shutdownCancel()

    if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Fatal("Server forced to shutdown:", err)
	}

    log.Println("Proxy server exiting")
}