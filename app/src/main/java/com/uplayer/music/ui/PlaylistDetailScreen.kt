package com.uplayer.music.ui

import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.uplayer.music.player.AlbumArtHelper
import com.uplayer.music.player.PlayerManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
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
    val scope = rememberCoroutineScope()
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
                    Text(playlistName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "${playlistTracks.size} lagu",
                        color = TextSecondary, fontSize = 12.sp
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
                        color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Long-press lagu di Library untuk menambahkan",
                        color = TextSecondary, fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(playlistTracks, key = { it.id }) { track ->
                    Row(
                        Modifier
                            .fillMaxWidth()
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
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp, maxLines = 1
                            )
                            Text(
                                track.artist,
                                color = TextSecondary,
                                fontSize = 12.sp, maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    PlaylistRepository.removeTrackFromPlaylist(
                                        context, playlistId, track.id
                                    )
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                "Hapus dari playlist",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
