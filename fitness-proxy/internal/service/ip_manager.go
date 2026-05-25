package service

import (
	"context"
	"fitness-proxy/internal/model"
	"fitness-proxy/internal/repository"
	"fmt"
	"net"

	"github.com/yl2chen/cidranger"
)

type IPManager struct {
	whitelist    cidranger.Ranger
	blacklist    cidranger.Ranger
	greylist     cidranger.Ranger
	blockedCount int
	repository   repository.IPRepository
}

func NewIPManager(repository repository.IPRepository) *IPManager {
	return &IPManager{
		whitelist:  cidranger.NewPCTrieRanger(),
		blacklist:  cidranger.NewPCTrieRanger(),
		greylist:   cidranger.NewPCTrieRanger(),
		repository: repository,
	}
}

func (m *IPManager) IsAllowed(ip net.IP) (bool, string) {
	// Проверяем черный список (высший приоритет)
	if contains, _ := m.blacklist.Contains(ip); contains {
		m.blockedCount++
		return false, "blacklisted"
	}

	// Проверяем белый список
	if contains, _ := m.whitelist.Contains(ip); contains {
		return true, "whitelisted"
	}

	// Серый список
	if contains, _ := m.greylist.Contains(ip); contains {
		return true, "grey"
	}

	// Политика по умолчанию
	return true, "default"
}

// Добавляем правило для ip
func (m *IPManager) AddRule(network string, ruleType string) error {

	_, ipNet, err := net.ParseCIDR(network)
	if err != nil {
		// Пробуем распарсить его.
		ip := net.ParseIP(network)
		if ip == nil {
			return fmt.Errorf("invalid IP or CIDR address: %s", network)
		}

		// Превращаем одиночный IP в CIDR-маску
		mask := net.CIDRMask(32, 32)
		if ip.To4() == nil {
			mask = net.CIDRMask(128, 128)
		}
		ipNet = &net.IPNet{IP: ip, Mask: mask}
	}

	// Создаем запись для рейнджера
	entry := cidranger.NewBasicRangerEntry(*ipNet)

	// Распределяем по спискам в зависимости от типа
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

// Правило по IP
func (m *IPManager) GetRuleInfo(ip net.IP) string {
	// Проверяем черный список (высший приоритет)
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

// Удалить правило для IP
func (m *IPManager) RemoveRule(ctx context.Context, id string) error {
	err := m.repository.DeleteByID(ctx, id)
	if err != nil {
		return err
	}
	return nil
}

// Перезапись правил IP
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

	m.whitelist = newWhitelist
	m.blacklist = newBlacklist
	return nil
}

// ReloadFromDB самостоятельно вычитывает правила из репозитория и обновляет деревья в памяти
func (m *IPManager) ReloadFromDB(ctx context.Context) error {
	// Вытягиваем все правила из репозитория
	rules, err := m.repository.GetAll(ctx)
	if err != nil {
		return fmt.Errorf("failed to fetch rules from repository: %w", err)
	}

	// Создаем новые чистые рейнджеры
	newWhitelist := cidranger.NewPCTrieRanger()
	newBlacklist := cidranger.NewPCTrieRanger()
	newGreylist := cidranger.NewPCTrieRanger()

	// Заполняем новые деревья
	for _, rule := range rules {
		var ipNet *net.IPNet

		_, ipNet, err = net.ParseCIDR(rule.Network)
		if err != nil {
			// Если это одиночный IP, превращаем его в маску /32
			ip := net.ParseIP(rule.Network)
			if ip == nil {
				// Пропускаем битое правило, чтобы всё дерево не падало
				continue
			}
			mask := net.CIDRMask(32, 32)
			if ip.To4() == nil {
				mask = net.CIDRMask(128, 128) // Для IPv6
			}
			ipNet = &net.IPNet{IP: ip, Mask: mask}
		}

		entry := cidranger.NewBasicRangerEntry(*ipNet)

		// Распределяем по новым деревьям
		switch rule.Type {
		case "white":
			_ = newWhitelist.Insert(entry)
		case "black":
			_ = newBlacklist.Insert(entry)
		case "grey":
			_ = newGreylist.Insert(entry)
		}
	}

	// Атомарно и безопасно заменяем старые деревья на новые
	m.whitelist = newWhitelist
	m.blacklist = newBlacklist
	m.greylist = newGreylist

	return nil
}

func (m *IPManager) GetAll(ctx context.Context) ([]model.IPRule, error) {
	return m.repository.GetAll(ctx)
}

// Обновить правило по IP
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

func (m *IPManager) GetRulesCount() int {
	return m.blacklist.Len() + m.whitelist.Len() + m.greylist.Len()
}

func (m *IPManager) CheckIPAccess(ctx context.Context, ip net.IP) (bool, string) {
	// Проверяем черный список (высший приоритет)
	if contains, _ := m.blacklist.Contains(ip); contains {
		m.blockedCount++
		return false, "blacklisted"
	}
	// Проверяем белый список
	if contains, _ := m.whitelist.Contains(ip); contains {
		return true, "whitelisted"
	}
	// Серый список
	if contains, _ := m.greylist.Contains(ip); contains {
		return true, "grey"
	}
	return true, "default"
}
