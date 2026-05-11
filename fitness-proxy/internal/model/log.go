package model

import "time"

type AccessLog struct {
    ID         interface{} `bson:"_id,omitempty"`
    IP         string      `bson:"client_ip"`
    URL        string      `bson:"url"`
    Method     string      `bson:"method"`
    StatusCode int         `bson:"status_code"`
    Reason     string      `bson:"reason,omitempty"` // Например: "Blacklisted by administrator"
    Latency    int64       `bson:"latency_ms"`
    Timestamp  time.Time   `bson:"ts"`
}