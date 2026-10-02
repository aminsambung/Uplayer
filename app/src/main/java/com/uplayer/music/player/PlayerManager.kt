package com.uplayer.music.player

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.uplayer.music.domain.model.Track

/**
 * Wrapper untuk MediaController yang terhubung ke PlaybackService.
 * Menggantikan ExoPlayer langsung → mendukung background playback & notifikasi.
 */
class PlayerManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var playlist: List<Track> = emptyList()

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isConnected by mutableStateOf(false)
        private set

    init {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )

        val future = MediaController.Builder(appContext, sessionToken).buildAsync()

        future.addListener({
            try {
                val c = future.get()
                controller = c

                c.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }

                    override fun onMediaItemTransition(
                        mediaItem: MediaItem?,
                        reason: Int
                    ) {
                        val idx = c.currentMediaItemIndex
                        currentTrack = playlist.getOrNull(idx)
                    }
                })

                isConnected = true
            } catch (e: Exception) {
                // Gagal konek ke service — biarkan isConnected = false
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playTrack(track: Track, tracks: List<Track>) {
        val c = controller ?: return
        if (track.uri.isBlank()) return

        playlist = tracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val items = tracks
            .filter { it.uri.isNotBlank() }
            .map { MediaItem.fromUri(it.uri) }

        c.setMediaItems(items, index, 0L)
        c.prepare()
        c.play()
        currentTrack = track
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
    }

    fun currentPosition(): Long = controller?.currentPosition ?: 0L

    fun duration(): Long = controller?.duration?.coerceAtLeast(0L) ?: 0L

    fun release() {
        controller?.release()
        controller = null
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
