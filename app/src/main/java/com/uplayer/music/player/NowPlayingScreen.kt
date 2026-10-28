package com.uplayer.music.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// ==================== COLORS ====================
private val DarkBg = Color(0xFF0F0F0F)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB0B0B0)
private val SliderRed = Color(0xFFFF5252)

@Composable
fun NowPlayingScreen(
    playerManager: PlayerManager,
    onClose: () -> Unit
) {
    val track = playerManager.currentTrack ?: return
    val isPlaying = playerManager.isPlaying

    // ===== PROGRESS STATE =====
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(playerManager.duration()) }
    var isDragging by remember { mutableFloatStateOf(0f) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    // ===== ROTATION ANIMATION =====
    val infiniteTransition = rememberInfiniteTransition(label = "album_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing), // 20 detik per putaran
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val currentRotation = if (isPlaying) rotation else 0f

    // ===== UPDATE POSITION =====
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
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        // 1. BACKGROUND BLUR (Dari Album Art)
        AsyncImage(
            model = AlbumArtHelper.getAlbumArtUri(track.albumId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
        )

        // Overlay gelap agar teks jelas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        // 2. KONTEN UTAMA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // ----- TOP BAR -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "⋮",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // ----- INFO LAGU -----
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
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
            }

            Spacer(Modifier.height(32.dp))

            // ----- ALBUM ART LINGKARAN (BERPUTAR) -----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .rotate(currentRotation)
                        // Hapus efek clip dan ubah ke CircleShape
                        .clip(CircleShape) 
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ----- INFO TAMBAHAN -----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.album,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = " ›",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // ----- KONTROL PLAY / PAUSE -----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerManager.previous() }) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Prev",
                        tint = TextPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { playerManager.togglePlayPause() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = TextPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                IconButton(onClick = { playerManager.next() }) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ----- SLIDER & WAKTU -----
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
                        thumbColor = Color.White,
                        activeTrackColor = SliderRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(if (isDragging == 1f) dragValue.toLong() else position.toLong()),
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

            // ----- BOTTOM CONTROLS -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerManager.cycleRepeatMode() }) {
                    Text(
                        "🔁",
                        fontSize = 20.sp,
                        color = if (playerManager.repeatMode > 0) SliderRed else TextSecondary
                    )
                }

                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { /* TODO */ }) {
                    Text("🎛️", fontSize = 20.sp, color = TextSecondary)
                }

                IconButton(onClick = { /* TODO */ }) {
                    Text("📤", fontSize = 20.sp, color = TextSecondary)
                }

                IconButton(onClick = { /* TODO */ }) {
                    Text("☰", fontSize = 24.sp, color = TextSecondary)
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
