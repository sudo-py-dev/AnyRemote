package com.anyremote.remote.ir

enum class AcProtocol(val minTemp: Int, val maxTemp: Int) {
    GREE(16, 30),
    ELECTRA(16, 32),
    AMCOR(12, 32),
}

/** Amcor ADR-853H, TAC-444 and TAC-495. Frame format and repeat timing from IRremoteESP8266. */
internal fun AcState.amcorPulses(): IntArray {
    val fan =
        when (speed) {
            "SL" -> 1
            "SM" -> 2
            "SH" -> 3
            else -> 4
        }
    val operatingMode =
        when (mode) {
            "MC" -> 1
            "MH" -> 2
            "MV" -> 3
            "MD" -> 4
            else -> 5
        }
    val frame =
        intArrayOf(
            1,
            (fan shl 4) or operatingMode,
            temp.coerceIn(12, 32) shl 1,
            0,
            0,
            if (power) 0x30 else 0xc0,
            if (mode == "MV") 0xc0 else 0,
            0,
        )
    frame[7] = frame.sumOf { (it shr 4) + (it and 15) }
    return buildList {
        repeat(2) {
            add(8200)
            add(4200)
            for (byte in frame) for (bit in 0..7) {
                val one = (byte shr bit) and 1 == 1
                add(if (one) 1500 else 600)
                add(if (one) 600 else 1500)
            }
            add(1900)
            add(34300)
        }
    }
        .toIntArray()
}
