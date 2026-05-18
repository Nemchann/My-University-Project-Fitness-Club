package service

import (
    "context"
    "fitness-proxy/internal/model"
    "fitness-proxy/internal/repository"
    "go.mongodb.org/mongo-driver/bson"
)
type LogService struct {
    repo repository.LogRepository
}

func NewLogService(repo repository.LogRepository) *LogService {
    return &LogService{repo: repo}
}

func (s *LogService) GetAuditLogs(ctx context.Context, level, ip string) ([]model.AccessLog, error) {
    filter := bson.M{}
    if level != "" {
        filter["level"] = level
    }
    if ip != "" {
        filter["ip"] = ip
    }

    // Задаем лимит в 100 записей на уровне бизнес-логики
    return s.repo.GetLogs(ctx, filter, 100)
}