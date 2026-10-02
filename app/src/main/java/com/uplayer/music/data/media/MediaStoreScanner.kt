package com.uplayer.music.data.media

import android.content.Context
import android.provider.MediaStore
import com.uplayer.music.data.local.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun scan(): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val filePath = cursor.getString(dataCol) ?: ""
                val folder = filePath.substringBeforeLast('/', "")

                tracks.add(
                    TrackEntity(
                        id = id,
                        uri = "content://media/external/audio/media/$id",
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol)?.takeIf {
                            it.isNotBlank() && it != "<unknown>"
                        },
                        album = cursor.getString(albumCol)?.takeIf {
                            it.isNotBlank() && it != "<unknown>"
                        },
                        albumId = cursor.getLong(albumIdCol),
                        duration = cursor.getLong(durationCol),
                        trackNumber = cursor.getInt(trackCol).takeIf { it > 0 },
                        folder = folder.takeIf { it.isNotEmpty() }
                    )
                )
            }
        }

        tracks
    }
}
