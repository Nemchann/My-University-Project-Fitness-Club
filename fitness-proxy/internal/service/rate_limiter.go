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
	Hour   *rate.Limiter // RPH
	Day    *rate.Limiter // RPD
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

func (i *IPRateLimiter) GetLimiters(ip string, rps float64, rpm, rph, rpd, b int) *IPLimiters {
    i.mu.Lock()
    defer i.mu.Unlock()

    limiters, exists := i.ips[ip]
	if !exists {
		limiters = &IPLimiters{
			// RPS: генерация rps токенов в секунду, размер корзины b
			Second: rate.NewLimiter(rate.Limit(rps), i.b),
			
			// RPM: разрешаем rpm запросов в минуту. 1 токен генерируется каждые (минута / rpm)
			Minute: rate.NewLimiter(rate.Every(time.Minute/time.Duration(rpm)), rpm),
			
			// RPH: разрешаем rph запросов в час. 1 токен каждые (час / rph)
			Hour:   rate.NewLimiter(rate.Every(time.Hour/time.Duration(rph)), rph),
			
			// RPD: разрешаем rpd запросов в сутки. 1 токен каждые (24 часа / rpd)
			Day:    rate.NewLimiter(rate.Every(24*time.Hour/time.Duration(rpd)), rpd),
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