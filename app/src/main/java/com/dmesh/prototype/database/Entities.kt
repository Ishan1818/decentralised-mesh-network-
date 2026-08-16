package com.dmesh.prototype.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey val nodeId: String,
    val displayName: String,
    val battery: Int,
    val rssi: Int,
    val lastSeen: Long,
    val latitude: Double?,
    val longitude: Double?,
    val statuses: String,
    val transportAddress: String?
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val timestamp: Long,
    val priority: String,
    val type: String,
    val payload: String,
    val state: String,
    val routePath: String,
    val expiresAt: Long
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val destinationId: String,
    val nextHop: String,
    val path: String,
    val hopCount: Int,
    val sequenceNumber: Long,
    val createdAt: Long,
    val expiresAt: Long
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val category: String,
    val message: String,
    val detail: String?
)

@Entity(tableName = "seen_messages")
data class SeenMessageEntity(
    @PrimaryKey val messageId: String,
    val seenAt: Long
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val nodeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val updatedAt: Long
)
