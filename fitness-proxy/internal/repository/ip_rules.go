package repository

import (
	"context"
	"fitness-proxy/internal/model"
	"log"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
    "go.mongodb.org/mongo-driver/bson/primitive"
    "fmt"
)

type MongoIPRepo struct {
	collection *mongo.Collection
}

func NewMongoIPRepo(db *mongo.Database) *MongoIPRepo {
	return &MongoIPRepo{
		collection: db.Collection("ip_rules"),
	}
}

//Метод получения всех правил
func (r *MongoIPRepo) GetAll(ctx context.Context) ([]model.IPRule, error) {
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

func (r *MongoIPRepo) InsertRule(ctx context.Context, ip string, ruleType string) error {
    doc := bson.M{
        "ip":         ip,
        "type":       ruleType,
    }

    result, error := r.collection.InsertOne(ctx, doc)
    if error != nil {
        return error
    }
    log.Printf("Inserted new IP rule with ID: %v", result.InsertedID)

    return nil
}


func (r *MongoIPRepo) DeleteByID(ctx context.Context, id string) error {
    // Превращаем строку в ObjectID
    objID, err := primitive.ObjectIDFromHex(id)
    if err != nil {
        return err // Ошибка, если строка — не валидный hex-код ID
    }

    result, error := r.collection.DeleteOne(ctx, bson.M{"_id": objID})
    if error != nil {
        return error
    }

    if result.DeletedCount == 0 {
        return fmt.Errorf("документ с id %s не найден", id)
    }

    log.Printf("Deleted IP rule with ID: %v", result.DeletedCount)
    return nil
}