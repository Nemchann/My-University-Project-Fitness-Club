package service

import (
	"time"
	"github.com/gin-gonic/gin"
	"sync/atomic"
	"sync"
    "net/http"
)

type Monitor struct {
    CurrentRPS    int64
    TotalRequests int64
	AverageLatency int64
	ActiveConnections int64
	CurrentTraffic int64
    TotalTrafficBytes int64
	RPSHistory     []int64
    TrafficHistory []int64 // байт в секунду
    bufferIndex    int
    mu             sync.Mutex
    ClientsMap sync.Map // Ключ: string (IP), Значение: *ClientStats
}

func NewMonitor () *Monitor{
	return &Monitor{
		CurrentRPS: 0,
		TotalRequests: 0,
		AverageLatency: 0,
		ActiveConnections: 0,
		RPSHistory:     make([]int64, 60), // храним за 60 секунд
        TrafficHistory: make([]int64, 60),
	}
}

type ClientStats struct {
	IP             string `json:"ip"`
	TotalRequests  int64  `json:"total_requests"`
	BlockedRequests int64  `json:"blocked_requests"`
	BytesTransferred int64 `json:"bytes_transferred"`
    BlockedRateLimit int64 `json:"blocked_rate_limit"` // Превысил лимит RPS/RPM
    BlockedBlacklist int64 `json:"blocked_blacklist"`  // Забанен по IP
}

func (m *Monitor) UpdateLatency(newLatency int64) {
	old := atomic.LoadInt64(&m.AverageLatency)
    if old == 0 {
        atomic.StoreInt64(&m.AverageLatency, newLatency)
        return
    }
    
    // Приблизительное скользящее среднее
    updated := (old*9 + newLatency) / 10
    atomic.StoreInt64(&m.AverageLatency, updated)
}

func (m *Monitor) StartRPSResetter(){
	ticker := time.NewTicker(time.Second)
    defer ticker.Stop()

    for range ticker.C {
        // Считываем и сбрасываем секундные счетчики
        rps := atomic.SwapInt64(&m.CurrentRPS, 0)
        traffic := atomic.SwapInt64(&m.CurrentTraffic, 0)

        m.updateHistory(rps, traffic)
    }
}

func (m *Monitor) GetCurrentRPS() int64 {
	m.mu.Lock()
	defer m.mu.Unlock()

	var totalRequestsInMinute int64 = 0
	for _, rps := range m.RPSHistory {
		totalRequestsInMinute += rps
	}

	// Возвращаем среднее количество запросов в секунду за последнюю минуту
	return totalRequestsInMinute / 60
}

//Сколько запросов в минуту
func (m *Monitor) GetRequestsPerMinute() int64 {
    var sum int64
    m.mu.Lock()
    for _, rps := range m.RPSHistory {
        sum += rps
    }
    m.mu.Unlock()
    return sum / 60
}

//Среднее время ответа
func (m *Monitor) GetAverageResponseTime() int64 {
    return atomic.LoadInt64(&m.AverageLatency)
}

func (m *Monitor) GetActiveConnections() int64 {
    return atomic.LoadInt64(&m.ActiveConnections)
}

func (m *Monitor) GetTotalRequests() int64 {
    return atomic.LoadInt64(&m.TotalRequests)
}

func (m *Monitor) GetRPSHistory() []int64 {
    m.mu.Lock()
    defer m.mu.Unlock()
    historyCopy := make([]int64, len(m.RPSHistory))
    copy(historyCopy, m.RPSHistory)
    return historyCopy
}

func (m *Monitor) GetTrafficHistory() []int64 {
    m.mu.Lock()
    defer m.mu.Unlock()
    historyCopy := make([]int64, len(m.TrafficHistory))
    copy(historyCopy, m.TrafficHistory)
    return historyCopy
}

//Клиенты, их статистика
func (m *Monitor) GetClientsMap() *sync.Map {
    return &m.ClientsMap
}

