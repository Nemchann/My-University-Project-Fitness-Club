package repository

import (
    "context"
    "fitness-proxy/internal/model"
    "go.mongodb.org/mongo-driver/mongo"
)

// Интерфейс для тестов
type LogRepository interface {
    Save(ctx context.Context, log model.AccessLog) error
}

type mongoLogRepo struct {
    collection *mongo.Collection
}

func NewMongoLogRepo(db *mongo.Database) LogRepository {
    return &mongoLogRepo{
        collection: db.Collection("access_logs"),
    }
}

func (r *mongoLogRepo) Save(ctx context.Context, entry model.AccessLog) error {
    _, err := r.collection.InsertOne(ctx, entry)
    return err
}