package com.uplayer.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uplayer.music.domain.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val duration: Long,
    val trackNumber: Int?,
    val folder: String?,
    val isFavorite: Boolean = false
)

// ==================== MAPPERS ====================
fun TrackEntity.toDomain(): Track = Track(
    id = id,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    trackNumber = trackNumber,
    folder = folder,
    isFavorite = isFavorite
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    trackNumber = trackNumber,
    folder = folder,
    isFavorite = isFavorite
)
