package com.anyremote.remote.ir

import androidx.compose.runtime.saveable.listSaver

/** Full Gree AC state; every key press sends the whole state, so it lives in one value. */
data class AcState(
    val power: Boolean = true,
    val temp: Int = 24,
    val mode: String = "MC",
    val speed: String = "SA",
    val swing: Boolean = true,
    /** Fixed vane position 1..5, or 0 when none was chosen. Ignored while [swing] is on. */
    val swingPosition: Int = 0,
    val turbo: Boolean = false,
    val light: Boolean = true,
    val eco: Boolean = false,
    val swingH: Boolean = false,
    val tempDisplay: Boolean = false,
) {
    fun toBytes(): ByteArray =
        IREncodingEngine.greeState(
            power = power,
            temp = temp,
            mode = mode,
            fanSpeed = speed,
            swing = swing,
            swingPosition = swingPosition,
            turbo = turbo,
            light = light,
            eco = eco,
            swingH = swingH,
            tempDisplay = tempDisplay,
        )

    fun toPulses(): IntArray = IREncodingEngine.encodeGree(toBytes())

    companion object {
        val Saver =
            listSaver<AcState, Any>(
                save = {
                    listOf(
                        it.power, it.temp, it.mode, it.speed, it.swing, it.swingPosition, it.turbo,
                        it.light, it.eco, it.swingH, it.tempDisplay)
                },
                restore = {
                    AcState(
                        power = it[0] as Boolean,
                        temp = it[1] as Int,
                        mode = it[2] as String,
                        speed = it[3] as String,
                        swing = it[4] as Boolean,
                        swingPosition = it[5] as Int,
                        turbo = it[6] as Boolean,
                        light = it[7] as Boolean,
                        eco = it[8] as Boolean,
                        swingH = it[9] as Boolean,
                        tempDisplay = it[10] as Boolean,
                    )
                },
            )
    }
}
