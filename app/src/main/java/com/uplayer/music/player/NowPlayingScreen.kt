package com.uplayer.music.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ==================== COLORS (Neon Theme) ====================
private val DarkBg = Color(0xFF0F0B1A)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFA09EB5)
private val NeonPink = Color(0xFFFF4B91)
private val NeonPurple = Color(0xFF8A2BE2)

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
    
    // State untuk Slider Lurus
    var isDraggingLinear by remember { mutableFloatStateOf(0f) }
    var dragValueLinear by remember { mutableFloatStateOf(0f) }

    // ===== UPDATE POSITION =====
    LaunchedEffect(track.id, isPlaying) {
        while (true) {
            if (isDraggingLinear == 0f) {
                position = playerManager.currentPosition().toFloat()
                duration = playerManager.duration()
            }
            delay(500)
        }
    }

    // ===== GESTURE STATE =====
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val pillAlpha = remember { Animatable(0f) }
    var pillText by remember { mutableStateOf("") }
    val swipeThreshold = 120f 

    // ===== ROOT =====
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        // 1. BACKGROUND BLUR
        AsyncImage(
            model = AlbumArtHelper.getAlbumArtUri(track.albumId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .alpha(0.5f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg.copy(alpha = 0.8f))
        )

        // 2. KONTEN UTAMA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                // Tap lama di mana saja untuk menutup
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onClose() }
                    )
                }
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Tutup", tint = TextPrimary)
                }
                IconButton(onClick = { /* TODO: Menu */ }) {
                    Text("⋮", color = TextPrimary, fontSize = 24.sp)
                }
            }

            // --- INFO LAGU ---
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

            // --- ALBUM ART DENGAN GESTURE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    // Gesture untuk gambar album
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { playerManager.togglePlayPause() }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { 
                                scope.launch {
                                    offsetX.snapTo(0f) 
                                    pillAlpha.snapTo(1f)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val newOffset = offsetX.value + dragAmount.x
                                    offsetX.snapTo(newOffset)
                                    pillText = if (newOffset > 0) "⏮ Previous" else "Next ⏭"
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value > swipeThreshold) playerManager.previous()
                                    else if (offsetX.value < -swipeThreshold) playerManager.next()
                                    offsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                    pillAlpha.animateTo(0f, animationSpec = tween(300))
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                    pillAlpha.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)) // Sudut membulat
                        .background(Color.DarkGray)
                )

                // ANIMASI PILL (Muncul saat digeser)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .alpha(pillAlpha.value)
                        .background(color = NeonPink, shape = RoundedCornerShape(50))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(pillText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- SLIDER LURUS & WAKTU ---
            Column(Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isDraggingLinear == 1f) dragValueLinear else position,
                    onValueChange = { v ->
                        isDraggingLinear = 1f
                        dragValueLinear = v
                    },
                    onValueChangeFinished = {
                        playerManager.seekTo(dragValueLinear.toLong())
                        isDraggingLinear = 0f
                    },
                    valueRange = 0f..(duration.coerceAtLeast(1).toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = NeonPink,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(if (isDraggingLinear == 1f) dragValueLinear.toLong() else position.toLong()),
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

            Spacer(Modifier.weight(1f))

            // --- KONTROL BAWAH (Play/Pause Utama & Menu) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Play Utama
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(brush = Brush.linearGradient(colors = listOf(NeonPurple, NeonPink)))
                        // Tetap bisa di-tap biasa (selain tap 2x di gambar)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { playerManager.togglePlayPause() }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶", 
                        fontSize = 36.sp, 
                        color = Color.White
                    )
                }
            }
            
            // --- MENU TAMBAHAN (Love, Shuffle, dll) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Text("🔁", fontSize = 20.sp, color = TextSecondary)
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Filled.FavoriteBorder, "Favorite", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { /* TODO */ }) {
                    Text("≡", fontSize = 26.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
