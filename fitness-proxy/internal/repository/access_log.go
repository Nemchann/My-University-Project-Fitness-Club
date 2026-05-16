package repository

import (
    "context"
    "fitness-proxy/internal/model"
    "go.mongodb.org/mongo-driver/mongo"
    "go.mongodb.org/mongo-driver/mongo/options"
    "go.mongodb.org/mongo-driver/bson"
)

//Генерация: go generate ./internal/repository    
////go:generate mockgen -source=access_log.go -destination=mocks/mock_access_log.go -package=mocks
type LogRepository interface {
    Save(ctx context.Context, log model.AccessLog) error
    GetLogs(ctx context.Context, filter bson.M, limit int64) ([]model.AccessLog, error)
}

type MongoLogRepository struct {
    collection *mongo.Collection
}
func NewMongoLogRepository(db *mongo.Database) *MongoLogRepository {
    return &MongoLogRepository{
        collection: db.Collection("access_logs"),
    }
}

func (r *MongoLogRepository) Save(ctx context.Context, entry model.AccessLog) error {
    _, err := r.collection.InsertOne(ctx, entry)
    return err
}

func (r *MongoLogRepository) GetLogs(ctx context.Context, filter bson.M, limit int64) ([]model.AccessLog, error) {
    findOptions := options.Find().SetSort(bson.M{"timestamp": -1}).SetLimit(limit)
    cursor, err := r.collection.Find(ctx, filter, findOptions)
    if err != nil {
        return nil, err
    }
    defer cursor.Close(ctx)

    var logs []model.AccessLog
    if err := cursor.All(ctx, &logs); err != nil {
        return nil, err
    }
    return logs, nil
}