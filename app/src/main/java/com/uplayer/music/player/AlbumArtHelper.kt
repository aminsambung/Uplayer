package com.uplayer.music.player

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object AlbumArtHelper {

    /**
     * URI album art dari MediaStore (Android 10+).
     * Format: content://media/external/audio/albumart/{albumId}
     */
    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }

    /**
     * Fallback: cari album art via MediaStore.Audio.Albums.
     */
    fun findAlbumArtByAlbumName(context: Context, albumName: String): Uri? {
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM
        )
        val selection = "${MediaStore.Audio.Albums.ALBUM} = ?"
        val args = arrayOf(albumName)

        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                return getAlbumArtUri(id)
            }
        }
        return null
    }
}
