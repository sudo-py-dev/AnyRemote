package com.anyremote.remote.ir

object IREncodingEngine {
    /**
     * Builds the 8-byte Gree AC state. Bit positions follow the Gree layout documented in
     * IRremoteESP8266 (ir_Gree.h):
     *
     * - byte 0: mode (0-2), power (3), fan (4-5), auto swing (6), sleep (7)
     * - byte 1: temperature - 16 (0-3)
     * - byte 2: turbo (4), light (5)
     * - byte 3: fixed 0b0101 in the high nibble
     * - byte 4: vertical swing (0-3), horizontal swing (4-6)
     * - byte 5: temperature display (0-1), fixed 0b100 in bits 3-5
     * - byte 7: econo (2), checksum (4-7)
     */
    fun greeState(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        swing: Boolean,
        swingPosition: Int = 0,
        turbo: Boolean = false,
        light: Boolean = true,
        eco: Boolean = false,
        swingH: Boolean = false,
        tempDisplay: Boolean = false,
    ): ByteArray {
        val modeVal =
            when (mode) {
                "MA" -> 0
                "MC" -> 1
                "MD" -> 2
                "MV" -> 3
                "MH" -> 4
                else -> 1
            }
        val fanVal =
            when (fanSpeed) {
                "SL" -> 1
                "SM" -> 2
                "SH" -> 3
                else -> 0
            }
        // SwingV: 1 = auto sweep, 2..6 = fixed vane positions, 0 = leave as is.
        val swingV =
            when {
                swing -> 1
                swingPosition in 1..5 -> swingPosition + 1
                else -> 0
            }
        fun Boolean.bit(shift: Int) = if (this) 1 shl shift else 0
        val on = power

        val state = ByteArray(8)
        state[0] = (modeVal or power.bit(3) or (fanVal shl 4) or swing.bit(6)).toByte()
        state[1] = ((temp.coerceIn(16, 30) - 16) and 0x0F).toByte()
        state[2] = ((on && turbo).bit(4) or (on && light).bit(5)).toByte()
        state[3] = 0x50
        state[4] = (swingV or ((if (on && swingH) 1 else 0) shl 4)).toByte()
        state[5] = (0x20 or (if (on && tempDisplay) 2 else 1)).toByte()
        state[7] = ((on && eco).bit(2) or (greeChecksum(state) shl 4)).toByte()
        return state
    }

    /** 10 + low nibbles of bytes 0-3 + high nibbles of bytes 4-6, keeping the low 4 bits. */
    fun greeChecksum(state: ByteArray): Int {
        var sum = 10
        for (i in 0..3) sum += state[i].toInt() and 0x0F
        for (i in 4..6) sum += (state[i].toInt() and 0xFF) shr 4
        return sum and 0x0F
    }

    /** Gree timing: header, 4 bytes + 3-bit footer, gap, 4 bytes, gap. Bits are sent LSB first. */
    fun encodeGree(state: ByteArray): IntArray {
        val pulses = mutableListOf(9000, 4500)
        fun bit(b: Int) {
            pulses += 620
            pulses += if (b == 1) 1600 else 540
        }
        fun bytes(range: IntRange) = range.forEach { i ->
            val b = state[i].toInt() and 0xFF
            for (bitIndex in 0..7) bit((b shr bitIndex) and 1)
        }

        bytes(0..3)
        intArrayOf(0, 1, 0).forEach(::bit)
        pulses += listOf(620, 19980)
        bytes(4..7)
        pulses += listOf(620, 19980)
        return pulses.toIntArray()
    }

    fun compileGenericNec(
        address: Int,
        command: Int,
        secondAddress: Int = address.inv(),
    ): IntArray {
        val pulses = mutableListOf(9000, 4500)
        val data =
            (address and 0xFF) or
                ((secondAddress and 0xFF) shl 8) or
                ((command and 0xFF) shl 16) or
                ((command.inv() and 0xFF) shl 24)
        for (i in 0..31) {
            pulses += 560
            pulses += if ((data shr i) and 1 == 1) 1690 else 560
        }
        pulses += listOf(560, 20000)
        return pulses.toIntArray()
    }
}
