package model

import (
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type CacheSetting struct {
    ID         primitive.ObjectID `bson:"_id,omitempty" json:"id"`
    Path       string             `bson:"path" json:"path"`
    TTLSeconds int                `bson:"ttl_seconds" json:"ttl_seconds"`
}