package com.uplayer.music.data.tag

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

data class TrackTags(
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val year: String?
)

object TagEditor {

    fun readTags(filePath: String): Result<TrackTags> = runCatching {
        val file = File(filePath)
        require(file.exists()) { "File tidak ditemukan" }

        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tagOrCreateAndSetDefault

        TrackTags(
            title = tag.getFirst(FieldKey.TITLE).ifBlank { null },
            artist = tag.getFirst(FieldKey.ARTIST).ifBlank { null },
            album = tag.getFirst(FieldKey.ALBUM).ifBlank { null },
            genre = tag.getFirst(FieldKey.GENRE).ifBlank { null },
            year = tag.getFirst(FieldKey.YEAR).ifBlank { null }
        )
    }

    fun writeTags(
        filePath: String,
        title: String?,
        artist: String?,
        album: String?,
        genre: String?,
        year: String?
    ): Result<Unit> = runCatching {
        val file = File(filePath)
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tagOrCreateAndSetDefault

        title?.let { tag.setField(FieldKey.TITLE, it) }
        artist?.let { tag.setField(FieldKey.ARTIST, it) }
        album?.let { tag.setField(FieldKey.ALBUM, it) }
        genre?.let { tag.setField(FieldKey.GENRE, it) }
        year?.let { tag.setField(FieldKey.YEAR, it) }

        AudioFileIO.write(audioFile)
    }
}
