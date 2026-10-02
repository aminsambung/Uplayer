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
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.uplayer.music.data.local.FavoriteRepository
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.model.formattedDuration
import com.uplayer.music.player.AlbumArtHelper
import com.uplayer.music.player.NowPlayingScreen
import com.uplayer.music.player.PlayerManager
import com.uplayer.music.ui.EqualizerScreen
import com.uplayer.music.ui.TagEditorScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ==================== COLORS ====================
val UplayerOrange = Color(0xFFFF6B00)
val UplayerRed = Color(0xFFEF4444)
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
                Surface(Modifier.fillMaxSize(), color = UplayerDarkBg) {
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
    var showNowPlaying by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var tagEditPath by remember { mutableStateOf<String?>(null) }

    // Tag Editor overlay
    tagEditPath?.let { path ->
        TagEditorScreen(
            filePath = path,
            onClose = { tagEditPath = null },
            onSaved = { tagEditPath = null }
        )
        return
    }

    // Equalizer overlay
    if (showEqualizer) {
        LaunchedEffect(Unit) {
            playerManager.refreshAudioSessionId()
        }
        EqualizerScreen(
            audioSessionId = playerManager.audioSessionId,
            onClose = { showEqualizer = false }
        )
        return
    }

    // Now Playing overlay
    if (showNowPlaying) {
        NowPlayingScreen(
            playerManager = playerManager,
            onClose = { showNowPlaying = false }
        )
        return
    }

    Scaffold(
        containerColor = UplayerDarkBg,
        bottomBar = {
            Column {
                playerManager.currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = playerManager.isPlaying,
                        onPlayPause = { playerManager.togglePlayPause() },
                        onNext = { playerManager.next() },
                        onExpand = { showNowPlaying = true }
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
                0 -> LibraryScreen(
                    playerManager = playerManager,
                    onEditTag = { path -> tagEditPath = path }
                )
                1 -> PlaceholderScreen("Search", Icons.Filled.Search)
                2 -> SettingsScreen(
                    onOpenEqualizer = { showEqualizer = true }
                )
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
    onNext: () -> Unit,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UplayerSurface)
            .clickable { onExpand() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UplayerSurface2)
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
                track.title, color = Color.White,
                fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1
            )
            Text(
                track.artist, color = UplayerTextSecondary,
                fontSize = 11.sp, maxLines = 1
            )
        }
        IconButton(onClick = onPlayPause) {
            Text(
                if (isPlaying) "⏸" else "▶",
                fontSize = 22.sp, color = UplayerOrange
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
fun LibraryScreen(
    playerManager: PlayerManager,
    onEditTag: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissions = remember {
        buildList {
            add(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissions[0]) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var allTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Favorites
    var selectedTrackForAction by remember { mutableStateOf<Track?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions()
    ) { result -> hasPermission = result[permissions[0]] == true }

    // Load tracks
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            allTracks = loadTracksFromDevice(context)
            isLoading = false
        }
    }

    // Observe favorites
    LaunchedEffect(Unit) {
        FavoriteRepository.observeFavoriteIds(context).collectLatest { ids ->
            favoriteIds = ids.toSet()
        }
    }

    val displayedTracks = if (selectedTab == 0) {
        allTracks
    } else {
        allTracks.filter { it.id in favoriteIds }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("🎧  Uplayer", color = Color.White, fontWeight = FontWeight.Bold)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )

        if (hasPermission && !isLoading && allTracks.isNotEmpty()) {
            // Tab: Semua / Favorit
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = UplayerDarkBg,
                contentColor = UplayerOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Semua (${allTracks.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "❤️ Favorit (${favoriteIds.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when {
            !hasPermission -> PermissionContent(
                onGrant = { permissionLauncher.launch(permissions) }
            )
            isLoading -> LoadingContent()
            allTracks.isEmpty() -> EmptyContent(
                onRetry = { permissionLauncher.launch(permissions) }
            )
            displayedTracks.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Belum ada favorit",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap ikon hati di lagu untuk menambahkan",
                            color = UplayerTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(displayedTracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            isPlaying = playerManager.currentTrack?.id == track.id
                                    && playerManager.isPlaying,
                            isFavorite = track.id in favoriteIds,
                            onClick = { playerManager.playTrack(track, displayedTracks) },
                            onLongClick = { selectedTrackForAction = track },
                            onFavoriteToggle = {
                                scope.launch {
                                    FavoriteRepository.toggleFavorite(context, track.id)
                                }
                            }
                        )
                        Divider(color = Color(0xFF2C2C2C), thickness = 1.dp)
                    }
                }
            }
        }
    }

    // Bottom sheet
    selectedTrackForAction?.let { track ->
        ModalBottomSheet(
            onDismissRequest = { selectedTrackForAction = null },
            containerColor = UplayerSurface
        ) {
            TrackActionSheet(
                track = track,
                isFavorite = track.id in favoriteIds,
                onDismiss = { selectedTrackForAction = null },
                onEditTag = {
                    if (track.filePath.isNotBlank()) {
                        onEditTag(track.filePath)
                    }
                    selectedTrackForAction = null
                },
                onPlay = {
                    playerManager.playTrack(track, allTracks)
                    selectedTrackForAction = null
                },
                onFavoriteToggle = {
                    scope.launch {
                        FavoriteRepository.toggleFavorite(context, track.id)
                    }
                    selectedTrackForAction = null
                }
            )
        }
    }
}

// ==================== BOTTOM SHEET ====================
@Composable
fun TrackActionSheet(
    track: Track,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onEditTag: () -> Unit,
    onPlay: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UplayerSurface2)
            ) {
                AsyncImage(
                    model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Text(
                    track.artist,
                    color = UplayerTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        Divider(color = UplayerSurface2)

        SheetAction("▶", "Putar Sekarang", onPlay)
        SheetAction(
            if (isFavorite) "💔" else "❤️",
            if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit",
            onFavoriteToggle
        )
        SheetAction("✏️", "Edit Info Lagu", onEditTag)
        SheetAction("ℹ️", "Info File", onDismiss)
    }
}

@Composable
private fun SheetAction(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(20.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== CONTENT STATES ====================
@Composable
fun PermissionContent(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎵", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Izinkan Akses Musik", color = Color.White,
            fontSize = 20.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Untuk memutar musik dari perangkatmu, Uplayer butuh izin.",
            color = UplayerTextSecondary, fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = UplayerOrange),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Izinkan Akses Musik", fontWeight = FontWeight.Bold) }
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
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Tidak Ada Musik", color = Color.White,
            fontSize = 20.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = UplayerOrange)
        ) { Text("Scan Ulang") }
    }
}

