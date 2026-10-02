package com.uplayer.music.data.local

import android.content.Context
import com.uplayer.music.data.local.entity.FavoriteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object FavoriteRepository {

    private fun dao(context: Context) =
        DatabaseProvider.get(context).dao()

    fun observeFavoriteIds(context: Context): Flow<List<Long>> {
        return dao(context).observeFavoriteIds()
    }

    suspend fun toggleFavorite(context: Context, trackId: Long): Boolean =
        withContext(Dispatchers.IO) {
            val isFav = dao(context).isFavorite(trackId)
            if (isFav) {
                dao(context).removeFavorite(trackId)
                false
            } else {
                dao(context).addFavorite(FavoriteEntity(trackId))
                true
            }
        }

    suspend fun isFavorite(context: Context, trackId: Long): Boolean =
        withContext(Dispatchers.IO) {
            dao(context).isFavorite(trackId)
        }
}
