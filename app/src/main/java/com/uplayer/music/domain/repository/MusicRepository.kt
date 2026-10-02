package com.uplayer.music.domain.repository

import com.uplayer.music.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun observeTracks(): Flow<List<Track>>

    fun observeFavorites(): Flow<List<Track>>

    fun searchTracks(query: String): Flow<List<Track>>

    suspend fun getTrackById(id: Long): Track?

    suspend fun toggleFavorite(id: Long)

    suspend fun refresh()
}
