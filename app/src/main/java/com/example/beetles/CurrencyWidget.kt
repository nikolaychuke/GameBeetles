package com.example.beetles

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CurrencyWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CurrencyWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val loading = RemoteViews(context.packageName, R.layout.widget_currency)
        loading.setTextViewText(R.id.widgetRate, "...")
        attachRefresh(loading, context)
        appWidgetManager.updateAppWidget(appWidgetId, loading)

        CoroutineScope(Dispatchers.IO).launch {
            val rate = CurrencyRepository().getYuanRate()

            val display = if (rate != null) {
                String.format("%.4f", rate)
            } else {
                "—"
            }

            Handler(Looper.getMainLooper()).post {
                val updated = RemoteViews(context.packageName, R.layout.widget_currency)
                updated.setTextViewText(R.id.widgetRate, display)
                attachRefresh(updated, context)
                appWidgetManager.updateAppWidget(appWidgetId, updated)
            }
        }
    }

    private fun attachRefresh(views: RemoteViews, context: Context) {
        val intent = Intent(context, CurrencyWidget::class.java).apply {
            action = ACTION_REFRESH
        }
        val pending = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetRoot, pending)
    }

    companion object {
        const val ACTION_REFRESH = "com.example.beetles.ACTION_REFRESH"
    }
}