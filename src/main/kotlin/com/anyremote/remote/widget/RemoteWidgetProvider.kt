package com.anyremote.remote.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.anyremote.remote.MainActivity
import com.anyremote.remote.R
import com.anyremote.remote.ir.IREncodingEngine
import com.anyremote.remote.ir.IRTransmitter

class RemoteWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_WIDGET_POWER = "com.anyremote.remote.widget.ACTION_WIDGET_POWER"
        const val ACTION_WIDGET_UP = "com.anyremote.remote.widget.ACTION_WIDGET_UP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_WIDGET_POWER -> {
                // Compile the simulated TV/AC Power IR pulse train
                val pulses =
                    IREncodingEngine.compileGreeAc(
                        power = true,
                        temp = 24,
                        mode = "MC",
                        fanSpeed = "SA",
                        swing = true,
                    )
                val pulseString = pulses.take(5).joinToString(", ") + "..."

                // Physically transmit the widget click via Android IR Blaster
                IRTransmitter.transmit(context, pulses, 38000)

                Toast
                    .makeText(
                        context,
                        "Widget sent: POWER PULSE [$pulseString]",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            ACTION_WIDGET_UP -> {
                // Compile the simulated Volume Up command pulse train
                val pulses = IREncodingEngine.compileGenericNec(address = 1, command = 18)
                val pulseString = pulses.take(5).joinToString(", ") + "..."

                // Physically transmit the widget click via Android IR Blaster
                IRTransmitter.transmit(context, pulses, 38200)

                Toast
                    .makeText(
                        context,
                        "Widget sent: VOLUME UP PULSE [$pulseString]",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.remote_widget)

        // 1. Setup Intent for Power Button click
        val powerIntent =
            Intent(context, RemoteWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_POWER
            }
        val powerPendingIntent =
            PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 1,
                powerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.btn_widget_power, powerPendingIntent)

        // 2. Setup Intent for Volume/Temp Up Button click
        val upIntent =
            Intent(context, RemoteWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_UP
            }
        val upPendingIntent =
            PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                upIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.btn_widget_up, upPendingIntent)

        // 3. Setup Intent to Launch Full Application
        val appIntent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val appPendingIntent =
            PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 3,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.btn_widget_open, appPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
