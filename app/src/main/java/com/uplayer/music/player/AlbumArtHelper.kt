package com.uplayer.music.player

import android.content.ContentUris
import android.net.Uri

object AlbumArtHelper {

    /**
     * URI album art dari MediaStore.
     * Format: content://media/external/audio/albumart/{albumId}
     */
    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
}
