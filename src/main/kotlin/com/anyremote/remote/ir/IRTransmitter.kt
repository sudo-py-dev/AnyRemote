package com.anyremote.remote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log
import android.widget.Toast

/**
 * Handles physical transmission of alternating IR pulse arrays (microseconds) via the native
 * Android ConsumerIrManager API at standard carrier frequencies (38kHz).
 */
object IRTransmitter {
    private const val TAG = "IRTransmitter"

    fun transmit(
        context: Context,
        pulses: IntArray,
        frequencyHz: Int = 38000,
    ) {
        if (pulses.isEmpty()) return

        try {
            val irManager =
                context.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager?
            if (irManager != null && irManager.hasIrEmitter()) {
                Log.i(
                    TAG,
                    "Hardware IR Blaster detected. Transmitting ${pulses.size} pulses at ${frequencyHz}Hz...")
                irManager.transmit(frequencyHz, pulses)
            } else {
                val warningMsg = "Physical IR Blaster not detected on this phone!"
                Log.w(TAG, warningMsg)
                Toast.makeText(context, warningMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while transmitting IR signal: ${e.message}", e)
            Toast.makeText(context, "IR permission denied!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error transmitting hardware IR signal: ${e.message}", e)
            Toast.makeText(context, "Hardware IR Error: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
