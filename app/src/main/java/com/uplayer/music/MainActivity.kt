package com.uplayer.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.uplayer.music.ui.navigation.UplayerNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFF6B00),
                    onPrimary = Color.White,
                    background = Color(0xFF0F0F0F),
                    surface = Color(0xFF1E1E1E),
                    onBackground = Color.White,
                    onSurface = Color.White,
                    surfaceVariant = Color(0xFF2C2C2C),
                    onSurfaceVariant = Color(0xFFB0B0B0)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F0F)
                ) {
                    val navController = rememberNavController()
                    UplayerNavGraph(navController)
                }
            }
        }
    }
}
