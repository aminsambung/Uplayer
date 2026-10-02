package com.uplayer.music.data.repository

import com.uplayer.music.data.local.dao.TrackDao
import com.uplayer.music.data.local.entity.toDomain
import com.uplayer.music.data.media.MediaStoreScanner
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineMusicRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val scanner: MediaStoreScanner
) : MusicRepository {

    override fun observeTracks(): Flow<List<Track>> =
        trackDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }

    override fun observeFavorites(): Flow<List<Track>> =
        trackDao.observeFavorites().map { list ->
            list.map { it.toDomain() }
        }

    override fun searchTracks(query: String): Flow<List<Track>> =
        trackDao.search(query).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getTrackById(id: Long): Track? =
        trackDao.getById(id)?.toDomain()

    override suspend fun toggleFavorite(id: Long) {
        val track = trackDao.getById(id) ?: return
        trackDao.setFavorite(id, !track.isFavorite)
    }

    override suspend fun refresh() {
        val scanned = scanner.scan()

        // Preserve favorite status yang sudah ada
        val existingFavorites = trackDao.observeAll()
        // Ambil dari database dulu, baru replace
        // Simpel: insert tanpa hapus agar playlist reference tetap valid
        trackDao.insertAll(scanned)
    }
}
