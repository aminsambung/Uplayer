package com.uplayer.music.data.local

import android.content.Context
import com.uplayer.music.data.local.entity.PlaylistEntity
import com.uplayer.music.data.local.entity.PlaylistTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object PlaylistRepository {

    private fun dao(context: Context) = DatabaseProvider.get(context).dao()

    // ==================== OBSERVE ====================
    fun observePlaylists(context: Context): Flow<List<PlaylistEntity>> {
        return dao(context).observePlaylists()
    }

    fun observePlaylistTracks(
        context: Context,
        playlistId: Long
    ): Flow<List<Long>> {
        return dao(context).observePlaylistTrackIds(playlistId)
    }

    // ==================== CREATE / DELETE PLAYLIST ====================
    suspend fun createPlaylist(context: Context, name: String): Long =
        withContext(Dispatchers.IO) {
            dao(context).createPlaylist(PlaylistEntity(name = name))
        }

    suspend fun deletePlaylist(context: Context, playlistId: Long) =
        withContext(Dispatchers.IO) {
            dao(context).deletePlaylist(playlistId)
        }

    // ==================== ADD / REMOVE TRACK ====================
    suspend fun addTrackToPlaylist(
        context: Context,
        playlistId: Long,
        trackId: Long,
        position: Int
    ) = withContext(Dispatchers.IO) {
        dao(context).addTrackToPlaylist(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = trackId,
                position = position
            )
        )
    }

    suspend fun removeTrackFromPlaylist(
        context: Context,
        playlistId: Long,
        trackId: Long
    ) = withContext(Dispatchers.IO) {
        dao(context).removeTrackFromPlaylist(playlistId, trackId)
    }
}
