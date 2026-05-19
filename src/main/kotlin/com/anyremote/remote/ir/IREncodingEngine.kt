package com.anyremote.remote.ir

import android.util.Log

object IREncodingEngine {
    private const val TAG = "IREncodingEngine"

    fun compileGreeAc(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        swing: Boolean,
    ): IntArray {
        val state = ByteArray(8)

        val modeVal =
            when (mode) {
                "MA" -> 0
                "MC" -> 1
                "MD" -> 2
                "MV" -> 3
                "MH" -> 4
                else -> 1
            }
        val powerVal = if (power) 1 else 0
        val fanVal =
            when (fanSpeed) {
                "SA" -> 0
                "SL" -> 1
                "SM" -> 2
                "SH" -> 3
                else -> 0
            }
        val swingAutoVal = if (swing) 1 else 0
        val sleepVal = 0

        state[0] =
            ((modeVal and 0x07) or
                    ((powerVal and 0x01) shl 3) or
                    ((fanVal and 0x03) shl 4) or
                    ((swingAutoVal and 0x01) shl 6) or
                    ((sleepVal and 0x01) shl 7))
                .toByte()

        val tempC = temp.coerceIn(16, 30)
        val tempVal = tempC - 16
        state[1] = (tempVal and 0x0F).toByte()

        state[2] = 0x20.toByte()

        state[3] = 0x50.toByte()

        val swingVVal = if (swing) 1 else 0
        state[4] = (swingVVal and 0x0F).toByte()

        state[5] = 0x21.toByte()

        state[6] = 0x00.toByte()

        var sum = 10
        for (i in 0..3) {
            sum += (state[i].toInt() and 0x0F)
        }
        for (i in 4..6) {
            sum += ((state[i].toInt() and 0xFF) shr 4)
        }
        val checksumVal = sum and 0x0F
        state[7] = (checksumVal shl 4).toByte()

        val hexState = state.joinToString(" ") { "%02X".format(it) }
        Log.i(TAG, "Compiled Gree AC 8-byte state: $hexState (Checksum nibble: $checksumVal)")

        val pulses = mutableListOf<Int>()

        pulses.add(9000)
        pulses.add(4500)

        for (i in 0..3) {
            val b = state[i].toInt() and 0xFF
            for (bitIndex in 0..7) {
                val bit = (b shr bitIndex) and 1
                pulses.add(620)
                if (bit == 1) {
                    pulses.add(1600)
                } else {
                    pulses.add(540)
                }
            }
        }

        val footerBits = intArrayOf(0, 1, 0)
        for (bit in footerBits) {
            pulses.add(620)
            if (bit == 1) {
                pulses.add(1600)
            } else {
                pulses.add(540)
            }
        }

        pulses.add(620)
        pulses.add(19980)

        for (i in 4..7) {
            val b = state[i].toInt() and 0xFF
            for (bitIndex in 0..7) {
                val bit = (b shr bitIndex) and 1
                pulses.add(620)
                if (bit == 1) {
                    pulses.add(1600)
                } else {
                    pulses.add(540)
                }
            }
        }

        pulses.add(620)
        pulses.add(19980)

        return pulses.toIntArray()
    }

    fun compileGenericNec(
        address: Int,
        command: Int,
    ): IntArray {

        val pulses = mutableListOf<Int>()

        pulses.add(9000)
        pulses.add(4500)

        val data =
            (address and 0xFF) or
                (((address.inv()) and 0xFF) shl 8) or
                ((command and 0xFF) shl 16) or
                (((command.inv()) and 0xFF) shl 24)

        for (i in 0..31) {
            val bit = (data shr i) and 1
            if (bit == 1) {

                pulses.add(560)
                pulses.add(1690)
            } else {

                pulses.add(560)
                pulses.add(560)
            }
        }

        pulses.add(560)
        pulses.add(20000)

        return pulses.toIntArray()
    }
}
