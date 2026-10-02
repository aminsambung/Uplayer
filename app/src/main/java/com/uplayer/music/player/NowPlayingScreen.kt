package com.uplayer.music.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uplayer.music.data.preferences.WallpaperConfig
import com.uplayer.music.data.preferences.WallpaperPreferences
import kotlinx.coroutines.delay

// ==================== COLORS ====================
private val Orange = Color(0xFFFF6B00)
private val OrangeSoft = Color(0xFFFF8A3D)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

// ==================== SCREEN ====================
@Composable
fun NowPlayingScreen(
    playerManager: PlayerManager,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val track = playerManager.currentTrack ?: return
    val isPlaying = playerManager.isPlaying

    // ===== WALLPAPER =====
    var wallpaperConfig by remember { mutableStateOf(WallpaperConfig()) }
    LaunchedEffect(Unit) {
        WallpaperPreferences.observe(context).collect { cfg ->
            wallpaperConfig = cfg
        }
    }

    // ===== PROGRESS =====
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(playerManager.duration()) }
    var isDragging by remember { mutableFloatStateOf(0f) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(track.id, isPlaying) {
        while (true) {
            if (isDragging == 0f) {
                position = playerManager.currentPosition().toFloat()
                duration = playerManager.duration()
            }
            delay(500)
        }
    }

    // ===== ROOT =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Wallpaper layer
        WallpaperBackground(config = wallpaperConfig)

        // Konten
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // ========== TOP BAR ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== HEADER: ALBUM ART KECIL + INFO LAGU ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art kecil (kiri)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface2)
                ) {
                    AsyncImage(
                        model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Info lagu (kanan)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = track.album,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ========== SLIDER + TIME ==========
            Column(Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isDragging == 1f) dragValue else position,
                    onValueChange = { v ->
                        isDragging = 1f
                        dragValue = v
                    },
                    onValueChangeFinished = {
                        playerManager.seekTo(dragValue.toLong())
                        isDragging = 0f
                    },
                    valueRange = 0f..(duration.coerceAtLeast(1).toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = Orange,
                        activeTrackColor = Orange,
                        inactiveTrackColor = Surface2
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(
                            if (isDragging == 1f) dragValue.toLong()
                            else position.toLong()
                        ),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatMs(duration),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== MAIN CONTROLS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { playerManager.toggleShuffle() }) {
                    Text(
                        text = "🔀",
                        fontSize = 24.sp,
                        color = if (playerManager.isShuffleOn) Orange
                                else TextSecondary
                    )
                }

                // Previous
                IconButton(
                    onClick = { playerManager.previous() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("⏮", fontSize = 32.sp, color = Color.White)
                }

                // Play/Pause besar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Orange, OrangeSoft))
                        )
                        .clickable { playerManager.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }

                // Next
                IconButton(
                    onClick = { playerManager.next() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("⏭", fontSize = 32.sp, color = Color.White)
                }

                // Repeat
                IconButton(onClick = { playerManager.cycleRepeatMode() }) {
                    Text(
                        text = when (playerManager.repeatMode) {
                            1 -> "🔁"
                            2 -> "🔂"
                            else -> "🔁"
                        },
                        fontSize = 24.sp,
                        color = if (playerManager.repeatMode > 0) Orange
                                else TextSecondary
                    )
                }
            }
        }
    }
}

// ==================== WALLPAPER BACKGROUND ====================
@Composable
private fun WallpaperBackground(config: WallpaperConfig) {
    Box(Modifier.fillMaxSize()) {
        when (config.type) {
            "solid" -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(config.colorStart))
                )
            }
            "gradient" -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(config.colorStart),
                                    Color(config.colorEnd)
                                )
                            )
                        )
                )
            }
            "gallery" -> {
                if (config.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = config.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF8A2BE2).copy(alpha = 0.6f),
                                        DarkBg
                                    )
                                )
                            )
                    )
                }
            }
            else -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF8A2BE2).copy(alpha = 0.6f),
                                    DarkBg
                                )
                            )
                        )
                )
            }
        }

        // Overlay gelap
        if (config.darkOverlay > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = config.darkOverlay))
            )
        }
    }
}

// ==================== HELPERS ====================
private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
