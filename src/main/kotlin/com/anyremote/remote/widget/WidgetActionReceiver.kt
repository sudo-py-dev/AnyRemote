package com.anyremote.remote.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.ir.IrCodebook
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.service.FavoriteManager

/**
 * Handles the widget's button taps. Declared android:exported="false", so only this app's own
 * PendingIntents can reach it; other apps can't make the phone fire IR codes.
 */
class WidgetActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_POWER = "com.anyremote.remote.widget.ACTION_WIDGET_POWER"
        const val ACTION_UP = "com.anyremote.remote.widget.ACTION_WIDGET_UP"
        private const val TV_DEVICE_ID = 1
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            ACTION_POWER -> {
                val on = !FavoriteManager.getWidgetAcPower(context)
                if (IRTransmitter.transmit(context, AcState(power = on).toPulses())) {
                    FavoriteManager.setWidgetAcPower(context, on)
                }
            }
            ACTION_UP ->
                IRTransmitter.transmit(
                    context, IrCodebook.pulses(TV_DEVICE_ID, codeset = 1, RemoteKey.VOLUME_UP))
        }
    }
}
