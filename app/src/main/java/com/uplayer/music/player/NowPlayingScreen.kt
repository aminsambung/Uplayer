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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
private val SurfacePill = Color(0xFF1F1F1F)
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

    // ===== WALLPAPER CONFIG =====
    var wallpaperConfig by remember { mutableStateOf(WallpaperConfig()) }
    LaunchedEffect(Unit) {
        WallpaperPreferences.observe(context).collect { cfg ->
            wallpaperConfig = cfg
        }
    }

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
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val currentRotation = if (isPlaying) rotation else 0f

    // ===== UPDATE POSITION SETIAP 500ms =====
    LaunchedEffect(track.id, isPlaying) {
        while (true) {
            if (isDragging == 0f) {
                position = playerManager.currentPosition().toFloat()
                duration = playerManager.duration()
            }
            delay(500)
        }
    }

    // ===== ROOT BOX =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ===== WALLPAPER LAYER =====
        WallpaperBackground(config = wallpaperConfig)

        // ===== CONTENT =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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
                        Icons.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Now Playing",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                ToolbarIcon("🎛️")
                ToolbarIcon("📊")
                ToolbarIcon("📷")
                ToolbarIcon("⋮")
            }

            Spacer(Modifier.height(16.dp))

            // ========== ALBUM ART + PROGRESS RING ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // Progress ring
                ProgressRing(
                    progress = if (duration > 0)
                        (position / duration).coerceIn(0f, 1f)
                    else 0f,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

                // Album art (kotak rounded, berputar)
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(currentRotation)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface2)
                ) {
                    AsyncImage(
                        model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ========== TIME LABELS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(
                        if (isDragging == 1f) dragValue.toLong()
                        else position.toLong()
                    ),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatMs(duration),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // ========== SLIDER (transparent, untuk drag) ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
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
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // ========== PILL CONTROLS ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PillContainer {
                    PillItem("1.0")
                    PillItem("❤️")
                    PillItem("🔊")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== TRACK INFO ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontSize = 20.sp,
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
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Lyrics",
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ========== MAIN CONTROLS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerManager.toggleShuffle() }) {
                    Text(
                        text = "🔀",
                        fontSize = 24.sp,
                        color = if (playerManager.isShuffleOn) Orange else TextSecondary
                    )
                }

                IconButton(
                    onClick = { playerManager.previous() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("⏮", fontSize = 32.sp, color = Color.White)
                }

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

                IconButton(
                    onClick = { playerManager.next() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("⏭", fontSize = 32.sp, color = Color.White)
                }

                IconButton(onClick = { playerManager.cycleRepeatMode() }) {
                    Text(
                        text = when (playerManager.repeatMode) {
                            1 -> "🔁"
                            2 -> "🔂"
                            else -> "🔁"
                        },
                        fontSize = 24.sp,
                        color = if (playerManager.repeatMode > 0) Orange else TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SLEEP TIMER STRIP ==========
            SleepTimerStrip()

            Spacer(Modifier.height(16.dp))
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
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = config.imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (android.os.Build.VERSION.SDK_INT >= 31
                                        && config.blurRadius > 0f
                                    ) {
                                        Modifier.blur(config.blurRadius.dp)
                                    } else Modifier
                                )
                        )
                    }
                } else {
                    // fallback kalau belum pilih gambar
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
            // "album_blur" dan default
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

        // Overlay gelap untuk readability
        if (config.darkOverlay > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = config.darkOverlay))
            )
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
private fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 6.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = Size(
            size.width - strokeWidth,
            size.height - strokeWidth
        )
        val topLeft = Offset(inset, inset)

        // Background circle
        drawArc(
            color = Color(0xFF2A2A2A).copy(alpha = 0.7f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            brush = Brush.sweepGradient(listOf(Orange, OrangeSoft, Orange)),
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Dot di ujung progress
        val angleRad = Math.toRadians(
            (-90f + 360f * progress.coerceIn(0f, 1f)).toDouble()
        )
        val radius = (size.width - strokeWidth) / 2
        val cx = size.width / 2 + radius * kotlin.math.cos(angleRad).toFloat()
        val cy = size.height / 2 + radius * kotlin.math.sin(angleRad).toFloat()
        drawCircle(
            color = Orange,
            radius = 8.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun ToolbarIcon(emoji: String) {
    IconButton(onClick = {}) {
        Text(emoji, fontSize = 18.sp)
    }
}

@Composable
private fun PillContainer(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfacePill)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        content()
    }
}

@Composable
private fun PillItem(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SleepTimerStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SleepChip("60<")
        SleepChip("30<")
        SleepChip("5<")
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfacePill),
            contentAlignment = Alignment.Center
        ) {
            Text("🌙", fontSize = 18.sp)
        }
        SleepChip("5>")
        SleepChip("30>")
        SleepChip("60>")
    }
}

@Composable
private fun SleepChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { /* TODO: set sleep timer */ }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== HELPERS ====================
private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
