package com.uplayer.music.domain.model

data class Track(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val duration: Long,       // milliseconds
    val trackNumber: Int?,
    val folder: String?,
    val isFavorite: Boolean = false
)

fun Track.formattedDuration(): String {
    val totalSeconds = duration / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
