package com.uplayer.music.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.uplayer.music.domain.model.Track

class PlayerManager private constructor(context: Context) {

    private val exoPlayer: ExoPlayer =
        ExoPlayer.Builder(context.applicationContext).build()

    private var playlist: List<Track> = emptyList()

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    init {
        exoPlayer.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                val index = exoPlayer.currentMediaItemIndex
                currentTrack = playlist.getOrNull(index)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    // Biarkan ExoPlayer auto-lanjut ke track berikutnya
                }
            }
        })
    }

    /** Putar track dari daftar tertentu. */
    fun playTrack(track: Track, tracks: List<Track>) {
        playlist = tracks
        val index = tracks.indexOfFirst { it.id == track.id }
            .coerceAtLeast(0)

        val items = tracks.map { MediaItem.fromUri(it.uri) }

        exoPlayer.setMediaItems(items, index, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        currentTrack = track
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
    }

    fun previous() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms)
    }

    fun currentPosition(): Long = exoPlayer.currentPosition

    fun duration(): Long = exoPlayer.duration.coerceAtLeast(0L)

    fun release() {
        exoPlayer.release()
    }

    companion object {
        @Volatile
        private var instance: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return instance ?: synchronized(this) {
                instance ?: PlayerManager(context).also { instance = it }
            }
        }
    }
}
