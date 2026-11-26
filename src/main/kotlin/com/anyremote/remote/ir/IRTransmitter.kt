package com.anyremote.remote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IRTransmitter {
    private const val TAG = "IRTransmitter"

    /** One background lane so frames go out in the order the user pressed the keys. */
    private val irDispatcher = Dispatchers.IO.limitedParallelism(1)

    /** Sends off the main thread; ConsumerIrManager.transmit blocks until the frame is done. */
    suspend fun send(
        context: Context,
        pulses: IntArray,
        frequencyHz: Int = IrCodebook.CARRIER_HZ,
    ): Boolean = withContext(irDispatcher) { transmit(context, pulses, frequencyHz) }

    /** Blocking send. Returns true when the frame was handed to the IR emitter. */
    fun transmit(
        context: Context,
        pulses: IntArray,
        frequencyHz: Int = IrCodebook.CARRIER_HZ,
    ): Boolean {
        if (pulses.isEmpty()) return false
        return try {
            val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager?
            if (irManager?.hasIrEmitter() == true) {
                irManager.transmit(frequencyHz, pulses)
                true
            } else {
                Log.w(TAG, "No IR emitter on this device")
                toast(context, "Physical IR Blaster not detected on this phone!")
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while transmitting IR signal", e)
            toast(context, "IR permission denied!")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error transmitting IR signal", e)
            toast(context, "Hardware IR error")
            false
        }
    }

    private fun toast(context: Context, message: String) {
        val app = context.applicationContext
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
        }
    }
}
