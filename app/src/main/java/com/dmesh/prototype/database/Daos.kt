package com.dmesh.prototype.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: NodeEntity)

    @Query("SELECT * FROM nodes")
    suspend fun getAll(): List<NodeEntity>

    @Query("DELETE FROM nodes WHERE nodeId = :nodeId")
    suspend fun delete(nodeId: String)
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    suspend fun getAll(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun delete(messageId: String)
}

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(route: RouteEntity)

    @Query("SELECT * FROM routes")
    suspend fun getAll(): List<RouteEntity>

    @Query("DELETE FROM routes WHERE destinationId = :destinationId")
    suspend fun delete(destinationId: String)
}

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<EventEntity>
}

@Dao
interface SeenMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markSeen(entity: SeenMessageEntity)

    @Query("SELECT * FROM seen_messages WHERE messageId = :messageId")
    suspend fun get(messageId: String): SeenMessageEntity?
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(location: LocationEntity)

    @Query("SELECT * FROM locations")
    suspend fun getAll(): List<LocationEntity>
}
