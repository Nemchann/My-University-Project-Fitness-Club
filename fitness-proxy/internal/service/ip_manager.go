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
    greylist  cidranger.Ranger // Если решишь добавить логику для серых списков
}

func NewIPManager() *IPManager {
    return &IPManager{
        whitelist: cidranger.NewPCTrieRanger(),
        blacklist: cidranger.NewPCTrieRanger(),
        greylist:  cidranger.NewPCTrieRanger(),
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

    // 3. Серый список
    if contains, _ := m.greylist.Contains(ip); contains {
        return true, "grey"
    }

    // 4. Политика по умолчанию (п. 1.2.3)
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
        return m.greylist.Insert(entry) 
    default:
        return fmt.Errorf("unknown rule type: %s", ruleType)
    }
}

func (m *IPManager) GetRuleInfo(ip net.IP) (string) {
    // 1. Проверяем черный список (высший приоритет п. 1.2.1)
    if contains, _ := m.blacklist.Contains(ip); contains {
        return "blacklisted"
    }
    if contains, _ := m.whitelist.Contains(ip); contains {
        return "whitelisted"
    }
    if contains, _ := m.greylist.Contains(ip); contains {
        return "grey"
    }

    return "default"
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

func (m *IPManager) UpdateRule(network string, ruleType string) error {

    _, ipNet, err := net.ParseCIDR(network)
    if err != nil {
        return err
    }
    // Удаляем из всех списков, чтобы избежать конфликтов
    m.whitelist.Remove(*ipNet)
    m.blacklist.Remove(*ipNet)
    m.greylist.Remove(*ipNet)

    return m.AddRule(network, ruleType)
}