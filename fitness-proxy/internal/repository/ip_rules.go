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

//go:generate mockgen -source=ip_rules.go -destination=mocks/mock_ip_rules.go -package=mocks
type IPRepository interface{
    GetAll(ctx context.Context) ([]model.IPRule, error)
    InsertRule(ctx context.Context, ip string, ruleType string) error
    DeleteByID(ctx context.Context, id string) error
    GetBlackList(ctx context.Context) ([]string, error)
    GetWhitelist(ctx context.Context) ([]string, error)
    GetGreylist(ctx context.Context) ([]string, error)
}

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

func (r *MongoIPRepo) GetBlackList(ctx context.Context) ([]string, error) {
    result, err := r.collection.Find(ctx, bson.M{"type": "black"})

    if err!= nil{
        erro := []string{""}
        return erro, err
    }

    var rules []model.IPRule
    if err = result.All(ctx, &rules); err != nil {
        erro := []string{""}
        return erro, err
    }

    var blackList []string
    for _, rule := range rules {
        blackList = append(blackList, rule.Network)
    }

    return blackList, nil
}


func (r *MongoIPRepo) GetWhitelist(ctx context.Context) ([]string, error) {
    result, err := r.collection.Find(ctx, bson.M{"type": "white"})

    if err!= nil{
        erro := []string{""}
        return erro, err
    }

    var rules []model.IPRule
    if err = result.All(ctx, &rules); err != nil {
        erro := []string{""}
        return erro, err
    }

    var whitelist []string
    for _, rule := range rules {
        whitelist = append(whitelist, rule.Network)
    }

    return whitelist, nil
}

func (r *MongoIPRepo) GetGreylist(ctx context.Context) ([]string, error) {
    result, err := r.collection.Find(ctx, bson.M{"type": "grey"})

    if err!= nil{
        erro := []string{""}
        return erro, err
    }

    var rules []model.IPRule
    if err = result.All(ctx, &rules); err != nil {
        erro := []string{""}
        return erro, err
    }

    var greylist []string
    for _, rule := range rules {
        greylist = append(greylist, rule.Network)
    }

    return greylist, nil
}