//Сколько всего трафика потрачено
func (m *Monitor) GetTotalTrafficBytes() int64 {
    return atomic.LoadInt64(&m.TotalTrafficBytes)
}

//Этот метод у меня дублируется выше
func (m *Monitor) GetLatency() int64 {
    return atomic.LoadInt64(&m.AverageLatency)
}

func (m *Monitor) Middleware() gin.HandlerFunc {
    return func(c *gin.Context) {
        // 1. Увеличиваем общий счетчик и текущий RPS сразу при входе запроса
        atomic.AddInt64(&m.TotalRequests, 1) 
        atomic.AddInt64(&m.CurrentRPS, 1) 

		atomic.AddInt64(&m.ActiveConnections, 1)
        defer atomic.AddInt64(&m.ActiveConnections, -1)

        start := time.Now()
        c.Next() // Выполнение остальных middleware и самого прокси
        
        // 2. После возврата ответа считаем Latency и Трафик
        latency := time.Since(start).Milliseconds()
        m.UpdateLatency(latency) // Метод для расчета среднего значения
        
        // Считаем размер ответа от Java-бэкенда
        size := int64(c.Writer.Size())
        atomic.AddInt64(&m.TotalTrafficBytes, size)
        atomic.AddInt64(&m.CurrentTraffic, size)

        // Здесь запрос ПОЛНОСТЬЮ завершился. Мы знаем ВСЁ.
        ip := c.ClientIP()
        bytesSent := int64(c.Writer.Size())
        if bytesSent < 0 { 
            bytesSent = 0 // На случай, если тело ответа было пустым
        }

        // Достаем статус блокировки (если IPFilter сработал, там будет true)
        blockReasonRaw, _ := c.Get("block_reason")
        blockReason := ""
        if val, ok := blockReasonRaw.(string); ok {
            blockReason = val
        }

        // Фиксируем всё в одном месте асинхронно!
        m.RecordClientActivity(ip, bytesSent, blockReason)
    }
}

func (m *Monitor) updateHistory(rps int64, traffic int64) {
    m.mu.Lock()
    defer m.mu.Unlock()

    m.RPSHistory[m.bufferIndex] = rps
    m.TrafficHistory[m.bufferIndex] = traffic

    // Сдвигаем индекс, если дошли до конца — возвращаемся в начало
    m.bufferIndex = (m.bufferIndex + 1) % 60
}

func (m *Monitor) RecordClientActivity(ip string, bytes int64, blockReason string) {
	// Достаем существующую статистику или создаем новую, если IP пришел впервые
	actual, _ := m.ClientsMap.LoadOrStore(ip, &ClientStats{IP: ip})
	stats := actual.(*ClientStats)

	// Атомарно увеличиваем счетчики
	atomic.AddInt64(&stats.TotalRequests, 1)
	atomic.AddInt64(&stats.BytesTransferred, bytes)
	switch blockReason {
	case "blacklist":
		atomic.AddInt64(&stats.BlockedBlacklist, 1)
	case "rate_limit":
		atomic.AddInt64(&stats.BlockedRateLimit, 1)
	}
}

func (m *Monitor) MaxConnectionsMiddleware(maxConn int64) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Считываем текущее кол-во соединений
		currentConns := m.GetActiveConnections()

		if currentConns >= maxConn {
			c.JSON(http.StatusTooManyRequests, gin.H{
				"error": "Server is busy. Too many active connections.",
			})
			c.Abort()
			return
		}

		
		m.IncrementActiveConnections()
		c.Next()
		// После завершения запроса декрементируем
		// m.DecrementActiveConnections()
	}
}

func (m *Monitor) IncrementActiveConnections() {
    m.mu.Lock()
    defer m.mu.Unlock()
    m.ActiveConnections++
}

func (m *Monitor) DecrementActiveConnections() {
    m.mu.Lock()
    defer m.mu.Unlock()
    m.ActiveConnections--
}