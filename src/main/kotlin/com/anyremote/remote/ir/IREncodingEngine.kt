package com.anyremote.remote.ir

import android.util.Log

object IREncodingEngine {
    private const val TAG = "IREncodingEngine"

    /**
     * Compiles Gree AC configuration into the exact 8-byte state byte array and converts it to a
     * microsecond pulse sequence (alternating high/low).
     */
    fun compileGreeAc(
        power: Boolean,
        temp: Int,
        mode: String, // "MA", "MC", "MD", "MV", "MH"
        fanSpeed: String, // "SA", "SL", "SM", "SH"
        swing: Boolean, // true (WA / Auto), false (WM / Manual)
    ): IntArray {
        val state = ByteArray(8)

        // 1. Byte 0: Mode (bits 0..2), Power (bit 3), Fan (bits 4..5), SwingAuto (bit 6), Sleep
        // (bit 7)
        val modeVal =
            when (mode) {
                "MA" -> 0
                "MC" -> 1
                "MD" -> 2
                "MV" -> 3
                "MH" -> 4
                else -> 1 // Default to Cool
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

        // 2. Byte 1: Temperature (bits 0..3: temp - 16)
        val tempC = temp.coerceIn(16, 30)
        val tempVal = tempC - 16
        state[1] = (tempVal and 0x0F).toByte() // No timer, timer bits are 0

        // 3. Byte 2: Light (bit 5) -> default active (0x20)
        state[2] = 0x20.toByte()

        // 4. Byte 3: unknown1 = 5 (0x50 in high nibble)
        state[3] = 0x50.toByte()

        // 5. Byte 4: SwingV (bits 0..3) -> 1 if swing is enabled, else 0
        val swingVVal = if (swing) 1 else 0
        state[4] = (swingVVal and 0x0F).toByte() // SwingH = 0

        // 6. Byte 5: unknown2 = 4 (0x20 in bits 3..5), DisplayTemp = 1 (0x01 in bits 0..1)
        state[5] = 0x21.toByte()

        // 7. Byte 6: 0x00
        state[6] = 0x00.toByte()

        // 8. Byte 7: Sum checksum calculation
        var sum = 10
        for (i in 0..3) {
            sum += (state[i].toInt() and 0x0F)
        }
        for (i in 4..6) {
            sum += ((state[i].toInt() and 0xFF) shr 4)
        }
        val checksumVal = sum and 0x0F
        state[7] = (checksumVal shl 4).toByte()

        // Log the compiled state for debugging
        val hexState = state.joinToString(" ") { "%02X".format(it) }
        Log.i(TAG, "Compiled Gree AC 8-byte state: $hexState (Checksum nibble: $checksumVal)")

        // 9. Convert to Microsecond Pulse Sequence
        val pulses = mutableListOf<Int>()

        // --- BLOCK 1 ---
        // Header
        pulses.add(9000)
        pulses.add(4500)

        // Data bytes 0 to 3
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

        // Footer 1: 0b010 (3 bits, LSB-first: 0, 1, 0)
        val footerBits = intArrayOf(0, 1, 0)
        for (bit in footerBits) {
            pulses.add(620)
            if (bit == 1) {
                pulses.add(1600)
            } else {
                pulses.add(540)
            }
        }

        // Trailing Footer 1 Mark and Gap (20ms)
        pulses.add(620)
        pulses.add(19980)

        // --- BLOCK 2 (No Header!) ---
        // Data bytes 4 to 7
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

        // Trailing Footer 2 Mark and End Gap
        pulses.add(620)
        pulses.add(19980)

        return pulses.toIntArray()
    }

    /**
     * Generates a pulse sequence for other device keys (TV power, vol, etc.). These use standard
     * NEC carrier encoding rules for verification.
     */
    fun compileGenericNec(
        address: Int,
        command: Int,
    ): IntArray {
        // Address and command translated to 32-bit NEC frame: [Addr, ~Addr, Cmd, ~Cmd]
        val pulses = mutableListOf<Int>()

        // NEC Lead code: 9000us high, 4500us low
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
                // Bit 1: 560us pulse, 1690us space
                pulses.add(560)
                pulses.add(1690)
            } else {
                // Bit 0: 560us pulse, 560us space
                pulses.add(560)
                pulses.add(560)
            }
        }

        // End code
        pulses.add(560)
        pulses.add(20000)

        return pulses.toIntArray()
    }
}
