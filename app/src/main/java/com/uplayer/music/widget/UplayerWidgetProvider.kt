package com.uplayer.music.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.uplayer.music.MainActivity
import com.uplayer.music.R

class UplayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_PLAY -> { /* TODO: connect to MediaController */ }
            ACTION_NEXT -> { /* TODO: connect to MediaController */ }
            ACTION_PREV -> { /* TODO: connect to MediaController */ }
        }
    }

    companion object {
        const val ACTION_TOGGLE_PLAY = "com.uplayer.music.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.uplayer.music.ACTION_NEXT"
        const val ACTION_PREV = "com.uplayer.music.ACTION_PREV"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.uplayer_widget)

            // Default state (nanti akan diupdate oleh MediaSession callback)
            views.setTextViewText(R.id.widget_title, "Uplayer")
            views.setTextViewText(R.id.widget_artist, "Belum ada lagu")

            // Klik widget → buka MainActivity
            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_album_art, openApp)

            // Tombol Play/Pause
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                buildPending(context, ACTION_TOGGLE_PLAY, 1)
            )

            // Tombol Next
            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                buildPending(context, ACTION_NEXT, 2)
            )

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        private fun buildPending(
            context: Context,
            action: String,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, UplayerWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, UplayerWidgetProvider::class.java)
            )
            ids.forEach { updateWidget(context, manager, it) }
        }
    }
}
