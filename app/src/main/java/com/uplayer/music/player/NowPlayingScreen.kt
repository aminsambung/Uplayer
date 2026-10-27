package com.uplayer.music.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.sin

// ==================== COLORS (LIGHT THEME) ====================
private val LightBg = Color(0xFFF4F6FA)
private val NeonPurple = Color(0xFFB44CF6)
private val NeonPink = Color(0xFFFF4DA6)
private val NeonCyan = Color(0xFF00E5FF)
private val DarkText = Color(0xFF1E1E24)
private val SubText = Color(0xFF6E6E80)

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
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = LinearEasing),
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

    // ===== ROOT (BACKGROUND PUTIH) =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // ========== TOP BAR ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tombol Power/Close
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E5EC))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏻", fontSize = 16.sp, color = DarkText)
                }

                // Judul lagu di tengah (kecil)
                Text(
                    text = track.title,
                    color = DarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp).weight(1f),
                    textAlign = TextAlign.Center
                )

                // Tombol Menu
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E5EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("≡", fontSize = 20.sp, color = DarkText, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== VINYL DISK + GLOW ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // 1. Glow Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        NeonPurple.copy(alpha = 0.3f),
                                        NeonCyan.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 1.5f
                                )
                            )
                        }
                )

                // 2. Vinyl Hitam
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0A14))
                        // Ring neon
                        .drawBehind {
                            drawCircle(
                                color = NeonPurple,
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                )

                // 3. Album Art (berputar)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.65f)
                        .rotate(currentRotation)
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

                // 4. Center Hole (Lubang Tengah)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6))
                        .drawBehind {
                            // Neon border
                            drawCircle(
                                color = NeonPink,
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Tombol Play/Pause di tengah
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        fontSize = 24.sp,
                        color = NeonPurple,
                        modifier = Modifier.clickable { playerManager.togglePlayPause() }
                    )
                }

                // 5. Equalizer Bars (di bawah vinyl)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (i in 0..15) {
                        EqualizerBar(index = i, isPlaying = isPlaying)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ========== SLIDER & WAKTU ==========
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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
                        thumbColor = DarkText,
                        activeTrackColor = DarkText,
                        inactiveTrackColor = Color(0xFFCBD0D8)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(if (isDragging == 1f) dragValue.toLong() else position.toLong()),
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Next", // Sesuai desain
                        color = SubText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatMs(duration),
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== KONTROL UTAMA ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol kiri: Repeat
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E5EC))
                        .clickable { playerManager.cycleRepeatMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔁",
                        fontSize = 18.sp,
                        color = if (playerManager.repeatMode > 0) NeonPurple else DarkText
                    )
                }

                // Tombol Prev
                IconButton(onClick = { playerManager.previous() }) {
                    Text("⏮", fontSize = 28.sp, color = DarkText)
                }

                // Tombol Play Besar
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A24))
                        .drawBehind {
                            drawCircle(
                                color = NeonPink,
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        .clickable { playerManager.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPlaying) "⏸" else "▶",
                        fontSize = 32.sp,
                        color = NeonPink
                    )
                }

                // Tombol Next
                IconButton(onClick = { playerManager.next() }) {
                    Text("⏭", fontSize = 28.sp, color = DarkText)
                }

                // Tombol kanan: Shuffle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E5EC))
                        .clickable { playerManager.toggleShuffle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔀",
                        fontSize = 18.sp,
                        color = if (playerManager.isShuffleOn) NeonPurple else DarkText
                    )
                }
            }
        }
    }
}

// ==================== EQUALIZER BAR ====================
@Composable
private fun EqualizerBar(index: Int, isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bar$index")
    val heightFraction by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500 + (index * 100),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "height$index"
    )

    val finalHeight = if (isPlaying) heightFraction else 0.2f

    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(finalHeight)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonPink, NeonPurple)
                )
            )
    )
}

// ==================== HELPERS ====================
private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
