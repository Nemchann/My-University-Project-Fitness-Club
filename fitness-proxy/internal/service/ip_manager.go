package service

import (
	"github.com/yl2chen/cidranger"
	"net"
	"fmt"

    "fitness-proxy/internal/model"
)

type IPManager struct {
    whitelist cidranger.Ranger
    blacklist cidranger.Ranger
}

func NewIPManager() *IPManager {
    return &IPManager{
        whitelist: cidranger.NewPCTrieRanger(),
        blacklist: cidranger.NewPCTrieRanger(),
    }
}

func (m *IPManager) IsAllowed(ip net.IP) (bool, string) {
    // 1. Проверяем черный список (высший приоритет п. 1.2.1)
    if contains, _ := m.blacklist.Contains(ip); contains {
        return false, "blacklisted"
    }

    // 2. Проверяем белый список (п. 1.2.2)
    if contains, _ := m.whitelist.Contains(ip); contains {
        return true, "whitelisted"
    }

    // 3. Политика по умолчанию (п. 1.2.3)
    return true, "default" 
}

func (m *IPManager) AddRule(network string, ruleType string) error{
	
    _, ipNet, err := net.ParseCIDR(network)
    if err != nil {
        return err
    }

    // 2. Создаем запись для рейнджера
    entry := cidranger.NewBasicRangerEntry(*ipNet)

    // 3. Распределяем по спискам в зависимости от типа
    switch ruleType {
    case "white":
        return m.whitelist.Insert(entry)
    case "black":
        return m.blacklist.Insert(entry)
    case "grey":
        // Если решишь добавить логику для серых списков
        return nil 
    default:
        return fmt.Errorf("unknown rule type: %s", ruleType)
    }
}

//перезапись правил IP
func (m *IPManager) Reload(rules []model.IPRule) error {
    // Создаем новые чистые рейнджеры
    newWhitelist := cidranger.NewPCTrieRanger()
    newBlacklist := cidranger.NewPCTrieRanger()

    for _, rule := range rules {
        _, ipNet, err := net.ParseCIDR(rule.Network)
        if err != nil {
            return err
        }
        entry := cidranger.NewBasicRangerEntry(*ipNet)
        
        if rule.Type == "white" {
            newWhitelist.Insert(entry)
        } else if rule.Type == "black" {
            newBlacklist.Insert(entry)
        }
    }

    // В Go замена указателя — операция быстрая и безопасная
    m.whitelist = newWhitelist
    m.blacklist = newBlacklist
    return nil
}