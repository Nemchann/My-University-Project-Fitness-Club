package service

import (
	"fmt"
	"net"
    "context"
	"github.com/yl2chen/cidranger"
    "fitness-proxy/internal/repository"
	"fitness-proxy/internal/model"
)

type IPManager struct {
    whitelist cidranger.Ranger
    blacklist cidranger.Ranger
    greylist  cidranger.Ranger 
    blockedCount int
    repository repository.IPRepository
}

func NewIPManager(repository repository.IPRepository) *IPManager {
    return &IPManager{
        whitelist: cidranger.NewPCTrieRanger(),
        blacklist: cidranger.NewPCTrieRanger(),
        greylist:  cidranger.NewPCTrieRanger(),
        repository: repository,
    }
}

func (m *IPManager) IsAllowed(ip net.IP) (bool, string) {
    // 1. Проверяем черный список (высший приоритет п. 1.2.1)
    if contains, _ := m.blacklist.Contains(ip); contains {
        m.blockedCount++
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
        // Если не получилось, возможно это одиночный IP. Пробуем распарсить его.
        ip := net.ParseIP(network)
        if ip == nil {
            return fmt.Errorf("invalid IP or CIDR address: %s", network)
        }
        
        // Превращаем одиночный IP в CIDR-маску (/32 для IPv4 или /128 для IPv6)
        mask := net.CIDRMask(32, 32)
        if ip.To4() == nil {
            mask = net.CIDRMask(128, 128)
        }
        ipNet = &net.IPNet{IP: ip, Mask: mask}
    }

    // 2. Создаем запись для рейнджера
    entry := cidranger.NewBasicRangerEntry(*ipNet)

    // 3. Распределяем по спискам в зависимости от типа
    switch ruleType {
    case "white":
        m.repository.InsertRule(context.Background(), network, "white")
        return m.whitelist.Insert(entry)
    case "black":
        m.repository.InsertRule(context.Background(), network, "black")
        return m.blacklist.Insert(entry)
    case "grey":
        m.repository.InsertRule(context.Background(), network, "grey")
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

func (m *IPManager) GetBlockedCount() int {
    return m.blockedCount
}

func (m *IPManager) GetRulesCount() int{
    return m.blacklist.Len() + m.whitelist.Len() + m.greylist.Len()
}

func (m *IPManager) CheckIPAccess(ctx context.Context, ip net.IP) (bool, string) {
    // 1. Проверяем черный список (высший приоритет п. 1.2.1)
    if contains, _ := m.blacklist.Contains(ip); contains {
        m.blockedCount++
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
    return true, "default"
}