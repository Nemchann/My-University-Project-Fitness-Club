package model

import (
	"time"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type IPRule struct {
    ID        primitive.ObjectID `bson:"_id,omitempty"`
    Network   string             `bson:"network"` // Например: "192.168.1.0/24"
    Type      string             `bson:"type"`    // "white", "black", "grey"
    Rate      float64            `bson:"rate"`  // количество запросов в секунду
    Burst     int                `bson:"burst"` // максимальный всплеск
    Comment   string             `bson:"comment"` // Для чего это правило (необязательно, но полезно)
    UpdatedAt time.Time          `bson:"updated_at"`
}