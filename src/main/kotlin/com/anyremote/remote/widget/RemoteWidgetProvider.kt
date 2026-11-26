package com.anyremote.remote.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.anyremote.remote.MainActivity
import com.anyremote.remote.R

/** Only draws the widget. Button taps go to the non-exported [WidgetActionReceiver]. */
class RemoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.remote_widget)
            views.setOnClickPendingIntent(
                R.id.btn_widget_power,
                actionIntent(context, appWidgetId * 10 + 1, WidgetActionReceiver.ACTION_POWER))
            views.setOnClickPendingIntent(
                R.id.btn_widget_up,
                actionIntent(context, appWidgetId * 10 + 2, WidgetActionReceiver.ACTION_UP))

            val appIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            views.setOnClickPendingIntent(
                R.id.btn_widget_open,
                PendingIntent.getActivity(context, appWidgetId * 10 + 3, appIntent, FLAGS))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun actionIntent(
        context: Context,
        requestCode: Int,
        action: String,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, WidgetActionReceiver::class.java).setAction(action),
            FLAGS,
        )

    private companion object {
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
