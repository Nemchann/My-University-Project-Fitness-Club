package model

import (
	"time"

)

type AccessLog struct {
    ID         interface{} `bson:"_id,omitempty"`
    IP         string      `bson:"client_ip"`
    Level      string      `bson:"level"` // Например: "info", "warning", "error"
    URL        string      `bson:"url"`
    Method     string      `bson:"method"`
    RequestID  string      `bson:"request_id,omitempty"` // Уникальный ID для отслеживания запроса
    StatusCode int         `bson:"status_code"`
    Reason     string      `bson:"reason,omitempty"` // Например: "Blacklisted by administrator"
    Latency    int64       `bson:"latency_ms"`
    Timestamp  time.Time   `bson:"ts"`
}