// ==================== TRACK ROW ====================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isPlaying) UplayerOrange.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UplayerSurface)
        ) {
            AsyncImage(
                model = AlbumArtHelper.getAlbumArtUri(track.albumId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isPlaying) {
                Box(
                    Modifier.fillMaxSize()
                        .background(UplayerOrange.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) { Text("⏸", fontSize = 20.sp, color = Color.White) }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isPlaying) UplayerOrange else Color.White,
                fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                track.artist, color = UplayerTextSecondary,
                fontSize = 12.sp, maxLines = 1
            )
        }

        // Favorite button
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite
                              else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Hapus favorit" else "Tambah favorit",
                tint = if (isFavorite) UplayerRed else UplayerTextSecondary
            )
        }

        Text(
            track.formattedDuration(),
            color = Color(0xFF6E6E6E), fontSize = 12.sp
        )
    }
}

// ==================== SETTINGS SCREEN ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenEqualizer: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            SettingsItem("Equalizer", "Atur nada musik", onOpenEqualizer)
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, color = UplayerTextSecondary, fontSize = 12.sp)
        }
        Text("›", color = UplayerTextSecondary, fontSize = 24.sp)
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
                    color = UplayerTextSecondary, fontSize = 14.sp,
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
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
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
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
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
                        albumId = cursor.getLong(albumIdCol),
                        durationMs = cursor.getLong(durationCol),
                        uri = uri,
                        filePath = cursor.getString(dataCol) ?: ""
                    )
                )
            }
        }

        result
    }
