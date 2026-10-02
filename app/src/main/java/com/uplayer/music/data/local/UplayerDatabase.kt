package com.uplayer.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uplayer.music.data.local.dao.UplayerDao
import com.uplayer.music.data.local.entity.FavoriteEntity
import com.uplayer.music.data.local.entity.PlaylistEntity
import com.uplayer.music.data.local.entity.PlaylistTrackEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UplayerDatabase : RoomDatabase() {
    abstract fun dao(): UplayerDao

    companion object {
        const val DB_NAME = "uplayer.db"
    }
}
