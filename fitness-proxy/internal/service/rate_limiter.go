package service

import (
	"sync"
	"golang.org/x/time/rate"
	"time"
)

type IPRateLimiter struct {
	ips map[string]*IPLimiters
	mu  sync.RWMutex
	r   rate.Limit // сколько токенов в секунду
	b   int        // размер корзины (burst)
}

func NewIPRateLimiter(r rate.Limit, b int) *IPRateLimiter {
	return &IPRateLimiter{
		ips: make(map[string]*IPLimiters),
		r:   r,
		b:   b,
	}
}

type IPLimiters struct {
    Second *rate.Limiter
    Minute *rate.Limiter
}

// func (i *IPRateLimiter) GetLimiter(ip string, r rate.Limit, b int) *rate.Limiter {
// 	i.mu.Lock()
// 	defer i.mu.Unlock()

// 	limiter, exists := i.ips[ip]
// 	if !exists {
// 		limiter = rate.NewLimiter(i.r, i.b)
// 		i.ips[ip] = limiter
// 	}

// 	limiter.SetLimit(r)
//     limiter.SetBurst(b)

// 	return limiter
// }

func (i *IPRateLimiter) GetLimiters(ip string, r float64, b int) *IPLimiters {
    i.mu.Lock()
    defer i.mu.Unlock()

    limiters, exists := i.ips[ip]
	if !exists {
		// Инициализируем наш набор лимитов
		limiters = &IPLimiters{
			Second: rate.NewLimiter(rate.Limit(r), b),
			// rate.Every(time.Minute/60) означает "один раз в секунду", 
			// но с корзиной (Burst) в 60 токенов. 
			Minute: rate.NewLimiter(rate.Every(time.Minute/60), 60),
		}
		i.ips[ip] = limiters
	}

	return limiters
}

//Додумать
func (i *IPRateLimiter) GetCount() int {
	i.mu.RLock()
	defer i.mu.RUnlock()
	return len(i.ips)
}