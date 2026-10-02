# Kotlin
-keep class kotlin.Metadata { *; }

# Uplayer
-keep class com.uplayer.music.** { *; }

# Media3
-keep class androidx.media3.** { *; }

# jaudiotagger
-keep class net.jthink.** { *; }
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
