package com.uplayer.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    audioSessionId: Int,
    onClose: () -> Unit
) {
    var equalizer by remember { mutableStateOf<Equalizer?>(null) }
    var bassBoost by remember { mutableStateOf<BassBoost?>(null) }

    var enabled by remember { mutableStateOf(false) }
    var bassEnabled by remember { mutableStateOf(false) }

    var bands by remember { mutableStateOf<List<Pair<Short, String>>>(emptyList()) }
    var bandLevels by remember { mutableStateOf<Map<Short, Short>>(emptyMap()) }
    var minLevel by remember { mutableStateOf(-1500.toShort()) }
    var maxLevel by remember { mutableStateOf(1500.toShort()) }

    // Setup equalizer
    LaunchedEffect(audioSessionId) {
        if (audioSessionId == 0) return@LaunchedEffect
        try {
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq

            val numBands = eq.numberOfBands
            val bandList = mutableListOf<Pair<Short, String>>()
            val levels = mutableMapOf<Short, Short>()

            for (i in 0 until numBands) {
                val band = i.toShort()
                val freq = eq.getCenterFreq(band) / 1000 // Hz → kHz
                val label = if (freq >= 1000) "${freq / 1000}kHz" else "${freq}Hz"
                bandList.add(band to label)
                levels[band] = eq.getBandLevel(band)
            }

            bands = bandList
            bandLevels = levels
            minLevel = eq.bandLevelRange[0]
            maxLevel = eq.bandLevelRange[1]

            val bb = BassBoost(0, audioSessionId)
            bassBoost = bb

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            equalizer?.release()
            bassBoost?.release()
        }
    }

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
            // Top bar
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
                    "Equalizer",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        enabled = checked
                        equalizer?.enabled = checked
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = Orange)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Band sliders
            bands.forEach { (band, label) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = Color.White, fontSize = 14.sp)
                        val level = bandLevels[band] ?: 0
                        Text(
                            "${level / 100}dB",
                            color = Orange,
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = (bandLevels[band] ?: 0).toFloat(),
                        onValueChange = { v ->
                            bandLevels = bandLevels + (band to v.toShort())
                            equalizer?.setBandLevel(band, v.toShort())
                        },
                        valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                        enabled = enabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Orange,
                            activeTrackColor = Orange,
                            inactiveTrackColor = Surface2
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Surface2)
            Spacer(Modifier.height(16.dp))

            // Bass boost
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Bass Boost", color = Color.White, fontWeight = FontWeight.Medium)
                    Text("Perkuat nada bass", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = bassEnabled,
                    onCheckedChange = { checked ->
                        bassEnabled = checked
                        bassBoost?.enabled = checked
                        if (checked) bassBoost?.setStrength(750)
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = Orange)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
