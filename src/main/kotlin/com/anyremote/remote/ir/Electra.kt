package com.anyremote.remote.ir

/** Electra Classic INV 17 / AUX YKR-T series. See IRremoteESP8266 ir_Electra.h/.cpp. */
internal fun AcState.electraPulses(): IntArray {
    val frame = IntArray(13)
    frame[0] = 0xc3
    frame[1] = ((temp.coerceIn(16, 32) - 8) shl 3) or if (swing) 0 else 7
    frame[2] = if (swingH) 0 else 0xe0
    frame[4] = when (speed) { "SL" -> 3; "SM" -> 2; "SH" -> 1; else -> 5 } shl 5
    frame[5] = if (turbo) 0x40 else 0
    frame[6] = when (mode) { "MC" -> 1; "MD" -> 2; "MH" -> 4; "MV" -> 6; else -> 0 } shl 5
    frame[9] = if (power) 0x20 else 0
    frame[11] = 0x08 // Leave the display light unchanged.
    frame[12] = frame.sum() and 0xff

    return buildList {
        add(9166)
        add(4470)
        for (byte in frame) for (bit in 0..7) {
            add(646)
            add(if ((byte shr bit) and 1 == 1) 1647 else 547)
        }
        add(646)
        add(100000)
    }.toIntArray()
}
