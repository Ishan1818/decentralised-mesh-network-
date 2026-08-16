package com.dmesh.prototype.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NodeEntity::class,
        MessageEntity::class,
        RouteEntity::class,
        EventEntity::class,
        SeenMessageEntity::class,
        LocationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DMeshDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun messageDao(): MessageDao
    abstract fun routeDao(): RouteDao
    abstract fun eventDao(): EventDao
    abstract fun seenMessageDao(): SeenMessageDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var instance: DMeshDatabase? = null

        fun get(context: Context): DMeshDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DMeshDatabase::class.java,
                    "dmesh.db"
                ).build().also { instance = it }
            }
    }
}
