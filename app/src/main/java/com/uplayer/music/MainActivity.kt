package com.uplayer.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uplayer.music.data.SampleData
import com.uplayer.music.domain.model.Track
import com.uplayer.music.domain.model.formattedDuration

// ==================== COLORS ====================
val UplayerOrange = Color(0xFFFF6B00)
val UplayerDarkBg = Color(0xFF0F0F0F)
val UplayerSurface = Color(0xFF1E1E1E)
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
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = UplayerDarkBg,
        bottomBar = {
            NavigationBar(containerColor = UplayerSurface, contentColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.LibraryMusic, null) },
                    label = { Text("Library") },
                    colors = navItemColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Search, null) },
                    label = { Text("Search") },
                    colors = navItemColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.Settings, null) },
                    label = { Text("Settings") },
                    colors = navItemColors()
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> LibraryScreen()
                1 -> PlaceholderScreen("Search", Icons.Rounded.Search)
                2 -> PlaceholderScreen("Settings", Icons.Rounded.Settings)
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

// ==================== LIBRARY SCREEN ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎧 ", fontSize = 20.sp)
                    Text("Uplayer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )

        Text(
            text = "${SampleData.tracks.size} lagu",
            color = UplayerTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(SampleData.tracks, key = { it.id }) { track ->
                TrackRow(track)
                HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun TrackRow(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: play */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UplayerSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.MusicNote, null, tint = UplayerOrange)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
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
fun PlaceholderScreen(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(name, color = Color.White, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = UplayerDarkBg)
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    icon,
                    null,
                    tint = UplayerOrange,
                    modifier = Modifier.size(72.dp)
                )
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
