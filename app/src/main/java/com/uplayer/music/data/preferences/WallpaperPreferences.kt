package com.uplayer.music.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wallpaperDataStore by preferencesDataStore("uplayer_wallpaper")

data class WallpaperConfig(
    val type: String = "album_blur",      // "theme", "solid", "gradient", "gallery", "album_blur"
    val imageUri: String = "",             // untuk gallery
    val colorStart: Long = 0xFF8A2BE2,    // untuk solid & gradient
    val colorEnd: Long = 0xFF0F0F0F,      // untuk gradient
    val blurRadius: Float = 20f,          // 0-50
    val opacity: Float = 0.7f,            // 0.3-1.0
    val darkOverlay: Float = 0.4f         // 0-0.8
)

object WallpaperPreferences {

    private val KEY_TYPE = stringPreferencesKey("type")
    private val KEY_URI = stringPreferencesKey("image_uri")
    private val KEY_COLOR_START = stringPreferencesKey("color_start")
    private val KEY_COLOR_END = stringPreferencesKey("color_end")
    private val KEY_BLUR = floatPreferencesKey("blur")
    private val KEY_OPACITY = floatPreferencesKey("opacity")
    private val KEY_OVERLAY = floatPreferencesKey("overlay")

    fun observe(context: Context): Flow<WallpaperConfig> {
        return context.wallpaperDataStore.data.map { prefs ->
            WallpaperConfig(
                type = prefs[KEY_TYPE] ?: "album_blur",
                imageUri = prefs[KEY_URI] ?: "",
                colorStart = prefs[KEY_COLOR_START]?.toLongOrNull() ?: 0xFF8A2BE2,
                colorEnd = prefs[KEY_COLOR_END]?.toLongOrNull() ?: 0xFF0F0F0F,
                blurRadius = prefs[KEY_BLUR] ?: 20f,
                opacity = prefs[KEY_OPACITY] ?: 0.7f,
                darkOverlay = prefs[KEY_OVERLAY] ?: 0.4f
            )
        }
    }

    suspend fun save(context: Context, config: WallpaperConfig) {
        context.wallpaperDataStore.edit { prefs ->
            prefs[KEY_TYPE] = config.type
            prefs[KEY_URI] = config.imageUri
            prefs[KEY_COLOR_START] = config.colorStart.toString()
            prefs[KEY_COLOR_END] = config.colorEnd.toString()
            prefs[KEY_BLUR] = config.blurRadius
            prefs[KEY_OPACITY] = config.opacity
            prefs[KEY_OVERLAY] = config.darkOverlay
        }
    }

    suspend fun reset(context: Context) {
        save(context, WallpaperConfig())
    }
}
