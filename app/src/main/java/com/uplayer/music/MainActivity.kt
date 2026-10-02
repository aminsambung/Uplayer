package com.uplayer.music

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.model.formattedDuration
import com.uplayer.music.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ==================== COLORS ====================
val UplayerOrange = Color(0xFFFF6B00)
val UplayerDarkBg = Color(0xFF0F0F0F)
val UplayerSurface = Color(0xFF1E1E1E)
val UplayerSurface2 = Color(0xFF2C2C2C)
val UplayerTextSecondary = Color(0xFFB0B0B0)

// ==================== ACTIVITY ====================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = UplayerOrange,
                    background = UplayerDarkBg,
                    surface = UplayerSurface
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = UplayerDarkBg) {
                    MainScreen()
                }
            }
        }
    }
}

// ==================== MAIN SCREEN ====================
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val playerManager = remember { PlayerManager.getInstance(context) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = UplayerDarkBg,
        bottomBar = {
            Column {
                // Mini player di atas bottom nav
                playerManager.currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = playerManager.isPlaying,
                        onPlayPause = { playerManager.togglePlayPause() },
                        onNext = { playerManager.next() }
                    )
                }
                NavigationBar(
                    containerColor = UplayerSurface,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Home, null) },
                        label = { Text("Library") },
                        colors = navItemColors()
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.Search, null) },
                        label = { Text("Search") },
                        colors = navItemColors()
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Settings, null) },
                        label = { Text("Settings") },
                        colors = navItemColors()
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> LibraryScreen(playerManager)
                1 -> PlaceholderScreen("Search", Icons.Filled.Search)
                2 -> PlaceholderScreen("Settings", Icons.Filled.Settings)
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = UplayerOrange,
    selectedTextColor = UplayerOrange,
    indicatorColor = UplayerOrange.copy(alpha = 0.15f),
    unselectedIconColor = UplayerTextSecondary,
    unselectedTextColor = UplayerTextSecondary
)

// ==================== MINI PLAYER ====================
@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UplayerSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UplayerSurface2),
            contentAlignment = Alignment.Center
        ) {
            Text("🎵", fontSize = 18.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = track.artist,
                color = UplayerTextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        IconButton(onClick = onPlayPause) {
            Text(
                text = if (isPlaying) "⏸" else "▶",
                fontSize = 22.sp,
                color = UplayerOrange
            )
        }

        IconButton(onClick = onNext) {
            Text("⏭", fontSize = 20.sp, color = Color.White)
        }
    }
}

// ==================== LIBRARY SCREEN ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(playerManager: PlayerManager) {
    val context = LocalContext.current

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            tracks = loadTracksFromDevice(context)
            isLoading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("🎧  Uplayer", color = Color.White, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )

        when {
            !hasPermission -> PermissionContent(
                onGrant = { permissionLauncher.launch(permission) }
            )
            isLoading -> LoadingContent()
            tracks.isEmpty() -> EmptyContent(
                onRetry = { permissionLauncher.launch(permission) }
            )
            else -> {
                Text(
                    text = "${tracks.size} lagu ditemukan",
                    color = UplayerTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(tracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            isPlaying = playerManager.currentTrack?.id == track.id
                                    && playerManager.isPlaying,
                            onClick = {
                                playerManager.playTrack(track, tracks)
                            }
                        )
                        Divider(color = Color(0xFF2C2C2C), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// ==================== CONTENT STATES ====================
@Composable
fun PermissionContent(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎵", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Izinkan Akses Musik",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Untuk memutar musik dari perangkatmu, Uplayer butuh izin mengakses file audio.",
            color = UplayerTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = UplayerOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Izinkan Akses Musik", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = UplayerOrange)
            Spacer(Modifier.height(16.dp))
            Text("Memindai lagu...", color = UplayerTextSecondary)
        }
    }
}

@Composable
fun EmptyContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Tidak Ada Musik",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Uplayer tidak menemukan file audio di perangkat ini.",
            color = UplayerTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = UplayerOrange)
        ) {
            Text("Scan Ulang")
        }
    }
}

// ==================== TRACK ROW ====================
@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlaying) UplayerOrange.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPlaying) UplayerOrange.copy(alpha = 0.3f) else UplayerSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPlaying) "⏸" else "🎵",
                fontSize = 20.sp,
                color = if (isPlaying) UplayerOrange else Color.Unspecified
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isPlaying) UplayerOrange else Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = UplayerTextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Text(
            text = track.formattedDuration(),
            color = Color(0xFF6E6E6E),
            fontSize = 12.sp
        )
    }
}

// ==================== PLACEHOLDER ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(name: String, icon: ImageVector) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(name, color = Color.White, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = UplayerOrange, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    "$name — Coming soon",
                    color = UplayerTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==================== MEDIA STORE QUERY ====================
suspend fun loadTracksFromDevice(context: android.content.Context): List<Track> =
    withContext(Dispatchers.IO) {
        val result = mutableListOf<Track>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                ).toString()

                result.add(
                    Track(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol)
                            ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                            ?: "Unknown Artist",
                        album = cursor.getString(albumCol)
                            ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                            ?: "Unknown Album",
                        durationMs = cursor.getLong(durationCol),
                        uri = uri
                    )
                )
            }
        }

        result
    }
