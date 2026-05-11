package service

import (
	"github.com/yl2chen/cidranger"
	"net"
	"testing"
)

//Додумать тесты для IPManager, чтобы проверить все 3 сценария: черный список, белый список и политика по умолчанию
func TestIPManager_IsAllowed(t *testing.T) {
    manager := NewIPManager()
    
    // Вручную добавим тестовые данные
    _, network, _ := net.ParseCIDR("192.168.1.0/24")
    manager.blacklist.Insert(cidranger.NewBasicRangerEntry(*network))

    // Проверяем IP из этого диапазона
    allowed, reason := manager.IsAllowed(net.ParseIP("192.168.1.5"))

    if allowed {
        t.Errorf("Ожидался запрет для забаненного IP, но доступ разрешен")
    }
    if reason != "blacklisted" {
        t.Errorf("Ожидалась причина blacklisted, получено: %s", reason)
    }
}