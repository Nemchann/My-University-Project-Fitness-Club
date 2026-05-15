package service

import (
	"time"
	"github.com/gin-gonic/gin"
	"sync/atomic"
	"sync"
)

type Monitor struct {
    CurrentRPS    int64
    TotalRequests int64
	AverageLatency int64
	ActiveConnections int64
	CurrentTraffic int64
	RPSHistory     []int64
    TrafficHistory []int64 // байт в секунду
    bufferIndex    int
    mu             sync.Mutex
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
        
        // Считаем размер ответа для статистики трафика
        responseSize := int64(c.Writer.Size())
        atomic.AddInt64(&m.CurrentTraffic, responseSize) //Исправить ошибку
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
