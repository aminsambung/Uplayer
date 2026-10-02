package com.uplayer.music.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uplayer.music.data.local.PlaylistRepository
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.model.formattedDuration
import com.uplayer.music.player.AlbumArtHelper
import com.uplayer.music.player.PlayerManager
import kotlinx.coroutines.flow.collectLatest

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface = Color(0xFF1E1E1E)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    allTracks: List<Track>,
    playerManager: PlayerManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var trackIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(playlistId) {
        PlaylistRepository.observePlaylistTracks(context, playlistId)
            .collectLatest { ids -> trackIds = ids }
    }

    val playlistTracks = trackIds.mapNotNull { id ->
        allTracks.find { it.id == id }
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        playlistName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${playlistTracks.size} lagu",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
        )

        if (playlistTracks.isNotEmpty()) {
            Button(
                onClick = {
                    playerManager.playTrack(playlistTracks.first(), playlistTracks)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Putar Semua", fontWeight = FontWeight.Bold)
            }
        }

        if (playlistTracks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎵", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Playlist masih kosong",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Long-press lagu di Library untuk menambahkan",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(playlistTracks, key = { it.id }) { track ->
                    PlaylistTrackRow(
                        track = track,
                        isPlaying = playerManager.currentTrack?.id == track.id
                                && playerManager.isPlaying,
                        onClick = {
                            playerManager.playTrack(track, playlistTracks)
                        },
                        onRemove = {
                            kotlinx.coroutines.MainScope().launch {
                                PlaylistRepository.removeTrackFromPlaylist(
                                    context, playlistId, track.id
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistTrackRow(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .background(
                if (isPlaying) Orange.copy(alpha = 0.1f) else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                .background(Surface2)
        ) {
            AsyncImage(
                model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isPlaying) {
                Box(
                    Modifier.fillMaxSize().background(Orange.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) { Text("⏸", fontSize = 20.sp, color = Color.White) }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isPlaying) Orange else Color.White,
                fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                track.artist, color = TextSecondary,
                fontSize = 12.sp, maxLines = 1
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Hapus dari playlist",
                tint = TextSecondary
            )
        }
    }
}

// Helper — supaya tidak perlu import launch di atas
private val kotlinx.coroutines.MainScope = kotlinx.coroutines.CoroutineScope(
    kotlinx.coroutines.Dispatchers.Main
)
