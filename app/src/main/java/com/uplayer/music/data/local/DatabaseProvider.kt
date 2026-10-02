package com.uplayer.music.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: UplayerDatabase? = null

    fun get(context: Context): UplayerDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                UplayerDatabase::class.java,
                UplayerDatabase.DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
