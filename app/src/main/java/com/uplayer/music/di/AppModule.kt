package com.uplayer.music.di

import android.content.Context
import androidx.room.Room
import com.uplayer.music.data.local.UplayerDatabase
import com.uplayer.music.data.local.dao.TrackDao
import com.uplayer.music.data.media.MediaStoreScanner
import com.uplayer.music.data.repository.OfflineMusicRepository
import com.uplayer.music.domain.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): UplayerDatabase = Room.databaseBuilder(
        context,
        UplayerDatabase::class.java,
        UplayerDatabase.DB_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideTrackDao(db: UplayerDatabase): TrackDao = db.trackDao()

    @Provides
    @Singleton
    fun provideMusicRepository(
        trackDao: TrackDao,
        scanner: MediaStoreScanner
    ): MusicRepository = OfflineMusicRepository(trackDao, scanner)
}
