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
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.uplayer_widget)

            views.setTextViewText(R.id.widget_title, "Uplayer")
            views.setTextViewText(R.id.widget_artist, "Tap untuk buka")

            // Klik widget buka MainActivity
            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_title, openApp)

            // Play/pause (untuk sekarang, buka app dulu)
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                PendingIntent.getActivity(
                    context, 1,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                PendingIntent.getActivity(
                    context, 2,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, UplayerWidgetProvider::class.java)
            )
            val intent = Intent(context, UplayerWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
