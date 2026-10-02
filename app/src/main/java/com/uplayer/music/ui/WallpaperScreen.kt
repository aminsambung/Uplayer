package com.uplayer.music.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uplayer.music.data.preferences.WallpaperConfig
import com.uplayer.music.data.preferences.WallpaperPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface = Color(0xFF1E1E1E)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf(WallpaperConfig()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load config dari DataStore
    LaunchedEffect(Unit) {
        config = WallpaperPreferences.observe(context).first()
        isLoading = false
    }

    // Photo picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            config = config.copy(type = "gallery", imageUri = it.toString())
        }
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        TopAppBar(
            title = {
                Text("Wallpaper", color = Color.White, fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== PREVIEW =====
            Text(
                "Preview",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))

            WallpaperPreview(
                config = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(24.dp))

            // ===== SUMBER WALLPAPER =====
            Text(
                "Sumber Wallpaper",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SourceChip(
                    label = "💿 Album",
                    selected = config.type == "album_blur",
                    onClick = { config = config.copy(type = "album_blur") },
                    modifier = Modifier.weight(1f)
                )
                SourceChip(
                    label = "🖼️ Galeri",
                    selected = config.type == "gallery",
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SourceChip(
                    label = "🎨 Solid",
                    selected = config.type == "solid",
                    onClick = { config = config.copy(type = "solid") },
                    modifier = Modifier.weight(1f)
                )
                SourceChip(
                    label = "🌈 Gradient",
                    selected = config.type == "gradient",
                    onClick = { config = config.copy(type = "gradient") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== EFEK =====
            Text(
                "Efek",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))

            EffectSlider(
                label = "Blur",
                value = config.blurRadius,
                range = 0f..50f,
                valueText = "${config.blurRadius.toInt()}px",
                onValueChange = { config = config.copy(blurRadius = it) }
            )

            EffectSlider(
                label = "Kecerahan",
                value = config.opacity,
                range = 0.3f..1f,
                valueText = "${(config.opacity * 100).toInt()}%",
                onValueChange = { config = config.copy(opacity = it) }
            )

            EffectSlider(
                label = "Overlay Gelap",
                value = config.darkOverlay,
                range = 0f..0.8f,
                valueText = "${(config.darkOverlay * 100).toInt()}%",
                onValueChange = { config = config.copy(darkOverlay = it) }
            )

            Spacer(Modifier.height(24.dp))

            // ===== TOMBOL =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            WallpaperPreferences.reset(context)
                            config = WallpaperConfig()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset", color = Color.White)
                }

                Button(
                    onClick = {
                        scope.launch {
                            WallpaperPreferences.save(context, config)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
fun WallpaperPreview(
    config: WallpaperConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        when (config.type) {
            "solid" -> {
                Box(Modifier.fillMaxSize().background(Color(config.colorStart)))
            }
            "gradient" -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color(config.colorStart), Color(config.colorEnd))
                            )
                        )
                )
            }
            "gallery" -> {
                if (config.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = config.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (android.os.Build.VERSION.SDK_INT >= 31) {
                                    Modifier.blur(config.blurRadius.dp)
                                } else Modifier
                            )
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Surface2))
                }
            }
            else -> { // album_blur
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Color(0xFF8A2BE2), Color(0xFF0F0F0F))
                            )
                        )
                )
            }
        }

        // Overlay gelap
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = config.darkOverlay))
        )

        // Teks preview
        Text(
            "SEDANG DIPUTAR",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SourceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Orange.copy(alpha = 0.2f) else Surface)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Orange else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(valueText, color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Orange,
                activeTrackColor = Orange,
                inactiveTrackColor = Surface2
            )
        )
    }
}
