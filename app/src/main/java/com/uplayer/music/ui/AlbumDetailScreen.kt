package com.uplayer.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.model.formattedDuration
import com.uplayer.music.player.AlbumArtHelper
import com.uplayer.music.player.PlayerManager

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface = Color(0xFF1E1E1E)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    tracks: List<Track>,
    playerManager: PlayerManager,
    onBack: () -> Unit
) {
    val totalDuration = tracks.sumOf { it.durationMs }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        TopAppBar(
            title = {
                Text(
                    albumName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ===== HEADER =====
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Album art besar
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                    ) {
                        if (tracks.isNotEmpty()) {
                            AsyncImage(
                                model = AlbumArtHelper.getAlbumArtUri(
                                    tracks.first().albumId
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        albumName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "${tracks.size} lagu · ${formatTotalDuration(totalDuration)}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (tracks.isNotEmpty()) {
                                playerManager.playTrack(tracks.first(), tracks)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Putar Semua", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ===== TRACKS =====
            items(tracks, key = { it.id }) { track ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            playerManager.playTrack(track, tracks)
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tracks.indexOf(track) + 1}",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(32.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = track.artist,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = track.formattedDuration(),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatTotalDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val hour = min / 60
    return if (hour > 0) {
        "${hour}j ${min % 60}m"
    } else {
        "${min}m"
    }
}
