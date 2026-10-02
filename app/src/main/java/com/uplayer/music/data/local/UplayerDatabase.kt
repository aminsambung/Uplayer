package com.uplayer.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uplayer.music.data.local.dao.TrackDao
import com.uplayer.music.data.local.entity.TrackEntity

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UplayerDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        const val DB_NAME = "uplayer.db"
    }
}
