package service

import (
	"sync"
	"golang.org/x/time/rate"
)

type IPRateLimiter struct {
	ips map[string]*rate.Limiter
	mu  sync.RWMutex
	r   rate.Limit // сколько токенов в секунду
	b   int        // размер корзины (burst)
}

func NewIPRateLimiter(r rate.Limit, b int) *IPRateLimiter {
	return &IPRateLimiter{
		ips: make(map[string]*rate.Limiter),
		r:   r,
		b:   b,
	}
}

func (i *IPRateLimiter) GetLimiter(ip string, r rate.Limit, b int) *rate.Limiter {
	i.mu.Lock()
	defer i.mu.Unlock()

	limiter, exists := i.ips[ip]
	if !exists {
		limiter = rate.NewLimiter(i.r, i.b)
		i.ips[ip] = limiter
	}

	limiter.SetLimit(r)
    limiter.SetBurst(b)

	return limiter
}

//Додумать
func (i *IPRateLimiter) GetCount() int {
	i.mu.RLock()
	defer i.mu.RUnlock()
	return len(i.ips)
}