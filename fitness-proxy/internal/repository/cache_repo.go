package repository

import (
	"context"
    "fitness-proxy/internal/model"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"fmt"
	"log"
)



type MongoCacheRepo struct {
    collection *mongo.Collection
}

func NewMongoCacheRepo(db *mongo.Database) *MongoCacheRepo {
	return &MongoCacheRepo{
		collection: db.Collection("cache_settings"),
	}
}

func (r *MongoCacheRepo) GetSettings(ctx context.Context) ([]model.CacheSetting, error) {
    cursor, err := r.collection.Find(ctx, bson.M{})
    if err != nil {
        return nil, err
    }
    var settings []model.CacheSetting
    err = cursor.All(ctx, &settings)
    return settings, err
}

func (r *MongoCacheRepo) GetTTLForPath(ctx context.Context, path string) (int, error) {
	var setting model.CacheSetting
	err := r.collection.FindOne(ctx, bson.M{"path": path}).Decode(&setting)
	if err != nil {
		return 0, err
	}
	return setting.TTLSeconds, nil
}

func (r *MongoCacheRepo) GetByID(ctx context.Context, id string) (*model.CacheSetting, error) {
	var setting model.CacheSetting
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil{
		return nil, err
	}
	errN := r.collection.FindOne(ctx, bson.M{"_id": objID}).Decode(&setting)
	if errN != nil {
		return nil, errN
	}
	return &setting, nil
}

func (r *MongoCacheRepo) DeleteById(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)

	if err != nil{
		return err
	}

	result, err := r.collection.DeleteOne(ctx, bson.M{"_id": objID})

	if result.DeletedCount == 0 {
        return fmt.Errorf("документ с id %s не найден", id)
    }

	log.Printf("Deleted cache setting with ID: %v", result.DeletedCount)
    return nil
}

func (r *MongoCacheRepo) DeleteByPath(ctx context.Context, path string) error {
	result, err := r.collection.DeleteOne(ctx, bson.M{"path": path})
	if err != nil{
		return err
	}

	if result.DeletedCount == 0 {
        return fmt.Errorf("документ с данным path %s не найден", path)
    }

	log.Printf("Deleted cache setting with path: %v", result.DeletedCount)
    return nil
}