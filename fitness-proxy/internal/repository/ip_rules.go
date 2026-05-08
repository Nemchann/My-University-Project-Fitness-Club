package repository

import (
	"context"
	"fitness-proxy/internal/model"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

type mongoIPRepo struct {
	collection *mongo.Collection
}

func NewMongoIPRepo(db *mongo.Database) *mongoIPRepo {
	return &mongoIPRepo{
		collection: db.Collection("ip_rules"),
	}
}

//Метод получения всех правил
func (r *mongoIPRepo) GetAll(ctx context.Context) ([]model.IPRule, error) {
    var rules []model.IPRule
    cursor, err := r.collection.Find(ctx, bson.M{}) // bson.M{} — это пустой фильтр (берем всё)
    if err != nil {
        return nil, err
    }
    defer cursor.Close(ctx)

    if err = cursor.All(ctx, &rules); err != nil {
        return nil, err
    }
    return rules, nil
}