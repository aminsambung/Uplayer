package com.uplayer.music.ui

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== COLORS ====================
private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

// ==================== SCREEN ====================
@Composable
fun EqualizerScreen(
    audioSessionId: Int,
    onClose: () -> Unit
) {
    // ===== STATE =====
    var equalizer by remember { mutableStateOf<Equalizer?>(null) }
    var bassBoost by remember { mutableStateOf<BassBoost?>(null) }
    var enabled by remember { mutableStateOf(false) }
    var bassEnabled by remember { mutableStateOf(false) }
    var bassStrength by remember { mutableStateOf(750) }

    var bands by remember { mutableStateOf<List<Pair<Short, String>>>(emptyList()) }
    var bandLevels by remember { mutableStateOf<Map<Short, Short>>(emptyMap()) }
    var minLevel by remember { mutableStateOf((-1500).toShort()) }
    var maxLevel by remember { mutableStateOf(1500.toShort()) }
    var isReady by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ===== SETUP EQUALIZER =====
    LaunchedEffect(audioSessionId) {
        if (audioSessionId == 0) {
            isReady = false
            return@LaunchedEffect
        }

        try {
            // Bikin Equalizer baru
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq

            // Baca band yang tersedia
            val numBands = eq.numberOfBands
            val bandList = mutableListOf<Pair<Short, String>>()
            val levels = mutableMapOf<Short, Short>()

            for (i in 0 until numBands) {
                val band = i.toShort()
                val freqHz = eq.getCenterFreq(band) / 1000 // Hz
                val label = when {
                    freqHz >= 1000 -> "${freqHz / 1000}kHz"
                    else -> "${freqHz}Hz"
                }
                bandList.add(band to label)
                levels[band] = eq.getBandLevel(band)
            }

            bands = bandList
            bandLevels = levels
            minLevel = eq.bandLevelRange[0]
            maxLevel = eq.bandLevelRange[1]

            // BassBoost
            val bb = BassBoost(0, audioSessionId)
            bassBoost = bb

            isReady = true
            errorMessage = null

        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.message ?: "Gagal inisialisasi equalizer"
            isReady = false
        }
    }

    // Release saat keluar
    DisposableEffect(Unit) {
        onDispose {
            try {
                equalizer?.enabled = false
                equalizer?.release()
            } catch (_: Exception) {}
            try {
                bassBoost?.enabled = false
                bassBoost?.release()
            } catch (_: Exception) {}
        }
    }

    // ===== UI =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ===== TOP BAR =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Equalizer",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        enabled = checked
                        try {
                            equalizer?.enabled = checked
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    enabled = isReady,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Orange,
                        uncheckedTrackColor = Surface2,
                        checkedThumbColor = Color.White,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== KONTEN =====
            when {
                // Belum ada lagu diputar
                audioSessionId == 0 -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("🎵", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Putar lagu dulu",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Equalizer aktif saat musik sedang diputar",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Ada error
                errorMessage != null -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Equalizer tidak tersedia",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                errorMessage ?: "Perangkat tidak mendukung",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Loading
                !isReady -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Orange)
                            Spacer(Modifier.height(16.dp))
                            Text("Memuat equalizer...", color = TextSecondary)
                        }
                    }
                }

                // Ready — tampilkan slider
                else -> {
                    // Info
                    Text(
                        "Geser untuk atur nada",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Band sliders
                    bands.forEach { (band, label) ->
                        BandSlider(
                            label = label,
                            level = bandLevels[band] ?: 0,
                            minLevel = minLevel,
                            maxLevel = maxLevel,
                            enabled = enabled,
                            onValueChange = { v ->
                                bandLevels = bandLevels + (band to v)
                                try {
                                    equalizer?.setBandLevel(band, v)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Reset button
                    Button(
                        onClick = {
                            try {
                                equalizer?.usePreset(0) // Normal preset
                                val newLevels = mutableMapOf<Short, Short>()
                                bands.forEach { (band, _) ->
                                    newLevels[band] = equalizer?.getBandLevel(band) ?: 0
                                }
                                bandLevels = newLevels
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Surface2,
                            contentColor = Color.White,
                            disabledContainerColor = Surface2.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Reset ke Normal")
                    }

                    Spacer(Modifier.height(24.dp))
                    Divider(color = Surface2)
                    Spacer(Modifier.height(24.dp))

                    // BassBoost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Bass Boost",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Perkuat nada bass",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = bassEnabled,
                            onCheckedChange = { checked ->
                                bassEnabled = checked
                                try {
                                    bassBoost?.enabled = checked
                                    if (checked) bassBoost?.setStrength(bassStrength.toShort())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            enabled = isReady,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Orange,
                                uncheckedTrackColor = Surface2
                            )
                        )
                    }

                    // Bass strength slider
                    if (bassEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Kekuatan: ${bassStrength / 10}%",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = bassStrength.toFloat(),
                            onValueChange = { v ->
                                bassStrength = v.toInt()
                                try {
                                    bassBoost?.setStrength(v.toInt().toShort())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Orange,
                                activeTrackColor = Orange,
                                inactiveTrackColor = Surface2
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ==================== BAND SLIDER ====================
@Composable
private fun BandSlider(
    label: String,
    level: Short,
    minLevel: Short,
    maxLevel: Short,
    enabled: Boolean,
    onValueChange: (Short) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(
                text = "${level.toInt() / 100}dB",
                color = Orange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = level.toFloat(),
            onValueChange = { v ->
                onValueChange(v.toInt().toShort())
            },
            valueRange = minLevel.toFloat()..maxLevel.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Orange,
                activeTrackColor = Orange,
                inactiveTrackColor = Surface2,
                disabledThumbColor = Color.Gray,
                disabledActiveTrackColor = Orange.copy(alpha = 0.3f),
                disabledInactiveTrackColor = Surface2
            )
        )
    }
}
